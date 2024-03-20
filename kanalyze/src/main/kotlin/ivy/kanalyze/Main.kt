/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package ivy.kanalyze

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject

fun main() {
    println("Hello")
    val environment = KotlinCoreEnvironment.createForProduction(
        { },
        CompilerConfiguration(),
        EnvironmentConfigFiles.JVM_CONFIG_FILES,
    )
    val code = """
            package ivy
            
            fun main() {
              println("Hello, world!")
            }
            
            fun test() = 42
            
            fun okay() {
                main()
            }
            
            class SomeUseCase {
              fun execute() {
              
              }
            }
        """.trimIndent()
    val file = PsiFileFactory.getInstance(environment.project)
        .createFileFromText("MyFile.kt", KotlinLanguage.INSTANCE, code)
        ?.let(::convertPsiFileToKtFile)
        ?: error("[IVY] File is null! Failed to parse file!")

    traversePsiTree(file)
}

fun traversePsiTree(ktFile: KtFile) {
    ktFile.accept(object : KtTreeVisitorVoid() {
        override fun visitNamedFunction(function: KtNamedFunction) {
            // Process the function
            super.visitNamedFunction(function)
            println("Function: ${function.name} (parent ${function.containingClassOrObject?.name})")
        }

        override fun visitFunctionType(type: KtFunctionType) {
            super.visitFunctionType(type)
        }

        override fun visitClass(klass: KtClass) {
            super.visitClass(klass)
            println("Class: ${klass.name}")
        }

        // Override other visit methods as needed
    })
}

fun convertPsiFileToKtFile(psiFile: PsiFile): KtFile? {
    return if (psiFile is KtFile) psiFile else null
}

fun extractFunctions(psiFile: PsiFile): List<KtNamedFunction> {
    // List to hold all found functions
    val functions = mutableListOf<KtNamedFunction>()

    // Recursively visit all children and sub-children of the file
    psiFile.acceptChildren(object : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            // If the element is a KtNamedFunction, add it to the list
            if (element is KtNamedFunction) {
                functions.add(element)
            }
            // Continue recursively visiting children
            super.visitElement(element)
        }
    })

    return functions
}