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
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.annotations.PreDestroy;

public class InjectionOrderTest extends TestCase {

	public static class InjectTargetMethod {
		
		boolean nonNull = false;
		
		Object o;

		@Inject
		void set(@Named("inject") Object o) {
			this.o = o;
		}

		@PreDestroy
		void pd() {
			// methods should always be uninjected after @PD is called
			nonNull = o != null;
		}
	}

	public static class InjectTargetField {
		
		boolean nonNull = false;
		
		@Inject @Named("inject")
		Object o;

		@PreDestroy
		void pd() {
			// fields should always be uninjected after @PD is called
			nonNull = o != null;
		}
	}

	/**
	 * Tests to ensure that the injection/uninjection order of fields is correct.
	 * <p>
	 * See bug 304859.
	 * </p>
	 */
	public void testDisposeMethod() throws Exception {
		// create a context
		IEclipseContext appContext = EclipseContextFactory.create();
		// set a value
		appContext.set("inject", "a");

		// instantiate the object
		InjectTargetMethod injectTargetMethod = (InjectTargetMethod) ContextInjectionFactory.make(InjectTargetMethod.class, appContext);
		// change the requested value so another injection occurs
		appContext.set("inject", "b");

		// now we dispose the context 
		((IDisposable) appContext).dispose();
		
		// check that the second 'set' invocation did not alter the order of notifications
		assertTrue("@PreDestroy was incorrectly called after the method was uninjected", injectTargetMethod.nonNull);
	}

	/**
	 * Tests to ensure that the injection/uninjection order of methods is correct.
	 * <p>
	 * See bug 304859.
	 * </p>
	 */
	public void testDisposeField() throws Exception {
		// create a context
		IEclipseContext appContext = EclipseContextFactory.create();
		// set a value
		appContext.set("inject", "a");

		// instantiate the object
		InjectTargetField injectTargetField = (InjectTargetField) ContextInjectionFactory.make(InjectTargetField.class, appContext);
		// change the requested value so another injection occurs
		appContext.set("inject", "b");

		// now we dispose the context 
		((IDisposable) appContext).dispose();
		
		assertTrue("@PreDestroy was incorrectly called after the field was uninjected", injectTargetField.nonNull);
	}
}
