/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.core.internal.expressions;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;

import org.eclipse.core.runtime.Plugin;

public class ExpressionPlugin extends Plugin {

	private static ExpressionPlugin fgDefault;

	static BundleListener fgBundleListener;

	private BundleContext fBundleContext;

	public ExpressionPlugin() {
		fgDefault = this;
	}

	public static ExpressionPlugin getDefault() {
		return fgDefault;
	}

	public static String getPluginId() {
		return "org.eclipse.core.expressions"; //$NON-NLS-1$
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fBundleContext= context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (fgBundleListener != null)
			context.removeBundleListener(fgBundleListener);
		fgBundleListener= null;
		super.stop(context);
	}

	public BundleContext getBundleContext() {
		return fBundleContext;
	}
}
