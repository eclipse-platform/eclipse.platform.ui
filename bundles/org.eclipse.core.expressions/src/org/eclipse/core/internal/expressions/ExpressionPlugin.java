/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import org.eclipse.core.runtime.Plugin;

import org.osgi.framework.BundleContext;

public class ExpressionPlugin extends Plugin {
	
	private static ExpressionPlugin fgDefault;
	
	private BundleContext fBundleContext;
	
	public ExpressionPlugin() {
		fgDefault= this;
	}	

	public static ExpressionPlugin getDefault() {
		return fgDefault;
	}
	
	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fBundleContext= context;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	public BundleContext getBundleContext() {
		return fBundleContext;
	}
}
