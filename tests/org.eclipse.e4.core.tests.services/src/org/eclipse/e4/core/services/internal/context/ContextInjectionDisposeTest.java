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
import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.e4.core.services.annotations.PreDestroy;
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

	/**
	 * Should prefer contextDisposed() over dispose().
	 */
	public void testContextDisposedOneArg() {
		class Injected {
			boolean contextDisposedInvoked = false;

			boolean disposeInvoked = false;
			@SuppressWarnings("unused")
			@In
			private String field;

			@SuppressWarnings("unused")
			public void contextDisposed(IEclipseContext context) {
				contextDisposedInvoked = true;
			}

			@SuppressWarnings("unused")
			public void dispose() {
				disposeInvoked = true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set("field", "hello");
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		((IDisposable) context).dispose();
		assertTrue(object.contextDisposedInvoked);
		assertFalse(object.disposeInvoked);
	}

	/**
	 * Should prefer dispose() over contextDisposed(IEclipseContext, String)
	 */
	public void testContextDisposedTwoArgs() {
		class Injected {
			boolean contextDisposedInvoked = false;

			boolean disposeInvoked = false;
			@SuppressWarnings("unused")
			@In
			private String field;

			@SuppressWarnings("unused")
			public void contextDisposed(IEclipseContext context, String arg2) {
				contextDisposedInvoked = true;
			}

			@SuppressWarnings("unused")
			public void dispose() {
				disposeInvoked = true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set("field", "hello");
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		((IDisposable) context).dispose();
		assertFalse(object.contextDisposedInvoked);
		assertTrue(object.disposeInvoked);
	}

	/**
	 * Should prefer contextDisposed() over dispose().
	 */
	public void testContextDisposedZeroArgs() {
		class Injected {
			boolean contextDisposedInvoked = false;
			boolean disposeInvoked = false;
			@SuppressWarnings("unused")
			@In
			private String field;

			@SuppressWarnings("unused")
			public void contextDisposed() {
				contextDisposedInvoked = true;
			}

			@SuppressWarnings("unused")
			public void dispose() {
				disposeInvoked = true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set("field", "hello");
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		((IDisposable) context).dispose();
		assertTrue(object.contextDisposedInvoked);
		assertFalse(object.disposeInvoked);
	}

	public void testDisposeContext() {
		class Injected {
			boolean contextDisposedInvoked = false;
			boolean disposeInvoked = false;
			@In
			Object injectedField;
			Object methodValue;

			@SuppressWarnings("unused")
			@PreDestroy
			public void destroy() {
				injectedField = null;
				methodValue = null;
			}

			@SuppressWarnings("unused")
			public void dispose() {
				disposeInvoked = true;
			}

			@SuppressWarnings("unused")
			public void contextDisposed(IEclipseContext context) {
				contextDisposedInvoked = true;
			}

			@SuppressWarnings("unused")
			@In
			public void setInjectedMethod(Object arg) {
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

		// disposing context should clear values
		((IDisposable) context).dispose();
		assertNull(object.injectedField);
		assertNull(object.methodValue);
		assertFalse(object.disposeInvoked);
		assertFalse(object.contextDisposedInvoked);
	}

}
