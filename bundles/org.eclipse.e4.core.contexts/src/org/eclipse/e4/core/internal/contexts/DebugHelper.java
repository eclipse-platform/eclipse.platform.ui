/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

public final class DebugHelper {

	private static final String PLUGIN_NAME = "org.eclipse.e4.core.contexts"; //$NON-NLS-1$
	private static final String OPTION_DEBUG = PLUGIN_NAME + "/debug"; //$NON-NLS-1$
	private static final String OPTION_DEBUG_NAMES = OPTION_DEBUG + "/names"; //$NON-NLS-1$
	private static final String OPTION_DEBUG_LISTENERS = OPTION_DEBUG + "/listeners"; //$NON-NLS-1$

	public static boolean DEBUG = false;
	public static boolean DEBUG_NAMES = false;
	public static boolean DEBUG_LISTENERS = false;

	static {
		try {
			// use qualified name for ContextsActivator to ensure that it won't be loaded outside of this block
			DEBUG = org.eclipse.e4.core.internal.contexts.osgi.ContextsActivator.getBooleanDebugOption(OPTION_DEBUG, false);
			DEBUG_NAMES = org.eclipse.e4.core.internal.contexts.osgi.ContextsActivator.getBooleanDebugOption(OPTION_DEBUG_NAMES, false);
			DEBUG_LISTENERS = org.eclipse.e4.core.internal.contexts.osgi.ContextsActivator.getBooleanDebugOption(OPTION_DEBUG_LISTENERS, false);
		} catch (NoClassDefFoundError noClass) {
			// no OSGi - OK
		}
	}

}
