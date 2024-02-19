package org.jetbrains.kotlin.cli.klib

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.DeclarationDescriptorVisitorEmptyBodies
import org.jetbrains.kotlin.utils.addIfNotNull

internal class SignaturePrinter(
        private val output: Appendable,
        private val signatureRenderer: KlibSignatureRenderer
) {
    fun print(module: ModuleDescriptor) {
        val collector = SignatureCollector()
        module.accept(collector, Unit)

        for (signature in collector.signatures.sorted()) {
            output.appendLine(signature)
        }
    }

    private inner class SignatureCollector : DeclarationDescriptorVisitorEmptyBodies<Unit, Unit>() {
        val signatures: MutableSet<String> = hashSetOf()

        override fun visitModuleDeclaration(descriptor: ModuleDescriptor, data: Unit) {
            descriptor.getPackageFragments().forEach { it.accept(this, data) }
        }

        override fun visitPackageFragmentDescriptor(descriptor: PackageFragmentDescriptor, data: Unit) {
            descriptor.getMemberScope().getContributedDescriptors().forEach { it.accept(this, data) }
        }

        override fun visitClassDescriptor(descriptor: ClassDescriptor, data: Unit) {
            extractSignatureFromDeclaration(descriptor) {
                descriptor.constructors.forEach { it.accept(this, data) }
                descriptor.unsubstitutedMemberScope.getContributedDescriptors().forEach { it.accept(this, data) }
            }
        }

        override fun visitFunctionDescriptor(descriptor: FunctionDescriptor, data: Unit) {
            extractSignatureFromDeclaration(descriptor)
        }

        override fun visitPropertyDescriptor(descriptor: PropertyDescriptor, data: Unit) {
            extractSignatureFromDeclaration(descriptor) {
                descriptor.getter?.let(::extractSignatureFromDeclaration)
                descriptor.setter?.let(::extractSignatureFromDeclaration)
            }
        }

        override fun visitConstructorDescriptor(descriptor: ConstructorDescriptor, data: Unit) {
            extractSignatureFromDeclaration(descriptor)
        }

        override fun visitTypeAliasDescriptor(descriptor: TypeAliasDescriptor, data: Unit) {
            extractSignatureFromDeclaration(descriptor)
        }

        private inline fun extractSignatureFromDeclaration(descriptor: DeclarationDescriptorWithVisibility, continuation: () -> Unit = {}) {
            signatures.addIfNotNull(signatureRenderer.render(descriptor))
            continuation()
        }
    }
}
