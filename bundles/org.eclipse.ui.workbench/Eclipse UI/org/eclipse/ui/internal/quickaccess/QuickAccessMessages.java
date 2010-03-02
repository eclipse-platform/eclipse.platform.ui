/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 * 
 */
public class QuickAccessMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.quickaccess.messages"; //$NON-NLS-1$
	public static String QuickAccess_Perspectives;
	public static String QuickAccess_Commands;
	public static String QuickAccess_Properties;
	public static String QuickAccess_Editors;
	public static String QuickAccess_Menus;
	public static String QuickAccess_New;
	public static String QuickAccess_Preferences;
	public static String QuickAccess_Previous;
	public static String QuickAccess_Views;
	public static String QuickAccess_PressKeyToShowAllMatches;
	public static String QuickAccess_StartTypingToFindMatches;
	public static String QuickAccess_AvailableCategories;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, QuickAccessMessages.class);
	}

	private QuickAccessMessages() {
	}
}
