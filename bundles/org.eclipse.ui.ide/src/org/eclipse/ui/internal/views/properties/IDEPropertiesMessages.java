/**********************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.views.properties;

import org.eclipse.osgi.util.NLS;

public class IDEPropertiesMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.properties.ideMessages";//$NON-NLS-1$

	// ==============================================================================
	// Properties View - IDE-specific strings
	// ==============================================================================

	public static String PropertySource_notLocal;
	public static String PropertySource_notFound;
	public static String PropertySource_readOnly;
	public static String PropertySource_undefinedPathVariable;
	public static String PropertySource_fileNotExist;

	public static String IResourcePropertyConstants_name;
	public static String IResourcePropertyConstants_path;
	public static String IResourcePropertyConstants_editable;
	public static String IResourcePropertyConstants_derived;
	public static String IResourcePropertyConstants_size;
	public static String IResourcePropertyConstants_lastModified;
	public static String IResourcePropertyConstants_info;
	public static String IResourcePropertyConstants_location;
	public static String IResourcePropertyConstants_resolvedLocation;
	public static String IResourcePropertyConstants_linked;
	public static String ResourceProperty_false;
	public static String ResourceProperty_true;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, IDEPropertiesMessages.class);
	}
}