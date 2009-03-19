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
package org.eclipse.e4.core.services.internal.context;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;

/**
 * Tests for the basic context injection functionality
 */
public class ContextInjectionTest extends TestCase {

	public ContextInjectionTest() {
		super();
	}

	public ContextInjectionTest(String name) {
		super(name);
	}

	/**
	 * Tests basic context injection
	 */
	public synchronized void testInjection() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Boolean testBoolean = new Boolean(true);
		String testStringViaMethod = new String("abcd");
		Object testObjectViaMethod = new Object();

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		// elements to be populated via fields
		context.set("Integer", testInt);
		context.set("string", testString); // this checks capitalization as well
		context.set("Boolean", testBoolean);
		// elements to be populated via methods
		context.set("StringViaMethod", testStringViaMethod);
		context.set("objectViaMethod", testObjectViaMethod); // this checks capitalization as well

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check field injection
		assertEquals(testString, userObject.getString());
		assertEquals(testInt, userObject.getInteger());
		assertEquals(context, userObject.getContext());

		// check method injection
		assertEquals(1, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());

		// check post processing
		assertTrue(userObject.isFinalized());
	}
	
	public void testOptionalInjection() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		Boolean testBoolean = new Boolean(true);
		String testStringViaMethod = new String("abcd");
		Object testObjectViaMethod = new Object();

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		// elements to be populated via fields
		context.set("Integer", testInt);
		context.set("string", testString); // this checks capitalization as well
		context.set("Boolean", testBoolean);
		// elements to be populated via methods
		context.set("StringViaMethod", testStringViaMethod);
		context.set("objectViaMethod", testObjectViaMethod); // this checks capitalization as well

		ObjectWithAnnotations userObject = new ObjectWithAnnotations();
		ContextInjectionFactory.inject(userObject, context);

		// check field injection
		assertEquals(testString, userObject.getString());
		assertEquals(testInt, userObject.getInteger());
		assertEquals(context, userObject.getContext());

		// check method injection
		assertEquals(1, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());

		// check that no extra injections are done
		assertNull(userObject.diMissing);
		assertNull(userObject.myMissing);
		assertEquals(0, userObject.setMissingCalled);

		// check incompatible types
		assertNull(userObject.diBoolean);
		assertNull(userObject.myBoolean);
		assertEquals(0, userObject.setBooleanCalled);

		// check incompatible types
		assertNull(userObject.diBoolean);
		assertNull(userObject.myBoolean);
		assertEquals(0, userObject.setBooleanCalled);

		// check post processing
		assertTrue(userObject.isFinalized());
	}

	/**
	 * Tests injection into classes with inheritance
	 */
	public synchronized void testInjectionAndInheritance() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		String testStringViaMethod = new String("abcd");
		Object testObjectViaMethod = new Object();

		// create context
		IEclipseContext context = EclipseContextFactory.create();
		// elements to be populated via fields
		context.set("Integer", testInt);
		context.set("string", testString); // this checks capitalization as well
		// elements to be populated via methods
		context.set("StringViaMethod", testStringViaMethod);
		context.set("objectViaMethod", testObjectViaMethod); // this checks capitalization as well

		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);

		// check inherited portion
		assertEquals(testString, userObject.getString());
		assertEquals(context, userObject.getContext());
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(1, userObject.setStringCalled);

		// check declared portion
		assertEquals(testInt, userObject.getInteger());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());
		assertEquals(1, userObject.setObjectCalled);

		// check post processing
		assertEquals(1, userObject.getFinalizedCalled());
	}

	/**
	 * Tests injection of objects from parent context
	 */
	public synchronized void testInjectionFromParent() {
		Integer testInt = new Integer(123);
		String testStringViaMethod = new String("abcd");
		Object testObjectViaMethod = new Object();

		// create parent context
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("Integer", testInt);
		parentContext.set("StringViaMethod", testStringViaMethod);

		// create child context
		IEclipseContext context = EclipseContextFactory.create(parentContext, null);
		context.set("objectViaMethod", testObjectViaMethod); // this checks capitalization as well
		context.set("string", "foo"); // not important for this test

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		assertEquals(testInt, userObject.getInteger());
		assertEquals(context, userObject.getContext());

		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());
		assertEquals(1, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);

		// check post processing
		assertTrue(userObject.isFinalized());
	}

	public static Test suite() {
		return new TestSuite(ContextInjectionTest.class);
	}

}
