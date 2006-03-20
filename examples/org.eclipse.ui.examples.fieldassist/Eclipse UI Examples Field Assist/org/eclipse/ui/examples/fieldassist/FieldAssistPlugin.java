/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.fieldassist;

import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class FieldAssistPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static FieldAssistPlugin plugin;
	
	// Our own content assist decorator (which adds the key binding)
	static String DEC_CONTENTASSIST = "org.eclipse.ui.examples.fieldassist.contentAssistDecoration";
	
	/**
	 * The constructor.
	 */
	public FieldAssistPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared plugin instance
	 */
	public static FieldAssistPlugin getDefault() {
		return plugin;
	}
}

