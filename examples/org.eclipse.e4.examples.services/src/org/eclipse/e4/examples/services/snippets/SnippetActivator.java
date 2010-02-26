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

package org.eclipse.e4.examples.services.snippets;

import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;

public class SnippetActivator implements BundleActivator {

	public static BundleContext bundleContext;
	
	public SnippetActivator() {
	}

	public void start(BundleContext aContext) throws Exception {
		bundleContext = aContext;
		SnippetSetup.initializeServices();
	}
	public void stop(BundleContext aContext) throws Exception {
		bundleContext = null;
	}
}