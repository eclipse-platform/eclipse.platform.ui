/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

class OrderedTestSuite extends TestSuite{
	public OrderedTestSuite(Class theClass, String name) {
		this(theClass);
		setName(name);
	}

	public OrderedTestSuite(final Class theClass) {
		super();
		setName(theClass.getName());
		try {
			getTestConstructor(theClass); // Avoid generating multiple error messages
		} catch (NoSuchMethodException e) {
			addTest(warning("Class " + theClass.getName()
					+ " has no public constructor TestCase(String name) or TestCase()"));
			return;
		}
		if (!Modifier.isPublic(theClass.getModifiers())) {
			addTest(warning("Class " + theClass.getName() + " is not public"));
			return;
		}
		Class superClass = theClass;
		Vector names = new Vector();
		while (Test.class.isAssignableFrom(superClass)) {
			Method[] methods = superClass.getDeclaredMethods();
			int size = methods.length;
			for (int i = 0; i < size - 1; i++) {
				for (int j = i + 1; j < size; j++) {
					if (methods[i].getName().compareTo(methods[j].getName()) > 0) {
						Method m = methods[i];
						methods[i] = methods[j];
						methods[j] = m;
					}
				}
			}
			for (int i = 0; i < size; i++) {
				addTestMethod(methods[i], names, theClass);
			}
			superClass = superClass.getSuperclass();
		}
		if (!tests().hasMoreElements())
			addTest(warning("No tests found in " + theClass.getName()));
	}

	private void addTestMethod(Method m, Vector names, Class theClass) {
		String name = m.getName();
		if (names.contains(name))
			return;
		if (!isPublicTestMethod(m)) {
			if (isTestMethod(m))
				addTest(warning("Test method isn't public: " + m.getName()));
			return;
		}
		names.addElement(name);
		addTest(createTest(theClass, name));
	}

	private boolean isPublicTestMethod(Method m) {
		return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
	}

	private boolean isTestMethod(Method m) {
		String name = m.getName();
		Class[] parameters = m.getParameterTypes();
		Class returnType = m.getReturnType();
		return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
	}

}