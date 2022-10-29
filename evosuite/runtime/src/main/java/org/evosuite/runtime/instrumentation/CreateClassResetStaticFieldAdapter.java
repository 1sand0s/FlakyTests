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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CreateClassResetStaticFieldAdapter extends MethodVisitor {

    private final List<StaticField> staticFields;

    private final String className;

    private final List<String> finalFields;

    private final String methodName;

    private Map<String, Object> staticFieldInitialValue;

    public CreateClassResetStaticFieldAdapter(MethodVisitor mv, String methodName, String className,
					      List<StaticField> staticFields, List<String> finalFields) {
        super(Opcodes.ASM9, mv);
        this.className = className;
        this.staticFields = staticFields;
        this.finalFields = finalFields;
	this.methodName = methodName;
	staticFieldInitialValue = new HashMap<String, Object>();
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible){
	System.out.println("Annotation : " + descriptor);
	return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor){
	super.visitFieldInsn(opcode, owner, name, descriptor);
	if(opcode == Opcodes.PUTSTATIC){
	    staticFieldInitialValue.put(owner.replace('/','.') + "." + name, null);
	    System.out.println("Opcode : " + opcode + "\nOwner : " + owner + "\nName : " + name + "\nDescriptor : " + descriptor + "\n\n");
	}
    }

    @Override
    public void visitCode() {
        super.visitCode();
	
	for(StaticField staticField : staticFields){/*
	    mv.visitFieldInsn(Opcodes.GETSTATIC, "org/evosuite/runtime/instrumentation/RuntimeInstrumentation", "staticFields", "Ljava/util/Map;");
	    mv.visitLdcInsn(staticField.name);
	    System.out.println("Instrumenting Field : " + staticField.toString());
	    mv.visitFieldInsn(Opcodes.GETSTATIC, staticField.className, staticField.name, staticField.desc);
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
	    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
	    mv.visitInsn(Opcodes.POP);*/
	}
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
