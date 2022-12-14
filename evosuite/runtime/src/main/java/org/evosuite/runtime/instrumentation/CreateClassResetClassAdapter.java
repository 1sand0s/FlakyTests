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

import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.classhandling.ModifiedTargetStaticFields;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * This visitor duplicates static initializers (i.e. <clinit>) into a new method
 * __STATIC_RESET() method, such that we can explicitly restore the initial
 * state of the class.
 *
 * @author Gordon Fraser
 */
public class CreateClassResetClassAdapter extends ClassVisitor {

    /**
     * This flag indicates that final static fields should be transformed into
     * non-final static fields.
     */
    private final boolean removeFinalModifierOnStaticFields;

    /**
     * This flag indicates that any update on final static fields should be
     * removed.
     */
    private boolean removeUpdatesOnFinalFields = true;

    /**
     * Allows to define if the current transformation should remove any updated
     * on those static fields that are final fields. If the
     * <code>removeFinalModifierOnStaticFields</code> is active, then no update
     * is removed since all static final fields are transformed into non-final
     * fields.
     *
     * @param removeUpdatesOnFinalFields
     */
    public void setRemoveUpdatesOnFinalFields(boolean removeUpdatesOnFinalFields) {
        this.removeUpdatesOnFinalFields = removeUpdatesOnFinalFields;
    }

    /**
     * The current class name being visited
     */
    private final String className;

    /**
     * Constant <code>static_classes</code>
     */
    public static List<String> staticClasses = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(CreateClassResetClassAdapter.class);

    public static ArrayList<StaticField> staticFields = new ArrayList<>();

    public static ArrayList<Object> staticFieldValues = new ArrayList<>();

    public static int count1;

    /**
     * Indicates if the current class being visited is an interface
     */
    private boolean isInterface = false;

    /**
     * Indicates if the current class being visited is an anonymous classs
     */
    private boolean isAnonymous = false;

    /**
     * Indicates if the <clinit> method was already found during the visit of
     * this class
     */
    private boolean clinitFound = false;

    private boolean definesUid = false;
    private long serialUID = -1L;

    /**
     * Indicates if the __STATIC_RESET() method has been already added to this
     * class definition
     */
    private boolean resetMethodAdded = false;

    /**
     * The final fields of this class
     */
    private final List<String> finalFields = new ArrayList<>();

    /**
     * Indicates if the current class being visited is an enumeration
     */
    private boolean isEnum = false;

    private static final Pattern ANONYMOUS_MATCHER1 = Pattern.compile(".*\\$\\d+.*$");

    private Set<String> modifiedStaticFieldsPUTSTATIC = new HashSet<String>();

    /**
     * Creates a new <code>CreateClassResetClassAdapter</code> instance
     *
     * @param visitor
     * @param className                         the class name to be visited
     * @param removeFinalModifierOnStaticFields if this parameter is true, all final static fields are
     *                                          translated into non-final static fields
     */
    public CreateClassResetClassAdapter(ClassVisitor visitor, String className,
                                        boolean removeFinalModifierOnStaticFields) {
        super(Opcodes.ASM9, visitor);
        this.className = className;
	System.out.println("Instrumenting Class : " + className);
        this.removeFinalModifierOnStaticFields = removeFinalModifierOnStaticFields;
    }

    /**
     * Detects if the current class is an anonymous class
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
        if (ANONYMOUS_MATCHER1.matcher(name).matches()) {
            isAnonymous = true;
        }
        if (superName.equals(java.lang.Enum.class.getName().replace(".", "/"))) {
            isEnum = true;
        }
    }

    
    /**
     * The list of the static fields declared in the class being visited
     */
    private final List<StaticField> static_fields = new LinkedList<>();

    /**
     * This list saves the static fields whose <code>final</code> modifier was
     * removed in the target class
     */
    private final ArrayList<String> modifiedStaticFields = new ArrayList<>();

