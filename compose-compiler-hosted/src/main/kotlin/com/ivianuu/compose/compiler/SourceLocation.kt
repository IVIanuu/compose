/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose.compiler

import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class SourceLocationClassBuilderInterceptorExtension : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory = SourceLocationClassBuilderFactory(interceptedFactory)
}

private class SourceLocationClassBuilderFactory(
    private val delegateFactory: ClassBuilderFactory
) : ClassBuilderFactory {

    override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
        return SourceLocationClassBuilder(delegateFactory.newClassBuilder(origin))
    }

    override fun getClassBuilderMode() = delegateFactory.classBuilderMode

    override fun asText(builder: ClassBuilder?): String? {
        return delegateFactory.asText((builder as SourceLocationClassBuilder).delegateClassBuilder)
    }

    override fun asBytes(builder: ClassBuilder?): ByteArray? {
        return delegateFactory.asBytes((builder as SourceLocationClassBuilder).delegateClassBuilder)
    }

    override fun close() {
        delegateFactory.close()
    }
}


private class SourceLocationClassBuilder(val delegateClassBuilder: ClassBuilder) :
    DelegatingClassBuilder() {

    override fun getDelegate(): ClassBuilder = delegateClassBuilder

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)

        if (origin.descriptor == null) return original
        // do not replace with s inline functions
        if ((origin.descriptor as? FunctionDescriptor)?.isInline == true) return original

        var lineNumber = 0

        return object : MethodVisitor(Opcodes.ASM5, original) {
            override fun visitLineNumber(line: Int, start: Label?) {
                super.visitLineNumber(line, start)
                lineNumber = line
            }

            override fun visitMethodInsn(
                opcode: Int,
                owner: String?,
                name: String?,
                descriptor: String?,
                isInterface: Boolean
            ) {
                if (opcode == 184 && name == "sourceLocation") {
                    InstructionAdapter(this).apply {
                        aconst("${origin.descriptor!!.fqNameSafe}:$lineNumber")
                    }
                } else {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
            }
        }
    }
}