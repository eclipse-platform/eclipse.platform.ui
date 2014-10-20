/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.osgi.util.NLS;

/**
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

	public static String ToolBarManagerRenderer_MenuCloseText;
	public static String ToolBarManagerRenderer_MenuRestoreText;

	private static final String BUNDLE_NAME = "org.eclipse.e4.ui.workbench.renderers.swt.messages";//$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
