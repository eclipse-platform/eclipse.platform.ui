/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.cheatsheets;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class CheatSheetTestPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static CheatSheetTestPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public CheatSheetTestPlugin() {
		super();
		plugin = this;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static CheatSheetTestPlugin getDefault() {
		return plugin;
	}
}
