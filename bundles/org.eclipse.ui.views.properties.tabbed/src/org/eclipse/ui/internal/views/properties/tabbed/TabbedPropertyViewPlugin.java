/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The common ui properties plug-in.
 * 
 * @author Anthony Hunter
 */
public class TabbedPropertyViewPlugin
	extends AbstractUIPlugin {

	private static TabbedPropertyViewPlugin plugin;

    /**
     * Constructor for TabbedPropertyViewPlugin.
     */
	public TabbedPropertyViewPlugin() {
		super();
		plugin = this;
	}

    /**
     * Retrieve the plug-in class for this plug-in.
     * @return the plug-in class for this plug-in.
     */
	public static TabbedPropertyViewPlugin getPlugin() {
		return plugin;
	}
}
