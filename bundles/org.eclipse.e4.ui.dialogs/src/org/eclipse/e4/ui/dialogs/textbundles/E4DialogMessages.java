/*******************************************************************************
 * Copyright (c) 2014, 2025 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Initial API and implementation based on WorkbenchSWTMessages
 *******************************************************************************/
package org.eclipse.e4.ui.dialogs.textbundles;

import org.eclipse.osgi.util.NLS;

/**
 * Based on org.eclipse.ui.internal.WorkbenchMessages
 */
public class E4DialogMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.dialogs.textbundles.messages";//$NON-NLS-1$

	public static String FilteredTable_AccessibleListenerFiltered;
	public static String FilteredTable_FilterMessage;
	public static String FilteredTree_AccessibleListenerClearButton;
	public static String FilteredTree_ClearToolTip;
	public static String FilteredTree_FilterMessage;
	public static String FilteredTree_AccessibleListenerFiltered;

	// --- Help Menu ---
	public static String AboutDialog_shellTitle;
	public static String AboutDialog_defaultProductName;

	// ==============================================================================
	// About Dialog
	// ==============================================================================
	public static String AboutPluginsDialog_state_installed;
	public static String AboutPluginsDialog_state_resolved;
	public static String AboutPluginsDialog_state_starting;
	public static String AboutPluginsDialog_state_stopping;
	public static String AboutPluginsDialog_state_uninstalled;
	public static String AboutPluginsDialog_state_active;
	public static String AboutPluginsDialog_state_unknown;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, E4DialogMessages.class);
	}
}
