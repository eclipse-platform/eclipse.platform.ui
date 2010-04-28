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
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Inject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * Tests for the basic context injection functionality
 */
public class ContextInjectionTest extends TestCase {

	public static Test suite() {
		return new TestSuite(ContextInjectionTest.class);
	}

	public ContextInjectionTest() {
		super();
	}

	public ContextInjectionTest(String name) {
		super(name);
	}

	/**
	 * Test trivial method injection and finalize method with context as an argument
	 */
	public void testContextSetOneArg() {
		class TestData {
		}
		class Injected {
			int contextSetCalled = 0;
			int setMethodCalled = 0;

			public TestData value;

			@SuppressWarnings("unused")
			@Inject
			public void contextSet(IEclipseContext context) {
				contextSetCalled++;
			}

			@SuppressWarnings("unused")
			@Inject
			public void InjectedMethod(TestData arg) {
				setMethodCalled++;
				value = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set(TestData.class.getName(), methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		assertEquals(1, object.setMethodCalled);
		assertEquals(1, object.contextSetCalled);

		TestData methodValue2 = new TestData();
		context.set(TestData.class.getName(), methodValue2);
		assertEquals(2, object.setMethodCalled);
		assertEquals(methodValue2, object.value);
		assertEquals(1, object.contextSetCalled);
	}

	/**
	 * Test filnalize method - no args
	 */
	public void testContextSetZeroArgs() {
		class TestData {
		}
		class Injected {
			int contextSetCalled = 0;
			int setMethodCalled = 0;

			public TestData value;

			@SuppressWarnings("unused")
			@Inject
			public void contextSet() {
				contextSetCalled++;
			}

			@SuppressWarnings("unused")
			@Inject
			public void InjectedMethod(TestData arg) {
				setMethodCalled++;
				value = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set(TestData.class.getName(), methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		assertEquals(1, object.setMethodCalled);
		assertEquals(1, object.contextSetCalled);

		TestData methodValue2 = new TestData();
		context.set(TestData.class.getName(), methodValue2);
		assertEquals(2, object.setMethodCalled);
		assertEquals(methodValue2, object.value);
		assertEquals(1, object.contextSetCalled);
	}

	/**
	 * Tests basic context injection
	 */
	public synchronized void testInjection() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Double testDouble = new Double(1.23);
		Float testFloat = new Float(12.3);
		Character testChar = new Character('v');

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		context.set(Integer.class.getName(), testInt);
		context.set(String.class.getName(), testString);
		context.set(Double.class.getName(), testDouble);
		context.set(Float.class.getName(), testFloat);
		context.set(Character.class.getName(), testChar);

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check field injection
		assertEquals(testString, userObject.injectedString);
		assertEquals(testInt, userObject.getInt());
		// assertEquals(context, userObject.context);

		// check method injection
		assertEquals(1, userObject.setMethodCalled);
		assertEquals(1, userObject.setMethodCalled2);
		assertEquals(testDouble, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertEquals(testChar, userObject.c);

		// check post processing
		assertTrue(userObject.finalized);
	}

	/**
	 * Tests injection of objects from parent context
	 */
	public synchronized void testInjectionFromParent() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Double testDouble = new Double(1.23);
		Float testFloat = new Float(12.3);
		Character testChar = new Character('v');

		// create parent context
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set(Integer.class.getName(), testInt);
		parentContext.set(String.class.getName(), testString);

		// create child context
		IEclipseContext context = parentContext.createChild();
		context.set(Double.class.getName(), testDouble);
		context.set(Float.class.getName(), testFloat);
		context.set(Character.class.getName(), testChar);

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check field injection
		assertEquals(testString, userObject.injectedString);
		assertEquals(testInt, userObject.getInt());
		// assertEquals(context, userObject.context);

		// check method injection
		assertEquals(1, userObject.setMethodCalled);
		assertEquals(1, userObject.setMethodCalled2);
		assertEquals(testDouble, userObject.d);
		assertEquals(testFloat, userObject.f);
		assertEquals(testChar, userObject.c);

		// check post processing
		assertTrue(userObject.finalized);
	}

	/**
	 * Tests injection into classes with inheritance
	 */
	public synchronized void testInjectionAndInheritance() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Float testFloat = new Float(12.3);

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		context.set(Integer.class.getName(), testInt);
		context.set(String.class.getName(), testString);
		context.set(Float.class.getName(), testFloat);

		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);

		// check inherited portion
		assertEquals(testString, userObject.getString());
		// assertEquals(context, userObject.getContext());
		assertEquals(testString, userObject.getStringViaMethod());
		assertEquals(1, userObject.setStringCalled);

		// check declared portion
		assertEquals(testInt, userObject.getInteger());
		assertEquals(testFloat, userObject.getObjectViaMethod());
		assertEquals(1, userObject.setObjectCalled);

		// make sure overridden injected method was called only once
		assertEquals(1, userObject.setOverriddenCalled);

		// check post processing
		assertEquals(1, userObject.getFinalizedCalled());
	}

}
