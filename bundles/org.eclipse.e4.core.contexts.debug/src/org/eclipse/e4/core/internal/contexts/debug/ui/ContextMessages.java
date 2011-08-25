/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import org.eclipse.osgi.util.NLS;

public class ContextMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.e4.core.internal.contexts.debug.ui.messages"; //$NON-NLS-1$

	// Context tracing view
	public static String dataTab;
	public static String allocationsTab;
	public static String linksTab;
	public static String keyColumn;
	public static String valueColumn;
	public static String linksLabel;
	public static String allocationsLabel;
	public static String contextTreeLabel;
	public static String showFunctions;
	public static String showCached;
	public static String leaksGroup;
	public static String snapshotButton;
	public static String diffButton;
	public static String refreshGroup;
	public static String autoUpdateButton;
	public static String diffDialogTitle;
	public static String diffDialogMessage;
	public static String noDiffMsg;
	public static String contextGCed;
	public static String targetButtonTooltip;
	public static String refreshButtonTooltip;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, ContextMessages.class);
	}
}