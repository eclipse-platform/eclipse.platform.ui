/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.tests;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test suite that contains the same tests as a regular {@link TestSuite},
 * but the order of tests is the order as declared in the classfile's bytecode.
 * 
 * <p>
 * <b>Background:</b> {@link java.lang.Class#getDeclaredMethods()} does not
 * specify the order of the methods. Up to JavaSE 6, the methods were usually
 * sorted in declaration order, but in JavaSE 7, the order is random. This class
 * guarantees reliable test execution order.
 * </p>
 * 
 * @since 3.9
 */
public class BytecodeOrderedTestSuite extends TestSuite {
	
	/**
	 * Creates a new test suite that runs tests in bytecode declaration order.
	 * 
	 * @param testClass the JUnit-3-style test class 
	 */
	public BytecodeOrderedTestSuite(Class testClass) {
		this(testClass, testClass.getName());
	}
	
	/**
	 * Creates a new test suite that runs tests in bytecode declaration order.
	 * 
	 * @param testClass the JUnit-3-style test class 
	 * @param name the name of the suite
	 */
	public BytecodeOrderedTestSuite(Class testClass, String name) {
    	super(name);
    	
    	TestSuite randomOrderSuite= new TestSuite(testClass);
    	ArrayList tests= Collections.list(randomOrderSuite.tests());
    	
    	class SortingException extends RuntimeException {
			private static final long serialVersionUID= 1L;
			public SortingException(String message) {
    			super(message);
    		}
    	}
    	final ArrayList orderedMethodNames= new ArrayList();
    	Class c= testClass;
    	try {
    		while (Test.class.isAssignableFrom(c)) {
    			addDeclaredTestMethodNames(c, orderedMethodNames);
    			c= c.getSuperclass();
    		}
    		Collections.sort(tests, new Comparator() {
				public int compare(Object o1, Object o2) {
					if (o1 instanceof TestCase && o2 instanceof TestCase) {
						TestCase t1= (TestCase) o1;
						TestCase t2= (TestCase) o2;
						int i1= orderedMethodNames.indexOf(t1.getName());
						int i2= orderedMethodNames.indexOf(t2.getName());
						if (i1 != -1 && i2 != -1)
							return i1 - i2;
					}
					throw new SortingException("suite failed to detect test order: " + o1 + ", " + o2);
				}
			});
    	} catch (SortingException e) {
    		addTest(error(testClass, "suite failed to detect test order", e)); //$NON-NLS-1$
    	} catch (IOException e) {
    		addTest(error(testClass, "suite failed to detect test order", e)); //$NON-NLS-1$
    	}
    	
    	for (Iterator iter= tests.iterator(); iter.hasNext(); ) {
			Test test= (Test) iter.next();
			addTest(test);
		}
	}

	private static Test error(Class testClass, String testMethod, final Exception exception) {
		return new TestCase(testMethod + "(" + testClass.getName() + ")") { //$NON-NLS-1$ //$NON-NLS-2$
			protected void runTest() throws Throwable {
				throw exception;
			}
		};
	}
	
    private void addDeclaredTestMethodNames(Class c, ArrayList methodNames) throws IOException {
    	/*
    	 * XXX: This method needs to be updated if new constant pool tags are specified.
    	 * Current supported major class file version: 51 (Java 1.7).
    	 * 
    	 * See JVMS 7, 4.4 The Constant Pool.
    	 */
        String className= c.getName();
        int lastDot= className.lastIndexOf(".");
		if (lastDot != -1)
        	className= className.substring(lastDot + 1);
		DataInputStream is= new DataInputStream(new BufferedInputStream(c.getResourceAsStream(className + ".class")));
        int magic= is.readInt();
        if (magic != 0xcafebabe)
            throw new IOException("bad magic bytes: 0x" + Integer.toHexString(magic));
        int minor= is.readUnsignedShort();
        int major= is.readUnsignedShort();
        int cpCount= is.readUnsignedShort();
        String[] constantPoolStrings= new String[cpCount];
        for (int i= 1; i < cpCount; i++) {

            byte tag= is.readByte();
            switch (tag) {
                case 7: // CONSTANT_Class
                    skip(is, 2);
                    break;
                case 9: // CONSTANT_Fieldref
                case 10: // CONSTANT_Methodref
                case 11: // CONSTANT_InterfaceMethodref
                    skip(is, 4);
                    break;
                case 8: // CONSTANT_String
                    skip(is, 2);
                    break;
                case 3: // CONSTANT_Integer
                case 4: // CONSTANT_Float
                    skip(is, 4);
                    break;
                case 5: // CONSTANT_Long
                case 6: // CONSTANT_Double
                    skip(is, 8);
                    i++; // weird spec wants this
                    break;
                case 12: // CONSTANT_NameAndType
                    skip(is, 4);
                    break;
                case 1: // CONSTANT_Utf8
                    constantPoolStrings[i]= is.readUTF();
                    break;
                case 15: // CONSTANT_MethodHandle
                    skip(is, 3);
                    break;
                case 16: // CONSTANT_MethodType
                    skip(is, 2);
                    break;
                case 18: // CONSTANT_InvokeDynamic
                    skip(is, 4);
                    break;
                default:
                	throw new IOException("unknown constant pool tag " + tag + " at index " + i 
                			+ ". Class file version: " + major + "." + minor);
            }
        }
        skip(is, 2 * 3); // access_flags, this_class, super_class
        int interfacesCount= is.readUnsignedShort();
        skip(is, 2 * interfacesCount);
        int fieldsCount= is.readUnsignedShort();
        for (int i= 0; i < fieldsCount; i++) {
            skip(is, 2 * 3); // access_flags, name_index, descriptor_index
            int attributesCount= is.readUnsignedShort();
            for (int j= 0; j < attributesCount; j++) {
                skip(is, 2); // attribute_name_index
                long attInfoCount= readUnsignedInt(is);
                skip(is, attInfoCount);
            }
        }

        int methodsCount= is.readUnsignedShort();
        for (int i= 0; i < methodsCount; i++) {
            skip(is, 2); // access_flags
            int nameIndex= is.readUnsignedShort();
            int descIndex= is.readUnsignedShort();
            if ("()V".equals(constantPoolStrings[descIndex])) {
				String name= constantPoolStrings[nameIndex];
				if (name.startsWith("test"))
					methodNames.add(name);
			}
            int attributesCount= is.readUnsignedShort();
            for (int j= 0; j < attributesCount; j++) {
                skip(is, 2); // attribute_name_index
                long attInfoCount= readUnsignedInt(is);
                skip(is, attInfoCount);
            }
        }
    }
    
	private static void skip(DataInputStream is, long bytes) throws IOException {
		while (bytes > 0)
			bytes -= is.skip(bytes);
		if (bytes != 0)
			throw new IOException("error in skipping bytes: " + bytes);
	}

	private static long readUnsignedInt(DataInputStream is) throws IOException {
		return is.readInt() & 0xFFFFffffL;
	}

}
