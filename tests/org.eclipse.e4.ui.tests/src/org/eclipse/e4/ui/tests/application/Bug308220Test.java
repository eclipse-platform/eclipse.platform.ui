/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440893
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.junit.Test;

public class Bug308220Test {

	static class WindowService {
		Object activePart;

		@Inject
		public void setActivePart(
				@Named(IServiceConstants.ACTIVE_PART) @Optional Object part) {
			activePart = part;
		}
	}

	@Test
	public void testBug308220() throws Exception {
		IEclipseContext app = EclipseContextFactory.create();

		// lookup function that goes down the context's active child chain
		app.set(IServiceConstants.ACTIVE_PART, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				IEclipseContext childContext = context.getActiveChild();
				if (childContext == null) {
					return null;
				}

				while (childContext != null) {
					context = childContext;
					childContext = context.getActiveChild();
				}
				return context.getLocal(Object.class.getName());
			}
		});

		app.runAndTrack(new RunAndTrack() {
			@Override
			public boolean changed(IEclipseContext context) {
				// remove this line to pass the test
				context.get(IServiceConstants.ACTIVE_PART);
				return true;
			}
		});

		// create two contexts
		IEclipseContext windowA = app.createChild();
		IEclipseContext windowB = app.createChild();

		Object o1 = new Object();
		Object o2 = new Object();

		IEclipseContext part = windowA.createChild();
		// set the active part as some object
		part.set(Object.class.getName(), o1);
		// construct the active chain
		part.activate();
		windowA.activate();

		WindowService windowServiceA = ContextInjectionFactory
				.make(WindowService.class, windowA);
		WindowService windowServiceB = ContextInjectionFactory
				.make(WindowService.class, windowB);

		// windowA should have an active part, it was set earlier
		assertEquals(o1, windowServiceA.activePart);
		// windowB has no child contexts, this should be null
		assertNull(windowServiceB.activePart);

		// change the active part
		part.set(Object.class.getName(), o2);

		// windowA's active part should have changed
		assertEquals(o2, windowServiceA.activePart);
		// windowB should still have no active part
		assertNull(windowServiceB.activePart);
	}
}
