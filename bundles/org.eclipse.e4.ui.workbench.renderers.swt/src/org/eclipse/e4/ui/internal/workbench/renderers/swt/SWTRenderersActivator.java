/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator
 */
public class SWTRenderersActivator implements BundleActivator {

	/**
	 * The bundle associated this plug-in
	 */
	private static BundleContext bundleContext;

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		bundleContext = null;
	}

	static BundleContext getContext() {
		return bundleContext;
	}

}
