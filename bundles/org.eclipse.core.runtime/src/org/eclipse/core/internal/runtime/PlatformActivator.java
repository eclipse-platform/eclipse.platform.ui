/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Julian Chen - fix for bug #92572, jclRM
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.app.CommandLineArgs;
import org.osgi.framework.*;

/**
 * Activator for the Eclipse runtime.
 */
public class PlatformActivator extends Plugin implements BundleActivator {
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext runtimeContext) throws Exception {
		PlatformActivator.context = runtimeContext;
		InternalPlatform.getDefault().start(runtimeContext);
		startAppContainer();
		InternalPlatform.getDefault().setRuntimeInstance(this);
		super.start(runtimeContext);
	}

	public void stop(BundleContext runtimeContext) {
		// Stop the platform orderly.		
		InternalPlatform.getDefault().stop(runtimeContext);
		InternalPlatform.getDefault().setRuntimeInstance(null);
	}

	private void startAppContainer() {
		// just using a class out of app admin to force it to lazy-start
		CommandLineArgs.getApplicationArgs();
	}
}
