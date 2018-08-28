/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties;

import org.eclipse.osgi.util.NLS;

/**
 */
public class PropertiesMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.properties.messages";//$NON-NLS-1$

	// package: org.eclipse.ui.views.properties

	// ==============================================================================
	// Properties View
	// ==============================================================================

	/** */
	public static String Categories_text;
	/** */
	public static String Categories_toolTip;

	/** */
	public static String Columns_text;
	/** */
	public static String Columns_toolTip;

	/** */
	public static String CopyProperty_text;

	/** */
	public static String Defaults_text;
	/** */
	public static String Defaults_toolTip;

	/** */
	public static String Filter_text;
	/** */
	public static String Filter_toolTip;

	/** */
	public static String Selection_description;
	/** */
	public static String Pin_text;
	/** */
	public static String Pin_toolTip;

	/** */
	public static String PropertyViewer_property;
	/** */
	public static String PropertyViewer_value;
	/** */
	public static String PropertyViewer_misc;

	/** */
	public static String CopyToClipboardProblemDialog_title;
	/** */
	public static String CopyToClipboardProblemDialog_message;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, PropertiesMessages.class);
	}
}