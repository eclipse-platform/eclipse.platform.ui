/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;

/**
 * 
 */
public class TestActivator implements BundleActivator {

	private static BundleContext context;

	public TestActivator() {
		super();
	}
	
	public static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext context) throws Exception {
		TestActivator.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		TestActivator.context = null;
	}
}
