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
 * Tests for the basic context functionality
 */
public class ContextDynamicTest extends TestCase {

	public ContextDynamicTest() {
		super();
	}

	public ContextDynamicTest(String name) {
		super(name);
	}

	/**
	 * Tests objects being added and removed from the context
	 */
	public synchronized void testAddRemove() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		String testStringViaMethod = new String("abcd");
		Object testObjectViaMethod = new Object();

		// create original context
		IEclipseContext context = EclipseContextFactory.create();
		context.set("Integer", testInt);
		context.set("StringViaMethod", testStringViaMethod);

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check basic injection
		assertEquals(testInt, userObject.getInteger());
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(1, userObject.setStringCalled);
		assertNull(userObject.getString());
		assertNull(userObject.getObjectViaMethod());
		assertEquals(0, userObject.setObjectCalled);

		// add service
		context.set("string", testString); // this checks capitalization as well
		context.set("objectViaMethod", testObjectViaMethod); // this checks capitalization as well

		// and check
		assertEquals(testString, userObject.getString());
		assertEquals(testInt, userObject.getInteger());
		assertEquals(1, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());

		// remove service
		context.remove("Integer");
		context.remove("StringViaMethod");

		// and check
		assertNull(userObject.getInteger());
		assertEquals(testString, userObject.getString());
		assertEquals(2, userObject.setStringCalled); // XXX same setter method called on remove
		assertEquals(1, userObject.setObjectCalled);
		assertNull(userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());
	}

	/**
	 * Tests objects being added and removed from the context
	 */
	public synchronized void testParentAddRemove() {
		Integer testInt = new Integer(123);
		String testString = new String("abc");
		String testStringViaMethod = new String("abcd");
		Object testObjectViaMethod = new Object();

		// create original context
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("Integer", testInt);
		parentContext.set("StringViaMethod", testStringViaMethod);

		IEclipseContext context = EclipseContextFactory.create(parentContext, null); 

		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check basic injection
		assertEquals(testInt, userObject.getInteger());
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(1, userObject.setStringCalled);
		assertNull(userObject.getString());
		assertNull(userObject.getObjectViaMethod());
		assertEquals(0, userObject.setObjectCalled);

		// add service
		parentContext.set("string", testString); // this checks capitalization as well
		parentContext.set("objectViaMethod", testObjectViaMethod); // this checks capitalization as well

		// and check
		assertEquals(testString, userObject.getString());
		assertEquals(testInt, userObject.getInteger());
		assertEquals(1, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);
		assertEquals(testStringViaMethod, userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());

		// remove service
		parentContext.remove("Integer");
		parentContext.remove("StringViaMethod");

		// add check
		assertNull(userObject.getInteger());
		assertEquals(testString, userObject.getString());
		// 2: the same setter method is called on set and remove
		assertEquals(2, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);
		assertNull(userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());
	}

	/**
	 * Tests context being disposed with objects implementing IContextAware
	 */
	public synchronized void testContextAware() {
		// create context
		IEclipseContext context = EclipseContextFactory.create();
		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check post processing
		assertTrue(userObject.isFinalized());
	}

	public static Test suite() {
		return new TestSuite(ContextDynamicTest.class);
	}

}
