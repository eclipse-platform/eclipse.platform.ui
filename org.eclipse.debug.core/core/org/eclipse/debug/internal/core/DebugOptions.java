/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.util.Hashtable;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.osgi.framework.BundleContext;

/**
 * Access to debug options.
 * 
 * @since 3.3
 */
public class DebugOptions implements DebugOptionsListener {
	
	// debug option flags
	public static boolean DEBUG = false;
	public static boolean DEBUG_COMMANDS = false;
	public static boolean DEBUG_EVENTS = false;

	static final String DEBUG_FLAG = "org.eclipse.debug.core/debug"; //$NON-NLS-1$
	static final String DEBUG_FLAG_COMMANDS = "org.eclipse.debug.core/debug/commands"; //$NON-NLS-1$
	static final String DEBUG_FLAG_EVENTS = "org.eclipse.debug.core/debug/events"; //$NON-NLS-1$
	
	/**
	 * Constructor
	 * @param context the bundle context
	 */
	public DebugOptions(BundleContext context) {
		Hashtable props = new Hashtable(2);
		props.put(org.eclipse.osgi.service.debug.DebugOptions.LISTENER_SYMBOLICNAME, DebugPlugin.getUniqueIdentifier());
		context.registerService(DebugOptionsListener.class.getName(), this, props);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.osgi.service.debug.DebugOptionsListener#optionsChanged(org.eclipse.osgi.service.debug.DebugOptions)
	 */
	public void optionsChanged(org.eclipse.osgi.service.debug.DebugOptions options) {
		DEBUG = options.getBooleanOption(DEBUG_FLAG, false);
		DEBUG_COMMANDS = DEBUG & options.getBooleanOption(DEBUG_FLAG_COMMANDS, false);
		DEBUG_EVENTS = DEBUG & options.getBooleanOption(DEBUG_FLAG_EVENTS, false);
	}
}
