/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.incubator;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 *
 */
public class IncubatorMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.incubator.messages"; //$NON-NLS-1$
	public static String CtrlEAction_Perspectives;
	public static String CtrlEAction_Commands;
	public static String CtrlEAction_Editors;
	public static String CtrlEAction_Menus;
	public static String CtrlEAction_New;
	public static String CtrlEAction_Preferences;
	public static String CtrlEAction_Views;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, IncubatorMessages.class);
	}
	private IncubatorMessages() {
	}
}
