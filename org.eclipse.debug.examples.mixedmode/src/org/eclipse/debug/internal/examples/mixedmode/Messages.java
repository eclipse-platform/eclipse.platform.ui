/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.examples.mixedmode;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.debug.internal.examples.mixedmode.messages"; //$NON-NLS-1$
	public static String FooDuplicateTab_0;
	public static String FooDuplicateTab_1;
	public static String FooDuplicateTab_2;
	public static String FooDuplicateTab_3;
	public static String FooPiggyBackTab_0;
	public static String FooPiggyBackTab_1;
	public static String FooPiggyBackTab_2;
	public static String FooPiggyBackTab_3;
	public static String FooTab_0;
	public static String FooTab_1;
	public static String FooTab_2;
	public static String FooTab_3;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
