/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.intro;

import org.eclipse.osgi.util.NLS;

/**
 * The IntroMessages are the messages used in the intro support.
 */
public class IntroMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.intro.intro";//$NON-NLS-1$

	public static String Intro_could_not_create_part;
	public static String Intro_could_not_create_proxy;
	public static String Intro_could_not_create_descriptor;
	public static String Intro_action_text;
	public static String Intro_default_title;
	public static String Intro_missing_product_title;
	public static String Intro_missing_product_message;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, IntroMessages.class);
	}
}
