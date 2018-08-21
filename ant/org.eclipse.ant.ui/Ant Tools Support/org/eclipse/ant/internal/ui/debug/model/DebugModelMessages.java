/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
package org.eclipse.ant.internal.ui.debug.model;

import org.eclipse.osgi.util.NLS;

public class DebugModelMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ant.internal.ui.debug.model.DebugModelMessages";//$NON-NLS-1$

	public static String AntDebugModelPresentation_0;
	public static String AntDebugModelPresentation_1;
	public static String AntDebugModelPresentation_2;
	public static String AntDebugModelPresentation_3;
	public static String AntDebugModelPresentation_4;
	public static String AntDebugModelPresentation_5;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DebugModelMessages.class);
	}
}