/*******************************************************************************
 * Copyright (c) 2018 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Angelo Zerr <angelo.zerr@gmail.com> - [minimap] Initialize minimap view - Bug 535450
 *******************************************************************************/
package org.eclipse.ui.internal.views.minimap;

import org.eclipse.osgi.util.NLS;

/**
 * MinimapMessages is the message class for the messages used in the minimap.
 *
 */
public class MinimapMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.minimap.MinimapMessages";//$NON-NLS-1$

	// ==============================================================================
	// Minimap View
	// ==============================================================================
	/** */
	public static String MinimapViewNoMinimap;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, MinimapMessages.class);
	}
}