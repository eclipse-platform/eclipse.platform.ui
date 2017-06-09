/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.examples.contributions;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @since 3.3
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ui.examples.contributions"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DEBUG_COMMANDS = getDebugOption("/trace/commands"); //$NON-NLS-1$
	}
	
	/**
	 * Piggy back of off org.eclipse.ui/trace/commands
	 */
	public static boolean DEBUG_COMMANDS = false;
	
	private static boolean getDebugOption(String option) {
        return "true".equalsIgnoreCase(Platform.getDebugOption(PlatformUI.PLUGIN_ID + option)); //$NON-NLS-1$
    }
}
