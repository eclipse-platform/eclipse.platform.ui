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
package org.eclipse.e4.ui.tests.application;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.services.IServiceConstants;

public class Bug299755Test extends TestCase {

	static class InjectionObject {

		// comment the @Inject out to get the test to pass
		@Inject
		Object object;
	}

	static class Out {

		@Inject
		private IEclipseContext context;

		public void setSelection(Object selection) {
			context.modify(IServiceConstants.SELECTION, selection);
		}
	}

	static class In {

		// comment the @Inject out to get the test to pass
		@Inject
		InjectionObject object;

		private Object selection;

		@Inject
		@Optional
		void setSelection(@Named(IServiceConstants.SELECTION) Object selection) {
			this.selection = selection;
		}

		public Object getSelection() {
			return selection;
		}
	}

	public void testBug299755() throws Exception {
		// create a top-level context
		IEclipseContext windowContext = EclipseContextFactory.create();
		windowContext.set(Object.class.getName(), new Object());
		// put the event broker inside
		windowContext.set(InjectionObject.class.getName(),
				new ContextFunction() {
					@Override
					public Object compute(IEclipseContext context,
							Object[] arguments) {
						try {
							return ContextInjectionFactory.make(
									InjectionObject.class, context);
						} catch (InvocationTargetException e) {
							throw new RuntimeException(e);
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						}
					}
				});
		// declare selection as modifiable
		windowContext.declareModifiable(IServiceConstants.SELECTION);

		// create an "out" part context
		IEclipseContext outContext = EclipseContextFactory.create(
				windowContext, null);
		// create an "in" part context
		IEclipseContext inContext = EclipseContextFactory.create(windowContext,
				null);

		Out out = (Out) ContextInjectionFactory.make(Out.class, outContext);
		In in = (In) ContextInjectionFactory.make(In.class, inContext);

		// no selection in the beginning
		assertNull(in.getSelection());

		// change the selection
		Object selection = new Object();
		out.setSelection(selection);

		// the "in" part should've gotten the new selection
		assertEquals(selection, in.getSelection());
	}
}
