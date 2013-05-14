/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

/**
 * See bug 296337: duplicate disposal of an object
 */
public class ComplexDisposalTest extends TestCase {

	public static class Test {
		private int count = 0;

		@Inject
		@Named("aString")
		String string;

		public int getCount() {
			return count;
		}

		@PreDestroy
		void preDestroy() {
			count++;
		}
	}

	public static class TestFunction extends ContextFunction {
		@Override
		public Object compute(IEclipseContext context, String contextKey) {
			return ContextInjectionFactory.make(Test.class, context);
		}
	}

	public void testU() throws Exception {
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("aString", "");
		parentContext.set(Test.class.getName(), new TestFunction());
		IEclipseContext context = parentContext.createChild();

		Test test = (Test) context.get(Test.class.getName());

		assertEquals(0, test.getCount());
		context.dispose();
		assertEquals("Context disposed, @PreDestroy should've been called", 1, test.getCount());
		parentContext.dispose();
		assertEquals("Parent context disposed, @PreDestroy should not have been called again", 1, test.getCount());
	}

	public void testV() throws Exception {
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("aString", "");
		IEclipseContext context = parentContext.createChild();

		Test test = (Test) ContextInjectionFactory.make(Test.class, context);

		assertEquals(0, test.getCount());
		context.dispose();
		assertEquals("Context disposed, @PreDestroy should've been called", 1, test.getCount());
		parentContext.dispose();
		assertEquals("Parent context disposed, @PreDestroy should not have been called again", 1, test.getCount());
	}

	public void testW() throws Exception {
		IEclipseContext parentContext = EclipseContextFactory.create();
		parentContext.set("aString", "");
		IEclipseContext context = parentContext.createChild();

		Test test = new Test();
		ContextInjectionFactory.inject(test, context);

		assertEquals(0, test.getCount());
		context.dispose();
		assertEquals("Context disposed, @PreDestroy should've been called", 1, test.getCount());
		parentContext.dispose();
		assertEquals("Parent context disposed, @PreDestroy should not have been called again", 1, test.getCount());
	}
}
