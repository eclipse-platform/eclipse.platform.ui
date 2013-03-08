/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.tests.contexts.inject.ObjectBasic;

/**
 * Tests for the basic context functionality
 */
public class ContextDynamicTest extends TestCase {

	public ContextDynamicTest() {
		super();
	}

	public ContextDynamicTest(String name) {
		super(name);
	}

	public void testReplaceFunctionWithStaticValue() {
		IEclipseContext parent = EclipseContextFactory.create();
		IEclipseContext context = parent.createChild();
		assertNull(context.getLocal("bar"));
		context.set("bar", "baz1");
		context.set("bar", new ContextFunction() {
			public Object compute(IEclipseContext context, String contextKey) {
				return "baz1";
			}
		});
		parent.set("bar", "baz2");
		assertEquals("baz1", context.get("bar"));
		context.set("bar", "baz3");
		assertEquals("baz3", context.get("bar"));
	}

	/**
	 * Tests objects being added and removed from the context
	 */
	public synchronized void testAddRemove() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Double testDouble = new Double(1.23);
		Float testFloat = new Float(12.3);
		Character testChar = new Character('v');

		// create original context
		IEclipseContext context = EclipseContextFactory.create();
		context.set(Integer.class.getName(), testInt);
		context.set(String.class.getName(), testString);
		context.set(Double.class.getName(), testDouble);
		context.set(Float.class.getName(), testFloat);
		context.set(Character.class.getName(), testChar);

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check basic injection
		assertEquals(testString, userObject.injectedString);
		assertEquals(testInt, userObject.getInt());
		assertEquals(context, userObject.context);
		assertEquals(1, userObject.setMethodCalled);
		assertEquals(1, userObject.setMethodCalled2);
		assertEquals(testDouble, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertEquals(testChar, userObject.c);

		// change value
		Double testDouble2 = new Double(3.45);
		Integer testInt2 = new Integer(123);
		context.set(Double.class.getName(), testDouble2);
		context.set(Integer.class.getName(), testInt2);

		// and check
		assertEquals(testString, userObject.injectedString);
		assertEquals(testInt2, userObject.getInt());
		assertEquals(context, userObject.context);
		assertEquals(2, userObject.setMethodCalled);
		assertEquals(1, userObject.setMethodCalled2);
		assertEquals(testDouble2, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertEquals(testChar, userObject.c);

		// remove element
		context.remove(String.class.getName());
		context.remove(Character.class.getName());

		// and check
		assertNull(userObject.injectedString);
		assertEquals(testInt2, userObject.getInt());
		assertEquals(context, userObject.context);
		assertEquals(2, userObject.setMethodCalled);
		assertEquals(2, userObject.setMethodCalled2);
		assertEquals(testDouble2, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertNull(userObject.c);
	}

	/**
	 * Tests objects being added and removed from the context
	 */
	public synchronized void testParentAddRemove() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Double testDouble = new Double(1.23);
		Float testFloat = new Float(12.3);
		Character testChar = new Character('v');

		// create original context
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set(Integer.class.getName(), testInt);
		parentContext.set(String.class.getName(), testString);
		parentContext.set(Double.class.getName(), testDouble);
		parentContext.set(Float.class.getName(), testFloat);
		parentContext.set(Character.class.getName(), testChar);
		IEclipseContext context = parentContext.createChild();

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check basic injection
		assertEquals(testString, userObject.injectedString);
		assertEquals(testInt, userObject.getInt());
		assertEquals(context, userObject.context);
		assertEquals(1, userObject.setMethodCalled);
		assertEquals(1, userObject.setMethodCalled2);
		assertEquals(testDouble, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertEquals(testChar, userObject.c);

		// change value
		Double testDouble2 = new Double(3.45);
		Integer testInt2 = new Integer(123);
		context.set(Double.class.getName(), testDouble2);
		context.set(Integer.class.getName(), testInt2);

		// and check
		assertEquals(testString, userObject.injectedString);
		assertEquals(testInt2, userObject.getInt());
		assertEquals(context, userObject.context);
		assertEquals(2, userObject.setMethodCalled);
		assertEquals(1, userObject.setMethodCalled2);
		assertEquals(testDouble2, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertEquals(testChar, userObject.c);

		// remove element
		parentContext.remove(String.class.getName());
		parentContext.remove(Character.class.getName());

		// and check
		assertNull(userObject.injectedString);
		assertEquals(testInt2, userObject.getInt());
		assertEquals(context, userObject.context);
		assertEquals(2, userObject.setMethodCalled);
		assertEquals(2, userObject.setMethodCalled2);
		assertEquals(testDouble2, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertNull(userObject.c);
	}

	public static Test suite() {
		return new TestSuite(ContextDynamicTest.class);
	}

}
