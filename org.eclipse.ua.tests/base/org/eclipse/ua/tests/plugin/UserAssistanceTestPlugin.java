/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
