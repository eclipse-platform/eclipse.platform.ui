/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.osgi.util.NLS;

public class LaunchViewMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.debug.internal.ui.views.launch.LaunchViewMessages";//$NON-NLS-1$

	public static String Breadcrumb_NoActiveContext;
    public static String breadcrumb_LabelPending;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, LaunchViewMessages.class);
	}
}
