/**********************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.externaltools.internal.model;

import org.eclipse.osgi.util.NLS;

public class ExternalToolsModelMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.externaltools.internal.model.ExternalToolsModelMessages";//$NON-NLS-1$
    
	public static String ImageDescriptorRegistry_Allocating_image_for_wrong_display_1;
	public static String BuilderUtils_5;
	public static String BuilderUtils_6;
	public static String BuilderUtils_7;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ExternalToolsModelMessages.class);
	}
}