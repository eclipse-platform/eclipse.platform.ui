/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
