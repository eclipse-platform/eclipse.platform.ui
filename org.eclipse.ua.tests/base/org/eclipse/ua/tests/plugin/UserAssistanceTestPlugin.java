/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.plugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class UserAssistanceTestPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static UserAssistanceTestPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public UserAssistanceTestPlugin() {
		super();
		plugin = this;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static UserAssistanceTestPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * @return the id of this plugin
	 */
	public static String getPluginId() {
		return "org.eclipse.ua.tests";
	}
}
