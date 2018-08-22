/*******************************************************************************
 * Copyright (c) 2011, 2014 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.minmax;

import org.eclipse.osgi.util.NLS;

/**
 */
public class Messages extends NLS {

	public static String TrimStack_EmptyStackTooltip;
	public static String TrimStack_SharedAreaTooltip;
	public static String TrimStack_CloseText;
	public static String TrimStack_DefaultOrientationItem;
	public static String TrimStack_RestoreText;
	public static String TrimStack_Horizontal;
	public static String TrimStack_OrientationMenu;
	public static String TrimStack_Vertical;
	public static String TrimStack_Show_In_Original_Location;

	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.workbench.addons.minmax.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
