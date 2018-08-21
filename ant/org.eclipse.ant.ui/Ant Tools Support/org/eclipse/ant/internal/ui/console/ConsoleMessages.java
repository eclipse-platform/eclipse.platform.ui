/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.console;

import org.eclipse.osgi.util.NLS;

public class ConsoleMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ant.internal.ui.console.ConsoleMessages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ConsoleMessages.class);
	}

	public static String JavacMarkerCreator_0;
}
