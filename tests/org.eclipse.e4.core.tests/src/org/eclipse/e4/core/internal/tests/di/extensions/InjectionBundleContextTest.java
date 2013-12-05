/*******************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others. All rights reserved. 
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.di.extensions;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.BundleContext;
import org.eclipse.e4.core.internal.tests.CoreTestsActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class InjectionBundleContextTest extends TestCase {

	// classed used as a user of the @BundleContext annotation
	static class InjectionTarget {

		private org.osgi.framework.BundleContext ctx;

		@Inject
		public void setBundleContext(
				@BundleContext @Optional org.osgi.framework.BundleContext ctx) {
			this.ctx = ctx;
		}

		public boolean hasContext() {
			return this.ctx != null;
		}

		public org.osgi.framework.BundleContext getContext() {
			return this.ctx;
		}
	}

	private InjectionTarget target;
	private Bundle bundle;

	@Override
	protected void tearDown() throws Exception {
		bundle.start();

		final org.osgi.framework.BundleContext bundleContext = CoreTestsActivator
				.getDefault().getBundleContext();
		final IEclipseContext localContext = EclipseContextFactory
				.getServiceContext(bundleContext);

		ContextInjectionFactory.uninject(target, localContext);

		super.tearDown();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final org.osgi.framework.BundleContext bundleContext = CoreTestsActivator
				.getDefault().getBundleContext();
		bundle = bundleContext.getBundle();

		final IEclipseContext localContext = EclipseContextFactory
				.getServiceContext(bundleContext);

		target = ContextInjectionFactory.make(InjectionTarget.class,
				localContext);
	}

	public void testInject() {
		assertTrue(target.hasContext());
	}

	public void testUnInject() throws BundleException, InterruptedException {
		// inject
		assertTrue(target.hasContext());

		// Check also that the BundleContext instance has indeed changed
		final org.osgi.framework.BundleContext firstContext = target
				.getContext();

		// uninject
		bundle.stop();
		assertFalse(target.hasContext());

		// re-inject
		bundle.start();
		assertTrue(target.hasContext());

		final org.osgi.framework.BundleContext secondContext = target
				.getContext();
		assertNotSame(firstContext, secondContext);
	}
}
