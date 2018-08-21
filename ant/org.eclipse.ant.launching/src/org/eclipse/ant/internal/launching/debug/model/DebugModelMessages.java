/**********************************************************************
.
. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.launching.debug.model;

import org.eclipse.osgi.util.NLS;

public class DebugModelMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ant.internal.launching.debug.model.DebugModelMessages";//$NON-NLS-1$

	public static String AntDebugTarget_0;

	public static String AntLineBreakpoint_0;
	public static String AntThread_0;
	public static String AntThread_1;
	public static String AntThread_2;
	public static String AntThread_3;
	public static String AntThread_4;

	public static String AntProperties_1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, DebugModelMessages.class);
	}
}