/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.manual;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.junit.Ignore;

/**
 * Manual test to observe error reporting. The JUnits in this
 * test are expected to produce exceptions in the output stream.
 */
public class InjectionErrorReportingTest extends TestCase {
	static class TestData {
	}
	
	static class InjectedMethod {
		public int setMethodCalled = 0;
		public TestData value;

		@Inject
		public void injectedMethod(@Named("testing123") TestData arg) {
			setMethodCalled++;
			value = arg;
		}
	}
	
	static class InjectedMethodNull {
		public int setMethodCalled = 0;
		public String nullString = null;

		@Inject
		public int injectedMethod(@Named("testing") TestData arg) {
			setMethodCalled++;
			return nullString.length();
		}
	}
	
	static class InjectedConstructor {
		public int setMethodCalled = 0;
		public TestData value;

		@Inject
		public InjectedConstructor(@Named("testing123") TestData arg) {
			setMethodCalled++;
			value = arg;
		}
	}
	
	static class InjectedConstructorCast {
		public int setMethodCalled = 0;
		public TestData value;
		public String nullString = "abc";

		@Inject
		public InjectedConstructorCast(@Named("testing") TestData arg) {
			setMethodCalled++;
			value = arg;
			Object otherObject = new TestData();
			nullString = (String) otherObject;
		}
	}

	static class InjectedField {
		@Inject @Named("testing123")
		public TestData data;
	}

	static class InjectedPostConstruct {
		public int setMethodCalled = 0;
		public String nullString = null;
		public int length;

		@PostConstruct
		public void myMethod() {
			setMethodCalled++;
			length =  nullString.length();
		}
	}
	
	static class InjectedPreDestroy {
		public int setMethodCalled = 0;
		public String nullString = null;
		public int length;

		@Inject
		public void injectedMethod(@Named("testing") TestData arg) {
			setMethodCalled++;
		}

		@PreDestroy
		public void myMethod() {
			setMethodCalled++;
			length =  nullString.length();
		}
	}
	
	@Creatable
	static class InjectedRecursive {
		@Inject
		public InjectedRecursive field;
	}

	/**
	 * Shows the error message for an unresolved method argument
	 */
	public void testMethodInjectionError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		InjectedMethod object = new InjectedMethod();
		boolean exception = false;
		try {
			ContextInjectionFactory.inject(object, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Shows the error message in case method call throws an exception
	 */
	public void testMethodInjectionNullError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		InjectedMethodNull object = new InjectedMethodNull();
		boolean exception = false;
		try {
			ContextInjectionFactory.inject(object, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Shows the error message for an unresolved constructor argument
	 */
	public void testConstructorInjectionError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		boolean exception = false;
		try {
			ContextInjectionFactory.make(InjectedConstructor.class, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Shows the error message for an exception in the injected constructor
	 */
	public void testConstructorCastError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		boolean exception = false;
		try {
			ContextInjectionFactory.make(InjectedConstructorCast.class, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Shows the error message for an unresolved field value
	 */
	public void testFieldInjectionError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		InjectedField object = new InjectedField();
		boolean exception = false;
		try {
			ContextInjectionFactory.inject(object, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Shows the error message in case @PostConstruct method call throws an exception
	 */
	public void testPostConstructError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		boolean exception = false;
		try {
			ContextInjectionFactory.make(InjectedPostConstruct.class, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Shows the error message in case @PreDestory method call throws an exception
	 */
	public void testPreDestoryError() {
		IEclipseContext context = EclipseContextFactory.create();
		TestData methodValue = new TestData();
		context.set("testing", methodValue);
		ContextInjectionFactory.make(InjectedPreDestroy.class, context);
		boolean exception = false;
		try {
			context.dispose();
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
	}
	
	/**
	 * Manual test to check error message for recursive object creation
	 */
	@Ignore("Exception on recursive creations removed with bug 377343")
	public void testRecursionError() {
		IEclipseContext context = EclipseContextFactory.create();
		boolean exception = false;
		try {
			ContextInjectionFactory.make(InjectedRecursive.class, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertTrue(exception);
		
		context.set(InjectedRecursive.class, new InjectedRecursive());
		exception = false;
		try {
			ContextInjectionFactory.make(InjectedRecursive.class, context);
		} catch (InjectionException e) {
			basicLog(e);
			exception = true;
		}
		assertFalse(exception);
	}

	private void basicLog(InjectionException e) {
		e.printStackTrace(System.out);
	}
}
