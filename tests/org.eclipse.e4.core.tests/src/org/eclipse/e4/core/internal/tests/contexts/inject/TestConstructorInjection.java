/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.internal.tests.contexts.inject;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class TestConstructorInjection extends TestCase {

	static class TestConstructorObject {

		public boolean defaultConstructorCalled = false;
		public boolean constructorIntStrCalled = false;
		public boolean constructorIntBoolCalled = false;
		public boolean injectedMethodCalled = false;
		public boolean nonInjectedMethodCalled = false;

		@Inject
		private Character c;

		public Integer i;
		public String s;
		public Boolean b;
		public Double d;
		public Float f;

		public boolean orderCorrect = true;

		public TestConstructorObject() {
			defaultConstructorCalled = true;
		}

		public TestConstructorObject(Integer i, String s) {
			constructorIntStrCalled = true;
			this.i = i;
			this.s = s;
		}

		@Inject
		public TestConstructorObject(Integer i, Boolean b) {
			constructorIntBoolCalled = true;
			this.i = i;
			this.b = b;
			// the constructor should be called first
			if ((c != null) || (d != null) || (f != null))
				orderCorrect = false;
		}

		@Inject
		public void injectedMethod(Double d, Float f) {
			injectedMethodCalled = true;
			this.d = d;
			this.f = f;
			// the method injection after constructor and field injection
			if ((c == null) || (i == null) || (b == null))
				orderCorrect = false;
		}

		public void nonInjectedMethod(Double d) {
			nonInjectedMethodCalled = true;
			this.d = d;
		}

		public Character getChar() {
			return c;
		}
	}

	public void testConstructorInjection() throws InvocationTargetException, InstantiationException {
		IEclipseContext context = EclipseContextFactory.create();
		Integer intValue = new Integer(123);
		context.set(Integer.class.getName(), intValue);
		Boolean boolValue = new Boolean(true);
		context.set(Boolean.class.getName(), boolValue);
		Double doubleValue = new Double(1.23);
		context.set(Double.class.getName(), doubleValue);
		Float floatValue = new Float(12.3);
		context.set(Float.class.getName(), floatValue);
		Character charValue = new Character('v');
		context.set(Character.class.getName(), charValue);

		Object result = ContextInjectionFactory.make(TestConstructorObject.class, context);
		assertNotNull(result);
		assertTrue(result instanceof TestConstructorObject);

		TestConstructorObject testObject = ((TestConstructorObject) result);

		assertFalse(testObject.defaultConstructorCalled);
		assertFalse(testObject.constructorIntStrCalled);
		assertTrue(testObject.constructorIntBoolCalled);
		assertTrue(testObject.injectedMethodCalled);
		assertFalse(testObject.nonInjectedMethodCalled);
		assertTrue(testObject.orderCorrect);

		assertEquals(intValue, testObject.i);
		assertEquals(boolValue, testObject.b);
		assertEquals(doubleValue, testObject.d);
		assertEquals(floatValue, testObject.f);
		assertEquals(charValue, testObject.getChar());
		assertNull(testObject.s);

	}

}
