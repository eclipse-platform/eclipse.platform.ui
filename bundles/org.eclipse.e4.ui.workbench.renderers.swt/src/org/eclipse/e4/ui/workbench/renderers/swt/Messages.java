/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *     Patrk Suzzi <psuzzi@gmail.com> - Bug 515253
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.osgi.util.NLS;

/**
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

	public static String ToolBarManagerRenderer_MenuCloseText;
	public static String ToolBarManagerRenderer_MenuRestoreText;

	public static String ToolBarManagerRenderer_LockToolbars;
	public static String ToolBarManagerRenderer_UnlockToolbars;
	public static String ToolBarManagerRenderer_ToggleLockToolbars;

	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.workbench.renderers.swt.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
