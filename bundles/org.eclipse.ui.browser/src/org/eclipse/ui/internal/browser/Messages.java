/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.osgi.util.NLS;
/**
 * Translated messages.
 */
public class Messages extends NLS {
	
	static {
		NLS.initializeMessages("org.eclipse.ui.internal.browser.Messages", Messages.class);
	}

    public static String BrowserText_title;
    public static String BrowserText_link;
    public static String BrowserText_tooltip;
    public static String BrowserText_dtitle;
    public static String BrowserText_text;
    public static String BrowserText_button_collapse;
    public static String BrowserText_button_expand;
}