/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.bindings.tests;


import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.e4.ui.bindings.tests"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private IEclipseContext appContext;
	private IEclipseContext serviceContext;

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		serviceContext = EclipseContextFactory.getServiceContext(context);
		appContext = serviceContext.createChild();
		addLogService(appContext);
	}

	private void addLogService(IEclipseContext context) {
		context.set(LogService.class.getName(), new LogService() {

			@Override
			public void log(int level, String message) {
				System.out.println(level + ": " + message);
			}

			@Override
			public void log(int level, String message, Throwable exception) {
				System.out.println(level + ": " + message);
				if (exception != null) {
					exception.printStackTrace();
				}
			}

			@Override
			public void log(ServiceReference sr, int level, String message) {
				// TODO Auto-generated method stub

			}

			@Override
			public void log(ServiceReference sr, int level, String message,
					Throwable exception) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		serviceContext.dispose();
		plugin = null;
	}

	public IEclipseContext getGlobalContext() {
		return appContext;
	}

}
