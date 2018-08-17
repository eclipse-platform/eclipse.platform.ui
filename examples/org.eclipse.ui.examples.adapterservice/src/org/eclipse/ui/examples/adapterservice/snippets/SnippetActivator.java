/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.examples.adapterservice.snippets;

import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;

public class SnippetActivator implements BundleActivator {

	public static BundleContext bundleContext;
	
	public SnippetActivator() {
	}

	@Override
	public void start(BundleContext aContext) throws Exception {
		bundleContext = aContext;
		SnippetSetup.initializeServices();
	}
	@Override
	public void stop(BundleContext aContext) throws Exception {
		bundleContext = null;
	}
}