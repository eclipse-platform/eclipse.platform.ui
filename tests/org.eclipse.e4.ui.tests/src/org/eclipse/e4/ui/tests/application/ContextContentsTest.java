/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.ISchedulingExecutor;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.services.EHandlerService;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.events.EventBrokerFactory;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.event.EventAdmin;

public class ContextContentsTest extends TestCase {

	private IEclipseContext osgiContext;
	private IEclipseContext appContext;

	@Override
	protected void setUp() throws Exception {
		osgiContext = createOSGiContext();
		appContext = createAppContext();
		super.setUp();
	}

	static {
		// we need EventAdmin
		Bundle bundle = Platform.getBundle("org.eclipse.equinox.event");
		try {
			if (bundle.getState() != Bundle.ACTIVE) {
				bundle.start(Bundle.START_TRANSIENT);
			}
		} catch (BundleException e) {
			throw new RuntimeException(e);
		}
	}

	private void testGet(IEclipseContext eclipseContext, String name,
			boolean expected) {
		if (expected) {
			assertNotNull(eclipseContext.get(name));
		} else {
			assertNull(eclipseContext.get(name));
		}
	}

	private void testGet_PARENT(IEclipseContext eclipseContext, boolean expected) {
		testGet(eclipseContext, IContextConstants.PARENT, expected);
	}

	public void testGet_PARENT() {
		testGet_PARENT(osgiContext, false);
		testGet_PARENT(appContext, true);
	}

	private void testGet(IEclipseContext eclipseContext, Class<?> cls,
			boolean expected) {
		testGet(eclipseContext, cls.getName(), expected);
	}

	// private void testGet_IEclipseContext(IEclipseContext eclipseContext,
	// boolean expected) {
	// testGet(eclipseContext, IEclipseContext.class, expected);
	// }
	//
	// public void testGet_IEclipseContext() {
	// testGet_IEclipseContext(osgiContext, true);
	// testGet_IEclipseContext(appContext, true);
	// }

	private void testGet_IExtensionRegistry(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IExtensionRegistry.class, expected);
	}

	public void testGet_IExtensionRegistry() {
		testGet_IExtensionRegistry(osgiContext, true);
		testGet_IExtensionRegistry(appContext, true);
	}

	private void testGet_EventAdmin(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, EventAdmin.class, expected);
	}

	public void testGet_EventAdmin() {
		testGet_EventAdmin(osgiContext, true);
		testGet_EventAdmin(appContext, true);
	}

	private void testGet_IAdapterManager(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IAdapterManager.class, expected);
	}

	public void testGet_IAdapterManager() {
		testGet_IAdapterManager(osgiContext, true);
		testGet_IAdapterManager(appContext, true);
	}

	private void testGet_IPreferencesService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IPreferencesService.class, expected);
	}

	public void testGet_IPreferencesService() {
		testGet_IPreferencesService(osgiContext, true);
		testGet_IPreferencesService(appContext, true);
	}

	private void testGet_ISchedulingExecutor(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, ISchedulingExecutor.SERVICE_NAME, expected);
	}

	public void testGet_ISchedulingExecutor() {
		testGet_ISchedulingExecutor(osgiContext, true);
		testGet_ISchedulingExecutor(appContext, true);
	}

	private void testGet_IEventBroker(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IEventBroker.class, expected);
	}

	public void testGet_IEventBroker() {
		testGet_IEventBroker(osgiContext, false);
		testGet_IEventBroker(appContext, true);
	}

	private void testGet_IContributionFactory(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IContributionFactory.class, expected);
	}

	public void testGet_IContributionFactory() {
		testGet_IContributionFactory(osgiContext, false);
		testGet_IContributionFactory(appContext, true);
	}

	// private void testGet_IContributionFactorySpi(
	// IEclipseContext eclipseContext, boolean expected) {
	// testGet(eclipseContext, IContributionFactorySpi.class, expected);
	// }
	//
	// public void testGet_IContributionFactorySpi() {
	// testGet_IContributionFactorySpi(osgiContext, false);
	// testGet_IContributionFactorySpi(appContext, true);
	// }

	private void testGet_IExceptionHandler(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IExceptionHandler.class, expected);
	}

	public void testGet_IExceptionHandler() {
		testGet_IExceptionHandler(osgiContext, false);
		testGet_IExceptionHandler(appContext, true);
	}

	private void testGet_Logger(IEclipseContext eclipseContext, boolean expected) {
		testGet(eclipseContext, Logger.class, expected);
	}

	public void testGet_Logger() {
		testGet_Logger(osgiContext, false);
		testGet_Logger(appContext, true);
	}

	private void testGet_IStylingEngine(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IStylingEngine.SERVICE_NAME, expected);
	}

	public void testGet_IStylingEngine() {
		testGet_IStylingEngine(osgiContext, false);
		testGet_IStylingEngine(appContext, false);
	}

	private void testGet_IPresentationEngine(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IPresentationEngine.SERVICE_NAME, expected);
	}

	public void testGet_IPresentationEngine() {
		testGet_IPresentationEngine(osgiContext, false);
		testGet_IPresentationEngine(appContext, false);
	}

	// private void testGet_ECommandService(IEclipseContext eclipseContext,
	// boolean expected) {
	// testGet(eclipseContext, ECommandService.class, expected);
	// }
	//
	// public void testGet_ECommandService() {
	// testGet_ECommandService(osgiContext, true);
	// testGet_ECommandService(appContext, true);
	// }

	private void testGet_EHandlerService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, EHandlerService.class, expected);
	}

	public void testGet_EHandlerService() {
		testGet_EHandlerService(osgiContext, true);
		testGet_EHandlerService(appContext, true);
	}

	// private void testGet_EBindingService(IEclipseContext eclipseContext,
	// boolean expected) {
	// testGet(eclipseContext, EBindingService.class, expected);
	// }
	//
	// public void testGet_EBindingService() {
	// testGet_EBindingService(osgiContext, true);
	// testGet_EBindingService(appContext, true);
	// }

	private void testGet_EContextService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, EContextService.class, expected);
	}

	public void testGet_EContextService() {
		testGet_EContextService(osgiContext, true);
		testGet_EContextService(appContext, true);
	}

	private IEclipseContext createOSGiContext() {
		IEclipseContext serviceContext = EclipseContextFactory
				.createServiceContext(Activator.getDefault().getBundle()
						.getBundleContext());
		return serviceContext;
	}

	private IEclipseContext createAppContext() {
		assertNotNull(osgiContext);

		IEclipseContext appContext = createContext(osgiContext);
		appContext.set(IEventBroker.class.getName(), EventBrokerFactory
				.newEventBroker());
		appContext.set(IContributionFactory.class.getName(), new Object());
		appContext.set(IExceptionHandler.class.getName(), new Object());
		appContext.set(Logger.class.getName(), new Object());
		return appContext;
	}

	private IEclipseContext createContext(IEclipseContext parent) {
		IEclipseContext eclipseContext = EclipseContextFactory.create(parent,
				null);
		return eclipseContext;
	}

}