    /**
     * During the visit of each field, static fields are collected. If the
     * <code>removeFinalModifierOnStaticFields</code> is active, final static
     * fields are transformed into non-final static fields.
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

	FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        if (name.equals("serialVersionUID")) {
            definesUid = true;
            // We must not remove final from serialVersionUID or else the
            // class cannot be serialised and de-serialised any more
            return fv;
        }

        if (hasStaticModifier(access)) {
            StaticField staticField = new StaticField(className, name, desc, value);
	    System.out.println("Initial Value : " + staticField.toString() + " " + value);
            staticFields.add(staticField);
	    //staticFieldValues.add(null);
        }

	/*  if (!isEnum && !isInterface && removeFinalModifierOnStaticFields) {
            int newAccess = access & (~Opcodes.ACC_FINAL);
            if (newAccess != access) {
                // this means that the field was modified
                modifiedStaticFields.add(name);
            }
            return super.visitField(newAccess, name, desc, signature, value);
        } else {
            if (hasFinalModifier(access))
                finalFields.add(name);

            return super.visitField(access, name, desc, signature, value);
	    }*/
	return fv;
    }

    /**
     * Returns true iif the access modifiers has a final modifier
     *
     * @param access
     * @return
     */
    private boolean hasFinalModifier(int access) {
        return (access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL;
    }

    /**
     * Returns true iif the access modifiers has a static modifier
     *
     * @param access
     * @return
     */
    private boolean hasStaticModifier(int access) {
        return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int methodAccess, String methodName, String descriptor, String signature,
                                     String[] exceptions) {

        MethodVisitor mv = super.visitMethod(methodAccess, methodName, descriptor, signature, exceptions);
	if(!methodName.equals("<clinit>")){
	    PutStaticInstrumenterMethodAdapter staticResetStaticFieldAdapter = new PutStaticInstrumenterMethodAdapter(mv, methodName, className, modifiedStaticFieldsPUTSTATIC);
	    return staticResetStaticFieldAdapter;
	}
	else{
	    CreateClassResetStaticFieldAdapter staticResetStaticFieldAdapter = new CreateClassResetStaticFieldAdapter(mv, methodName, className);
	    return staticResetStaticFieldAdapter;
	}
	/*System.out.println("Method Name : " + methodName);
	if(methodName.startsWith("test") || methodName.equals("<clinit>")){
	    System.out.println("Method Name : " + methodName);
	    CreateClassResetStaticFieldAdapter staticResetStaticFieldAdapter = new CreateClassResetStaticFieldAdapter(mv, methodName, className, staticFields, finalFields);
	    
	    //return new MultiMethodVisitor(staticResetStaticFieldAdapter, mv);
	    return staticResetStaticFieldAdapter;
	    }*/
	/*
        if (methodName.equals("<clinit>") && !isInterface && !isAnonymous && !resetMethodAdded) {
            clinitFound = true;
            logger.info("Found static initializer in class " + className);
            // determineSerialisableUID();

            // duplicates existing <clinit>
            // TODO: Removed | Opcodes.ACC_PUBLIC
            // Does __STATIC_RESET need to be public?
            // <clinit> apparently can be private, resulting
            // in illegal modifiers
            MethodVisitor visitMethod = super.visitMethod(methodAccess | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    ClassResetter.STATIC_RESET, descriptor, signature, exceptions);

            CreateClassResetMethodAdapter staticResetMethodAdapter = new CreateClassResetMethodAdapter(visitMethod,
                    className, this.RuntimeInstrumentation.staticFields, finalFields);

            resetMethodAdded = true;

            if (this.removeUpdatesOnFinalFields) {
                MethodVisitor mv2 = new RemoveFinalMethodAdapter(className, staticResetMethodAdapter, finalFields);

                return new MultiMethodVisitor(mv2, mv);
            } else {
                return new MultiMethodVisitor(staticResetMethodAdapter, mv);
            }
        } else if (methodName.equals(ClassResetter.STATIC_RESET)) {
            if (resetMethodAdded) {
                // Do not add reset method a second time
            } else {
                resetMethodAdded = true;
            }
	    }*/
	//    return mv;
    }

    /**
     * After all the class code was visited, If no <clinit> was found, an empty
     * __STATIC_RESET() method is synthesized.
     */
    @Override
    public void visitEnd() {
        if (!clinitFound && !isInterface && !isAnonymous && !resetMethodAdded) {
            // create brand new __STATIC_RESET
            if (!definesUid) {
                // determineSerialisableUID();
                // createSerialisableUID();
            }
            createEmptyStaticReset();
        } else if (clinitFound) {
            if (!definesUid) {
                // createSerialisableUID();
            }
        }
        if (!modifiedStaticFields.isEmpty()) {
            ModifiedTargetStaticFields.getInstance().addFinalFields(modifiedStaticFields);
        }
        super.visitEnd();
    }

    @Deprecated
    private void determineSerialisableUID() {
        try {
            Class<?> clazz = Class.forName(className.replace('/', '.'), false,
                    MethodCallReplacementClassAdapter.class.getClassLoader());
            if (Serializable.class.isAssignableFrom(clazz)) {
                ObjectStreamClass c = ObjectStreamClass.lookup(clazz);
                serialUID = c.getSerialVersionUID();
            }
        } catch (ClassNotFoundException e) {
            logger.info("Failed to add serialId to class " + className + ": " + e.getMessage());
        }

    }

    @Deprecated
    // This method is a code clone from MethodCallReplacementClassAdapter
    private void createSerialisableUID() {
        // Only add this for serialisable classes
        if (serialUID < 0)
            return;
        /*
         * If the class is serializable, then adding a hashCode will change the
         * serialVersionUID if it is not defined in the class. Hence, if it is
         * not defined, we have to define it to avoid problems in serialising
         * the class.
         */
        logger.info("Adding serialId to class " + className);
        visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "serialVersionUID", "J", null,
                serialUID);
    }

    /**
     * Creates an empty __STATIC_RESET method where no <clinit> was found.
     */
    private void createEmptyStaticReset() {
        logger.info("Creating brand-new static initializer in class " + className);
	System.out.println("Creating brand-new static initializer in class " + className);
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                ClassResetter.STATIC_RESET, "()V", null, null);
        mv.visitCode();
	System.out.println("Num static fields : " + staticFields.size() + " " + modifiedStaticFieldsPUTSTATIC.size() + " " + staticFieldValues.size());
        for (int j = 0; j < staticFields.size(); j++) {
	    StaticField staticField = staticFields.get(j);
	    if(!modifiedStaticFieldsPUTSTATIC.contains(staticField.name))
		continue;
            if (!finalFields.contains(staticField.name) && !staticField.name.startsWith("__cobertura")
                    && !staticField.name.startsWith("$jacoco") && !staticField.name.startsWith("$VRc") // Old
                    // Emma
                    && !staticField.name.startsWith("$gzoltar")
            ) {

                logger.info("Adding bytecode for initializing field " + staticField.name);
		System.out.println("Value for : " + staticField.name + " is : " + count1 + " " + j);
		mv.visitFieldInsn(Opcodes.GETSTATIC, "org/evosuite/runtime/instrumentation/CreateClassResetClassAdapter", "staticFieldValues", "Ljava/util/ArrayList;");
		mv.visitIntInsn(Opcodes.BIPUSH, j);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "get", "(I)Ljava/lang/Object;");
		
		Type type = Type.getType(staticField.desc);
		switch (type.getSort()) {
		case Type.BOOLEAN:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
		    break;
		case Type.BYTE:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
		    break;
		case Type.CHAR:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "characterValue", "()C");
		    break;
		case Type.SHORT:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
		case Type.INT:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
		    break;
		case Type.FLOAT:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
		    break;
		case Type.LONG:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
		    break;
		case Type.DOUBLE:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
		    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
		    break;
		case Type.ARRAY:
		case Type.OBJECT:
		    mv.visitTypeInsn(Opcodes.CHECKCAST, staticField.desc.substring(1,staticField.desc.length()-1));
		    break;
		}
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className, staticField.name, staticField.desc);
	    }
	}
	mv.visitInsn(Opcodes.RETURN);
	mv.visitMaxs(0, 0);
	mv.visitEnd();
    }
}
