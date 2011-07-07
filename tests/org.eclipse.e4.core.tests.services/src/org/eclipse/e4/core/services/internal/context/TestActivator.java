/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;

public class TestActivator implements BundleActivator {

	public static BundleContext bundleContext;
	public TestActivator() {
	}

	public void start(BundleContext aContext) throws Exception {
		bundleContext = aContext;
	}
	public void stop(BundleContext aContext) throws Exception {
		bundleContext = null;
	}

}
