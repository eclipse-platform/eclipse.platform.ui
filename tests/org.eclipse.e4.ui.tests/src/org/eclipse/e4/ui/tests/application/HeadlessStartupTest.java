/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import junit.framework.TestCase;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.services.EclipseAdapter;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.workbench.ActivePartLookupFunction;
import org.eclipse.e4.ui.internal.workbench.ExceptionHandler;
import org.eclipse.e4.ui.internal.workbench.ReflectionContributionFactory;
import org.eclipse.e4.ui.internal.workbench.WorkbenchLogger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.ui.workbench.IExceptionHandler;

public abstract class HeadlessStartupTest extends TestCase {

	protected IEclipseContext osgiContext;

	protected IEclipseContext applicationContext;

	@Override
	protected void setUp() throws Exception {
		applicationContext = createApplicationContext();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		applicationContext.dispose();

		// if (osgiContext instanceof IDisposable) {
		// ((IDisposable) osgiContext).dispose();
		// }
	}

	private IEclipseContext createOSGiContext() {
		osgiContext = EclipseContextFactory.getServiceContext(Activator
				.getDefault().getBundle().getBundleContext());
		return osgiContext;
	}

	private IEclipseContext createApplicationContext() {
		return createApplicationContext(createOSGiContext());
	}

	protected IEclipseContext createApplicationContext(
			IEclipseContext osgiContext) {
		assertNotNull(osgiContext);

		final IEclipseContext appContext = osgiContext
				.createChild("Application Context");

		appContext.set(
				IContributionFactory.class.getName(),
				new ReflectionContributionFactory(
						(IExtensionRegistry) appContext
								.get(IExtensionRegistry.class.getName())));
		appContext.set(IExceptionHandler.class.getName(),
				new ExceptionHandler());
		// TODO is there a reason the logger isn't injected with the context?
		appContext
				.set(Logger.class.getName(), ContextInjectionFactory.make(
						WorkbenchLogger.class, appContext));

		appContext.set(Adapter.class.getName(),
				ContextInjectionFactory.make(EclipseAdapter.class, appContext));
		appContext.set(ContextManager.class.getName(), new ContextManager());

		appContext.set(IServiceConstants.ACTIVE_CONTEXTS,
				new ActiveContextsFunction());
		appContext.set(IServiceConstants.ACTIVE_PART,
				new ActivePartLookupFunction());

		return appContext;
	}
}
