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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class CreateClassResetStaticFieldAdapter extends MethodVisitor {

    private final String className;

    private final String methodName;

    private MethodVisitor mv;

    private static ArrayList<AbstractInsnNode> opStackInsnTracker = new ArrayList<>();

    public CreateClassResetStaticFieldAdapter(MethodVisitor mv, String methodName, String className) {
        super(Opcodes.ASM9, mv);
        this.className = className;
	this.methodName = methodName;
	this.mv = mv;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor){
	super.visitFieldInsn(opcode, owner, name, descriptor);
	if(opcode == Opcodes.PUTSTATIC){
	    for(int k = 0; k < opStackInsnTracker.size(); k++)
		System.out.println(opStackInsnTracker.get(k));
	    for(int j = 0; j < CreateClassResetClassAdapter.staticFields.size(); j++){
		if(CreateClassResetClassAdapter.staticFields.get(j).name.equals(name) &&
		   CreateClassResetClassAdapter.staticFields.get(j).desc.equals(descriptor)){
		    int fieldType = Type.getType(CreateClassResetClassAdapter.staticFields.get(j).desc).getSort();

		    if(isPrimitive(fieldType)){
			mv.visitFieldInsn(Opcodes.GETSTATIC, CreateClassResetClassAdapter.staticFields.get(j).className, CreateClassResetClassAdapter.staticFields.get(j).name, CreateClassResetClassAdapter.staticFields.get(j).desc);
			mv.visitInsn(Opcodes.DUP);
			
			switch(Type.getType(CreateClassResetClassAdapter.staticFields.get(j).desc).getSort()){
		        case Type.BOOLEAN:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
			    break;
		        case Type.BYTE:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
			    break;
		        case Type.SHORT:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
			    break;
		        case Type.INT:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
			    break;
		        case Type.LONG:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
			    break;
		        case Type.FLOAT:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
			    break;
		        case Type.DOUBLE:
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
			    break;
			}
		    }
		    else{
			int index = 0;
			mv.visitTypeInsn(Opcodes.NEW, ((TypeInsnNode)opStackInsnTracker.get(index++)).desc);
			mv.visitInsn(Opcodes.DUP);
			index++;
			if(opStackInsnTracker.size() == 4){
			    if(opStackInsnTracker.get(index) instanceof InsnNode){
				mv.visitInsn(((InsnNode)opStackInsnTracker.get(index)).getOpcode());
			    }
			    else if(opStackInsnTracker.get(index) instanceof IntInsnNode){
				mv.visitIntInsn(((IntInsnNode)opStackInsnTracker.get(index)).getOpcode(),
						((IntInsnNode)opStackInsnTracker.get(index)).operand);
			    }
			    else{
				mv.visitLdcInsn(((LdcInsnNode)opStackInsnTracker.get(index)).cst);
			    }
			    index++;
			}
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					   ((MethodInsnNode)opStackInsnTracker.get(index)).owner,
					   ((MethodInsnNode)opStackInsnTracker.get(index)).name,
					   ((MethodInsnNode)opStackInsnTracker.get(index)).desc,
					   ((MethodInsnNode)opStackInsnTracker.get(index)).itf);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "org/evosuite/runtime/instrumentation/CreateClassResetClassAdapter", "staticFieldValues", "Ljava/util/ArrayList;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitIntInsn(Opcodes.BIPUSH, j);
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(ILjava/lang/Object;)V");
		    }
		}
	    }
	}
	opStackInsnTracker.clear();
    }

    @Override
    public void visitTypeInsn(int opcode, String type){
	super.visitTypeInsn(opcode, type);
	opStackInsnTracker.add(new TypeInsnNode(opcode, type));
    }
    
    @Override
    public void visitInsn(int opcode){
	super.visitInsn(opcode);
	opStackInsnTracker.add(new InsnNode(opcode));
    }

    @Override
    public void visitIntInsn(int opcode, int operand){
	super.visitIntInsn(opcode, operand);
	opStackInsnTracker.add(new IntInsnNode(opcode, operand));
    }

    @Override
    public void visitLdcInsn(Object operand){
	super.visitLdcInsn(operand);
	opStackInsnTracker.add(new LdcInsnNode(operand));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface){
	super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
	opStackInsnTracker.add(new MethodInsnNode(opcode, owner, name, descriptor, isInterface));
    }
    
    @Override
    public void visitCode() {
        super.visitCode();
    }

    static boolean isPrimitive(int type){
	switch(type){
	case Type.BOOLEAN:
	case Type.BYTE:
	case Type.SHORT:
	case Type.INT:
	case Type.LONG:
	case Type.FLOAT:
	case Type.DOUBLE:
	    return true;
	case Type.OBJECT:
	    return false;
	}
	return false;
    }

    void visitOpStackInsn(AbstractInsnNode insn){
    }
}
