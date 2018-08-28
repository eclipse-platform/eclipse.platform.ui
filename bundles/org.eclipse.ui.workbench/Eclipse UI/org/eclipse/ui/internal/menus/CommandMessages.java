/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal.menus;

import org.eclipse.osgi.util.NLS;

/**
 *
 * @since 3.5
 *
 */
public class CommandMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.menus.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, CommandMessages.class);
	}

	public static String Tooltip_Accelerator;
}
