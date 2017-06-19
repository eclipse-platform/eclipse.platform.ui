/*******************************************************************************
 * Copyright (c) 2013, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 431667, 440893
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 450209
 *******************************************************************************/
package org.eclipse.e4.core.commands.tests;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestActivator implements BundleActivator {

	private static TestActivator plugin = null;
	private IEclipseContext appContext;
	private IEclipseContext serviceContext;

	public static TestActivator getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		serviceContext = EclipseContextFactory.getServiceContext(context);
		appContext = serviceContext.createChild();
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
