/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Creatable;

public class AutoConstructTest extends TestCase {

	@Creatable
	static class Dependent1 {
		@Inject
		public Dependent1() {
			// placeholder
		}
	}

	static class Dependent2 {
		@Inject
		public Dependent2() {
			// placeholder
		}
	}

	static class Consumer1 {
		@Inject
		public Consumer1(Dependent1 dep) {
			// placeholder
		}
	}

	static class Consumer2 {
		@Inject
		public Consumer2(Dependent2 dep) {
			// placeholder
		}
	}

	/**
	 * Checks that only classes with @Creatable are auto-constructed
	 */
	public void testCreatable() {
		IEclipseContext context = EclipseContextFactory.create();
		Consumer1 consumer1 = ContextInjectionFactory.make(Consumer1.class,
				context);
		assertNotNull(consumer1);

		boolean exception = false;
		try {
			ContextInjectionFactory.make(Consumer2.class, context);
		} catch (InjectionException e) {
			exception = true; // expected
		}
		assertTrue(exception);

		context.set(Dependent2.class, new Dependent2());
		Consumer2 consumer2 = ContextInjectionFactory.make(Consumer2.class,
				context);
		assertNotNull(consumer2);
	}
}
