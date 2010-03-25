/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
 * Tests for injection handling of context dispose, and handling disposal of injected objects.
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
			private String inject__Field;

			public void dispose() {
				disposeInvoked = true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set(String.class.getName(), "hello");
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		((IDisposable) context).dispose();
		assertTrue(object.disposeInvoked);
	}

	public void testDisposeContext() {
		class Injected implements IDisposable {
			boolean disposeInvoked = false;
			Object inject__Field;
			String methodValue;

			public void dispose() {
				disposeInvoked = true;
			}

			public void inject__InjectedMethod(String arg) {
				methodValue = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object fieldValue = new Object();
		Object methodValue = "abc";
		context.set(Object.class.getName(), fieldValue);
		context.set(String.class.getName(), methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);

		assertEquals(fieldValue, object.inject__Field);
		assertEquals(methodValue, object.methodValue);

		// disposing context should clear values
		((IDisposable) context).dispose();
		assertNull(object.inject__Field);
		assertNull(object.methodValue);
		assertTrue(object.disposeInvoked);
	}

	public void testReleaseObject() {
		class Injected implements IDisposable {
			boolean disposeInvoked = false;
			boolean destroyInvoked = false;

			Integer inject__Field;
			Object methodValue;

			public void destroy() {
				destroyInvoked = true;
			}

			public void dispose() {
				disposeInvoked = true;
			}

			public void inject__InjectedMethod(String arg) {
				methodValue = arg;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		Object fieldValue = new Integer(123);
		Object methodValue = "abc";
		context.set(Integer.class.getName(), fieldValue);
		context.set(String.class.getName(), methodValue);
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);

		assertEquals(fieldValue, object.inject__Field);
		assertEquals(methodValue, object.methodValue);

		// releasing should have the same effect on the single object as
		// disposing the context does.
		ContextInjectionFactory.uninject(object, context);

		assertNull(object.inject__Field);
		assertNull(object.methodValue);
		assertFalse(object.disposeInvoked);
		assertFalse(object.destroyInvoked);
	}

}
