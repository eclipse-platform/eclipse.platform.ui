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

import javax.inject.Inject;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;

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

	public void testContextSetOneArg() {
		class Injected {
			int contextSetCalled = 0;
			int setMethodCalled = 0;

			@SuppressWarnings("unused")
			public void contextSet(IEclipseContext context) {
				contextSetCalled++;
			}

			@SuppressWarnings("unused")
			@Inject
			public void setInjectedMethod(Object arg) {
				setMethodCalled++;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object methodValue = new Object();
		context.set("injectedMethod", methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		assertEquals(1, object.setMethodCalled);
		assertEquals(1, object.contextSetCalled);
		context.set("injectedMethod", "AnotherValue");
		assertEquals(2, object.setMethodCalled);
		assertEquals(1, object.contextSetCalled);
	}

	public void testContextSetZeroArgs() {
		class Injected {
			int contextSetCalled = 0;
			int setMethodCalled = 0;

			@SuppressWarnings("unused")
			public void contextSet() {
				contextSetCalled++;
			}

			@SuppressWarnings("unused")
			@Inject
			public void setInjectedMethod(Object arg) {
				setMethodCalled++;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object methodValue = new Object();
		context.set("injectedMethod", methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		assertEquals(1, object.setMethodCalled);
		assertEquals(1, object.contextSetCalled);
		context.set("injectedMethod", "AnotherValue");
		assertEquals(2, object.setMethodCalled);
		assertEquals(1, object.contextSetCalled);
	}

	/**
	 * Tests that fields are injected before methods.
	 */
	public void testFieldMethodOrder() {
		final AssertionFailedError[] error = new AssertionFailedError[1];
		class Injected {
			@Inject
			Object injectedField;
			Object methodValue;

			@SuppressWarnings("unused")
			@Inject
			public void setInjectedMethod(Object arg) {
				try {
					assertTrue(injectedField != null);
				} catch (AssertionFailedError e) {
					error[0] = e;
				}
				methodValue = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object fieldValue = new Object();
		Object methodValue = new Object();
		context.set("injectedField", fieldValue);
		context.set("injectedMethod", methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		if (error[0] != null)
			throw error[0];
		assertEquals(fieldValue, object.injectedField);
		assertEquals(methodValue, object.methodValue);

		// removing method value, the field should still have value
		context.remove("injectedMethod");
		if (error[0] != null)
			throw error[0];
		assertEquals(fieldValue, object.injectedField);
		assertNull(object.methodValue);

		((IDisposable) context).dispose();
		if (error[0] != null)
			throw error[0];
	}

	/**
	 * Tests that a class with multiple post-construct methods has post-construct methods invoked at
	 * the correct time.
	 */
	public void testInheritedPostConstruct() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("OverriddenMethod", new Object());
		context.set("StringViaMethod", "");
		context.set("ObjectViaMethod", new Object());
		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);
		assertEquals(1, userObject.superPostConstructCount);
		assertEquals(1, userObject.subPostConstructCount);
		context.set("OverriddenMethod", new Object());
		assertEquals(1, userObject.superPostConstructCount);
		assertEquals(1, userObject.subPostConstructCount);
	}

	/**
	 * Tests that a class with multiple pre-destroy methods has those methods invoked at the correct
	 * time.
	 */
	public void testInheritedPreDestroy() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("OverriddenMethod", new Object());
		context.set("StringViaMethod", "");
		context.set("ObjectViaMethod", new Object());
		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);
		assertEquals(0, userObject.superPreDestroyCount);
		assertEquals(0, userObject.subPreDestroyCount);
		context.set("OverriddenMethod", new Object());
		assertEquals(0, userObject.superPreDestroyCount);
		assertEquals(0, userObject.subPreDestroyCount);
		((IDisposable) context).dispose();
		assertEquals(1, userObject.superPreDestroyCount);
		assertEquals(1, userObject.subPreDestroyCount);
	}

	/**
	 * Tests that a class with a @PreDestroy method that is overridden by a subclass.
	 */
	public void testOverriddenPreDestroy() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("OverriddenMethod", new Object());
		context.set("StringViaMethod", "");
		context.set("ObjectViaMethod", new Object());
		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);
		assertEquals(0, userObject.overriddenPreDestroyCount);
		context.set("OverriddenMethod", new Object());
		assertEquals(0, userObject.overriddenPreDestroyCount);
		((IDisposable) context).dispose();
		assertEquals(1, userObject.overriddenPreDestroyCount);
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
		context.set("OverriddenMethod", new Object());

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
	 * Tests that a setter overridden from a superclass is only invoked once.
	 */
	public void testOverriddenSetter() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("OverriddenMethod", new Object());
		context.set("StringViaMethod", "");
		context.set("ObjectViaMethod", new Object());
		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);
		assertEquals(1, userObject.getOverriddenCount());
		context.set("OverriddenMethod", new Object());
		assertEquals(2, userObject.getOverriddenCount());

	}

	public void testPostConstruct() {
		class Injected {
			int postConstructCalled = 0;
			int setMethodCalled = 0;

			@SuppressWarnings("unused")
			@PostConstruct
			public void init() {
				postConstructCalled++;
			}

			@SuppressWarnings("unused")
			@Inject
			public void setInjectedMethod(Object arg) {
				setMethodCalled++;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object methodValue = new Object();
		context.set("injectedMethod", methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		assertEquals(1, object.setMethodCalled);
		assertEquals(1, object.postConstructCalled);
		context.set("injectedMethod", "AnotherValue");
		assertEquals(2, object.setMethodCalled);
		assertEquals(1, object.postConstructCalled);
	}

	/**
	 * Tests that post-construct methods are always called after all setters have been called
	 */
	public void testPostConstructAfterSetters() {
		IEclipseContext context = EclipseContextFactory.create();
		context.set("OverriddenMethod", new Object());
		context.set("StringViaMethod", "");
		context.set("ObjectViaMethod", new Object());
		ObjectSubClass userObject = new ObjectSubClass();
		ContextInjectionFactory.inject(userObject, context);
		assertEquals(1, userObject.postConstructSetObjectCalled);
		assertEquals(1, userObject.postConstructSetStringCalled);
		assertEquals(1, userObject.postConstructSetOverriddenCalled);

	}

}
