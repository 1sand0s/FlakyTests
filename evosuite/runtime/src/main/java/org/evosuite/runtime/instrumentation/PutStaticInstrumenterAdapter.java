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
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author 1sand0s
 */
public class PutStaticInstrumenterAdapter extends ClassVisitor {

    private final String className;

    Set<String> modifiedStaticFields = new HashSet<String>();
    
    /**
     * Creates a new <code>PutStaticInstrumenterAdapter</code> instance
     *
     * @param visitor
     * @param className                         the class name to be visited
     */
    public PutStaticInstrumenterAdapter(ClassVisitor visitor, String className) {
        super(Opcodes.ASM9, visitor);
        this.className = className;
    }

    @Override
    public void visitEnd(){
	System.out.println("Modified Static Fields");
	for(String str : modifiedStaticFields){
	    System.out.println(str);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodVisitor visitMethod(int methodAccess, String methodName, String descriptor, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = super.visitMethod(methodAccess, methodName, descriptor, signature, exceptions);
	if(!methodName.equals("<clinit>")){
	    PutStaticInstrumenterMethodAdapter staticResetStaticFieldAdapter = new PutStaticInstrumenterMethodAdapter(mv, methodName, className, modifiedStaticFields);
	    return staticResetStaticFieldAdapter;
	}
	return mv;
    }
}
