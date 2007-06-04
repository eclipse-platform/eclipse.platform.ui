/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for the code in this plugin.
 * 
 * @since 3.3
 */
public class ContributionMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.examples.contributions.messages";//$NON-NLS-1$
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ContributionMessages.class);
	}
	public static String InfoView_countElements;
	public static String SampleHandler_hello_msg;
	public static String SampleHandler_plugin_name;
}
