/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.internal.contexts.EclipseContext;

public class DisposingReferencedContextTest extends TestCase {
	
	static class MandatoryTarget {
		@Inject @Named("object")
		Object object;

		@Inject
		void setActiveContext(@Named(EclipseContext.ACTIVE_CHILD) IEclipseContext partContext) {
			if (partContext != null)
				partContext.get("someVar");
		}
	}
	
	static class OptionalTarget {
		@Inject @Named("object")
		Object object;

		@Inject
		void setActiveContext(@Optional @Named(EclipseContext.ACTIVE_CHILD) IEclipseContext partContext) {
			if (partContext != null)
				partContext.get("someVar");
		}
	}

	public void testContextDisposeCausesCompleteUninjection_Mandatory_True() {
		testContextDisposeCausesCompleteUninjection_Mandatory(true);
	}

	public void testContextDisposeCausesCompleteUninjection_Mandatory_False() {
		testContextDisposeCausesCompleteUninjection_Mandatory(false);
	}
	
	public void testContextDisposeCausesCompleteUninjection_Optional_True() {
		testContextDisposeCausesCompleteUninjection_Optional(true);
	}

	public void testContextDisposeCausesCompleteUninjection_Optional_False() {
		testContextDisposeCausesCompleteUninjection_Optional(false);
	}

	private void testContextDisposeCausesCompleteUninjection_Mandatory(boolean disposeFirst) {
		IEclipseContext windowContext = EclipseContextFactory.create("windowContext");
		IEclipseContext partContext = windowContext.createChild("partContext");

		partContext.activate();

		Object o = new Object();
		windowContext.set("object", o);

		MandatoryTarget target = new MandatoryTarget();
		ContextInjectionFactory.inject(target, windowContext);
		assertEquals("The object should have been injected", o, target.object);

		partContext.dispose();

		assertEquals("The object should not have been uninjected", o, target.object);
	}

	private void testContextDisposeCausesCompleteUninjection_Optional(boolean disposeFirst) {
		IEclipseContext windowContext = EclipseContextFactory.create();
		IEclipseContext partContext = windowContext.createChild();

		partContext.activate();

		Object o = new Object();
		windowContext.set("object", o);

		OptionalTarget target = new OptionalTarget();
		ContextInjectionFactory.inject(target, windowContext);
		assertEquals("The object should have been injected", o, target.object);

		partContext.dispose();

		assertEquals("The object should not have been uninjected", o, target.object);
	}
}
