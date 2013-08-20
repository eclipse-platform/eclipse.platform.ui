/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;

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
		class Injected {

			boolean disposeInvoked = false;

			@SuppressWarnings("unused")
			@Inject
			private String Field;

			@PreDestroy
			public void dispose() {
				disposeInvoked = true;
			}
		}
		IEclipseContext context = EclipseContextFactory.create();
		context.set(String.class.getName(), "hello");
		Injected object = new Injected();
		ContextInjectionFactory.inject(object, context);
		context.dispose();
		assertTrue(object.disposeInvoked);
	}

	public void testDisposeContext() {
		class Injected {
			boolean disposeInvoked = false;

			@Inject
			Object Field;
			String methodValue;

			@PreDestroy
			public void dispose() {
				disposeInvoked = true;
			}

			@Inject
			public void InjectedMethod(String arg) {
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

		assertEquals(fieldValue, object.Field);
		assertEquals(methodValue, object.methodValue);

		// disposing context calls @PreDestory, but does not clear injected values
		context.dispose();
		assertNotNull(object.Field);
		assertNotNull(object.methodValue);
		assertTrue(object.disposeInvoked);
	}

	public void testReleaseObject() {
		class Injected {
			boolean disposeInvoked = false;

			@Inject
			Integer Field;
			Object methodValue;

			@PreDestroy
			public void dispose() {
				disposeInvoked = true;
			}

			@Inject
			public void InjectedMethod(@Optional String arg) {
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

		assertEquals(fieldValue, object.Field);
		assertEquals(methodValue, object.methodValue);

		// releasing should have the same effect on the single object as
		// disposing the context does.
		ContextInjectionFactory.uninject(object, context);

		assertEquals(fieldValue, object.Field);
		assertNull(object.methodValue);
		assertTrue(object.disposeInvoked);
	}

}
