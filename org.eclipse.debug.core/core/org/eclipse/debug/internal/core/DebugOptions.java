/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.Platform;

/**
 * Access to debug options.
 * 
 * @since 3.3
 */
public class DebugOptions {

	// debug option flags
	public static boolean DEBUG = false;
	public static boolean DEBUG_COMMANDS = false;
	public static boolean DEBUG_EVENTS = false;

	public static void initDebugOptions() {
		DEBUG = "true".equals(Platform.getDebugOption("org.eclipse.debug.core/debug"));  //$NON-NLS-1$//$NON-NLS-2$
		DEBUG_COMMANDS = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.debug.core/debug/commands")); //$NON-NLS-1$
		DEBUG_EVENTS = DEBUG && "true".equals( //$NON-NLS-1$
				 Platform.getDebugOption("org.eclipse.debug.core/debug/events")); //$NON-NLS-1$
	}
}
