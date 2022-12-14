/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.Label; 
import org.objectweb.asm.Handle;
import java.util.Set;

public class PutStaticInstrumenterMethodAdapter extends MethodVisitor {

    private final Set<String> modifiedStaticFields;

    private final String className;

    private final String methodName;

    private boolean isAtomicUpdate;

    private String atomicFieldName;

    public PutStaticInstrumenterMethodAdapter(MethodVisitor mv, String methodName, String className,
					      Set<String> modifiedStaticFields) {
        super(Opcodes.ASM9, mv);
        this.className = className;
        this.modifiedStaticFields = modifiedStaticFields;
	this.methodName = methodName;
	isAtomicUpdate = false;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
	System.out.println("Annotation : " + descriptor);
	return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitInsn(int opcode){
	super.visitInsn(opcode);
	isAtomicUpdate = false;
    }

    @Override
    public void visitIntInsn(int opcode, int operand){
	super.visitIntInsn(opcode, operand);
	isAtomicUpdate = false;
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex){
	super.visitVarInsn(opcode, varIndex);
	isAtomicUpdate = false;
    }

    @Override
    public void visitTypeInsn(int opcode, String type){
	super.visitTypeInsn(opcode, type);
	isAtomicUpdate = false;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface){
	super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
	System.out.println(name + " " + descriptor);
	if(opcode == Opcodes. INVOKEVIRTUAL && isAtomicUpdate){
	    modifiedStaticFields.add(atomicFieldName);
	}
	isAtomicUpdate = false;
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments){
	super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
	isAtomicUpdate = false;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label){
	super.visitJumpInsn(opcode, label);
	isAtomicUpdate = false;
    }

    @Override
    public void visitLdcInsn(Object value){
	super.visitLdcInsn(value);
	isAtomicUpdate = false;
    }

    @Override
    public void visitIincInsn(int varIndex, int increment){
	super.visitIincInsn(varIndex, increment);
	isAtomicUpdate = false;
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels){
	super.visitTableSwitchInsn(min, max, dflt, labels);
	isAtomicUpdate = false;
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels){
	super.visitLookupSwitchInsn(dflt, keys, labels);
	isAtomicUpdate = false;
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions){
	super.visitMultiANewArrayInsn(descriptor, numDimensions);
	isAtomicUpdate = false;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor){
	super.visitFieldInsn(opcode, owner, name, descriptor);
	switch(opcode){
	case Opcodes.PUTSTATIC :
	    modifiedStaticFields.add(name);
	    break;
	case Opcodes.GETSTATIC :
	    atomicFieldName = name;
	    isAtomicUpdate = true;
	    break;
	}       
    }

    @Override
    public void visitCode() {
        super.visitCode();
		/*	for (StaticField staticField : staticFields) {
	    
            if (!finalFields.contains(staticField.name)
		&& !staticField.name.startsWith("__cobertura")
		&& !staticField.name.startsWith("$jacoco")
		&& !staticField.name.startsWith("$VRc") // Old Emma
		&& !staticField.name.startsWith("$gzoltar")
		) {
		
                if (staticField.value != null) {
                    mv.visitLdcInsn(staticField.value);
                } else {
                    Type type = Type.getType(staticField.desc);
                    switch (type.getSort()) {
		    case Type.BOOLEAN:
		    case Type.BYTE:
		    case Type.CHAR:
		    case Type.SHORT:
		    case Type.INT:
			mv.visitInsn(Opcodes.ICONST_0);
			break;
		    case Type.FLOAT:
			mv.visitInsn(Opcodes.FCONST_0);
			break;
		    case Type.LONG:
			mv.visitInsn(Opcodes.LCONST_0);
			break;
		    case Type.DOUBLE:
			mv.visitInsn(Opcodes.DCONST_0);
			break;
                        case Type.ARRAY:
		    case Type.OBJECT:
			mv.visitInsn(Opcodes.ACONST_NULL);
			break;
                    }
                }
                mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
				  staticField.name, staticField.desc);
            }
	    }*/
    }
}
