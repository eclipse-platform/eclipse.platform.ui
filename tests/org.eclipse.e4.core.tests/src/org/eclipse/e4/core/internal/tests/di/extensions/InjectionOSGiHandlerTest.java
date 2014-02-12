/*******************************************************************************
 * Copyright (c) 2014 Markus Alexander Kuppe and others. All rights reserved. 
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di.extensions;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.OSGiBundle;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

public class InjectionOSGiHandlerTest extends TestCase {

	public static class TestHandler {
		
		private BundleContext ctx;

		@Execute
		public void execute(@OSGiBundle BundleContext ctx) {
			this.ctx = ctx;
		}

		public BundleContext getCtx() {
			return ctx;
		}
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInjectBCinExecute() {
		
		final BundleContext bundleContext = CoreTestsActivator
				.getDefault().getBundleContext();
		final IEclipseContext localContext = EclipseContextFactory
				.getServiceContext(bundleContext);

		TestHandler handler = new TestHandler();
		ContextInjectionFactory.invoke(handler, Execute.class, localContext);

		assertNotNull(handler.getCtx());
	}

}
