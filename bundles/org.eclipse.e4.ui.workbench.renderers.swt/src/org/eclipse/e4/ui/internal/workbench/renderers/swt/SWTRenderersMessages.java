/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
 *     Simon Scholz <scholzsimon@arcor.de - Bug 429729
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.eclipse.osgi.util.NLS;

/**
 * SWTRenderers message catalog
 */
public class SWTRenderersMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.internal.workbench.renderers.swt.messages"; //$NON-NLS-1$

	public static String choosePartsToSaveTitle;
	public static String choosePartsToSave;

	public static String menuClose;
	public static String menuCloseOthers;
	public static String menuCloseAll;
	public static String menuCloseRight;
	public static String menuCloseLeft;
	public static String menuDetach;

	public static String viewMenu;

	static {
		// load message values from bundle file
		reloadMessages();
	}

	public static void reloadMessages() {
		NLS.initializeMessages(BUNDLE_NAME, SWTRenderersMessages.class);
	}
}