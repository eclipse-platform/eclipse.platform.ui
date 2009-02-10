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
		IEclipseContext context = EclipseContextFactory.create(null);
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

		// dispose context
// TBD we currently don't have dispose functionality
//		context.dispose();
//		assertNull(userObject.getContext());
//		assertTrue(userObject.isDisposed());
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
		IEclipseContext parentContext = EclipseContextFactory.create(null);
		parentContext.set("Integer", testInt);
		parentContext.set("StringViaMethod", testStringViaMethod);

		IEclipseContext context = EclipseContextFactory.create(null, parentContext, null); 

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
		assertEquals(1, userObject.setStringCalled);
		assertEquals(1, userObject.setObjectCalled);
		assertNull(userObject.getStringViaMethod());
		assertEquals(testObjectViaMethod, userObject.getObjectViaMethod());
	}

	/**
	 * Tests context being disposed with objects implementing IContextAware
	 */
	public synchronized void testContextAware() {
		// create context
		IEclipseContext context = EclipseContextFactory.create(null);
		ObjectBasic userObject = new ObjectBasic();
		ContextInjectionFactory.inject(userObject, context);

		// check post processing
		assertTrue(userObject.isFinalized());

		// check disposal
		assertFalse(userObject.isDisposed());
		// TBD we currently don't have dispose functionality
//		context.dispose();
//		assertTrue(userObject.isDisposed());
	}

	/**
	 * Tests context being disposed using reflection
	 */
//	public synchronized void testDisposal() {
//		// create context
//		IEclipseContext context = EclipseContextFactory.create(null);
//		ObjectContextAware userObject = new ObjectContextAware();
//		ContextInjectionFactory.inject(userObject, context);
//
//		// check post processing
//		assertTrue(userObject.isFinalized());
//		assertFalse(userObject.isDisposed());
//		assertEquals(context, userObject.getEquinoxContext());

		// check disposal
		// TBD we currently don't have dispose functionality
//		context.dispose();
//
//		assertTrue(userObject.isDisposed());
//		assertTrue(userObject.isFinalized());
//		assertNull(userObject.getEquinoxContext());
//	}

	/**
	 * Tests parent context being disposed
	 */
//	public synchronized void testDisposalParent() {
		// create context
		// TBD we currently don't have dispose functionality
//		IEclipseContext parentContext = EclipseContextFactory.create(null);
//		IEclipseContext context1 = parentContext.newChild(null);
//		IEclipseContext context11 = context1.newChild(null);
//		IEclipseContext context2 = parentContext.newChild(null);
//
//		ObjectBasic userObject1 = new ObjectBasic();
//		ContextInjectionFactory.inject(userObject1, context1);
//
//		ObjectBasic userObject11 = new ObjectBasic();
//		ContextInjectionFactory.inject(userObject11, context11);
//
//		ObjectBasic userObject2 = new ObjectBasic();
//		ContextInjectionFactory.inject(userObject2, context2);
//
//		// check post processing
//		assertTrue(userObject1.isFinalized());
//		assertTrue(userObject11.isFinalized());
//		assertTrue(userObject2.isFinalized());
//
//		assertFalse(userObject1.isDisposed());
//		assertFalse(userObject11.isDisposed());
//		assertFalse(userObject2.isDisposed());
//
//		// check disposal
//		parentContext.dispose();
//		assertTrue(userObject1.isDisposed());
//		assertTrue(userObject11.isDisposed());
//		assertTrue(userObject2.isDisposed());
//	}

	public static Test suite() {
		return new TestSuite(ContextDynamicTest.class);
	}

}
