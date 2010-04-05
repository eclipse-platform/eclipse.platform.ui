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

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.Adapter;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.ISchedulingExecutor;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.osgi.service.event.EventAdmin;

public class ContextContentsTest extends HeadlessStartupTest {

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
		testGet_PARENT(applicationContext, true);
	}

	private void testGet(IEclipseContext eclipseContext, Class<?> cls,
			boolean expected) {
		testGet(eclipseContext, cls.getName(), expected);
	}

	private void testGet_IEclipseContext(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IEclipseContext.class, expected);
	}

	public void testGet_IEclipseContext() {
		testGet_IEclipseContext(osgiContext, true);
		testGet_IEclipseContext(applicationContext, true);
	}

	private void testGet_IExtensionRegistry(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IExtensionRegistry.class, expected);
	}

	public void testGet_IExtensionRegistry() {
		testGet_IExtensionRegistry(osgiContext, true);
		testGet_IExtensionRegistry(applicationContext, true);
	}

	private void testGet_EventAdmin(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, EventAdmin.class, expected);
	}

	public void testGet_EventAdmin() {
		testGet_EventAdmin(osgiContext, true);
		testGet_EventAdmin(applicationContext, true);
	}

	private void testGet_IAdapterManager(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IAdapterManager.class, expected);
	}

	public void testGet_IAdapterManager() {
		testGet_IAdapterManager(osgiContext, true);
		testGet_IAdapterManager(applicationContext, true);
	}

	public void testGet_Adapter() {
		testGet(osgiContext, Adapter.class, false);
		testGet(applicationContext, Adapter.class, true);
	}

	private void testGet_IPreferencesService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IPreferencesService.class, expected);
	}

	public void testGet_IPreferencesService() {
		testGet_IPreferencesService(osgiContext, true);
		testGet_IPreferencesService(applicationContext, true);
	}

	private void testGet_ISchedulingExecutor(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, ISchedulingExecutor.SERVICE_NAME, expected);
	}

	public void testGet_ISchedulingExecutor() {
		testGet_ISchedulingExecutor(osgiContext, true);
		testGet_ISchedulingExecutor(applicationContext, true);
	}

	private void testGet_IEventBroker(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IEventBroker.class, expected);
	}

	public void testGet_IEventBroker() {
		testGet_IEventBroker(osgiContext, false);
		testGet_IEventBroker(applicationContext, true);
	}

	private void testGet_IContributionFactory(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IContributionFactory.class, expected);
	}

	public void testGet_IContributionFactory() {
		testGet_IContributionFactory(osgiContext, false);
		testGet_IContributionFactory(applicationContext, true);
	}

	// private void testGet_IContributionFactorySpi(
	// IEclipseContext eclipseContext, boolean expected) {
	// testGet(eclipseContext, IContributionFactorySpi.class, expected);
	// }
	//
	// public void testGet_IContributionFactorySpi() {
	// testGet_IContributionFactorySpi(osgiContext, false);
	// testGet_IContributionFactorySpi(applicationContext, true);
	// }

	private void testGet_IExceptionHandler(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IExceptionHandler.class, expected);
	}

	public void testGet_IExceptionHandler() {
		testGet_IExceptionHandler(osgiContext, false);
		testGet_IExceptionHandler(applicationContext, true);
	}

	private void testGet_Logger(IEclipseContext eclipseContext, boolean expected) {
		testGet(eclipseContext, Logger.class, expected);
	}

	public void testGet_Logger() {
		testGet_Logger(osgiContext, false);
		testGet_Logger(applicationContext, true);
	}

	private void testGet_IStylingEngine(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IStylingEngine.SERVICE_NAME, expected);
	}

	public void testGet_IStylingEngine() {
		testGet_IStylingEngine(osgiContext, false);
		testGet_IStylingEngine(applicationContext, false);
	}

	private void testGet_IPresentationEngine(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, IPresentationEngine.SERVICE_NAME, expected);
	}

	public void testGet_IPresentationEngine() {
		testGet_IPresentationEngine(osgiContext, false);
		testGet_IPresentationEngine(applicationContext, false);
	}

	private void testGet_ECommandService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, ECommandService.class, expected);
	}

	public void testGet_ECommandService() {
		testGet_ECommandService(osgiContext, false);
		testGet_ECommandService(applicationContext, true);
	}

	private void testGet_EHandlerService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, EHandlerService.class, expected);
	}

	public void testGet_EHandlerService() {
		testGet_EHandlerService(osgiContext, false);
		testGet_EHandlerService(applicationContext, true);
	}

	// private void testGet_EBindingService(IEclipseContext eclipseContext,
	// boolean expected) {
	// testGet(eclipseContext, EBindingService.class, expected);
	// }
	//
	// public void testGet_EBindingService() {
	// testGet_EBindingService(osgiContext, false);
	// testGet_EBindingService(applicationContext, true);
	// }

	private void testGet_EContextService(IEclipseContext eclipseContext,
			boolean expected) {
		testGet(eclipseContext, EContextService.class, expected);
	}

	public void testGet_EContextService() {
		testGet_EContextService(osgiContext, true);
		testGet_EContextService(applicationContext, true);
	}

}
