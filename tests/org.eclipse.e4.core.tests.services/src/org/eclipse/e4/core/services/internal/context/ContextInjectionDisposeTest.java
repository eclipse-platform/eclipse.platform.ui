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
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;

/**
 * Tests for the basic context injection functionality
 */
public class ContextInjectionDisposeTest extends TestCase {

	public static Test suite() {
		return new TestSuite(ContextInjectionDisposeTest.class);
	}

	public ContextInjectionDisposeTest() {
		super();
	}

	public ContextInjectionDisposeTest(String name) {
		super(name);
	}

	public void testContextDisposedNoArg() {
		class Injected implements IDisposable {

			boolean disposeInvoked = false;
			private String inject_Field;

			public void dispose() {
				disposeInvoked = true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set("Field", "hello");
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		((IDisposable) context).dispose();
		assertTrue(object.disposeInvoked);
	}

	public void testDisposeContext() {
		class Injected implements IDisposable {
			boolean disposeInvoked = false;
			Object inject_Field;
			String methodValue;

			public void dispose() {
				disposeInvoked = true;
			}

			public void inject_InjectedMethod(String arg) {
				methodValue = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object fieldValue = new Object();
		Object methodValue = "abc";
		context.set("Field", fieldValue);
		context.set(String.class.getName(), methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);

		assertEquals(fieldValue, object.inject_Field);
		assertEquals(methodValue, object.methodValue);

		// disposing context should clear values
		((IDisposable) context).dispose();
		assertNull(object.inject_Field);
		assertNull(object.methodValue);
		assertTrue(object.disposeInvoked);
	}

	public void testReleaseObject() {
		class Injected implements IDisposable {
			boolean disposeInvoked = false;
			boolean destroyInvoked = false;

			Object inject_Field;
			Object methodValue;

			public void destroy() {
				destroyInvoked = true;
			}

			public void dispose() {
				disposeInvoked = true;
			}

			public void inject_InjectedMethod(String arg) {
				methodValue = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object fieldValue = new Object();
		Object methodValue = "abc";
		context.set("Field", fieldValue);
		context.set(String.class.getName(), methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);

		assertEquals(fieldValue, object.inject_Field);
		assertEquals(methodValue, object.methodValue);

		// releasing should have the same effect on the single object as
		// disposing the context does.
		ContextInjectionFactory.uninject(object, context);

		assertNull(object.inject_Field);
		assertNull(object.methodValue);
		assertFalse(object.disposeInvoked);
		assertFalse(object.destroyInvoked);
	}

}
