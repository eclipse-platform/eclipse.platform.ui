/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;


import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The central class for access to the Eclipse Platform User Interface. 
 * This class cannot be instantiated; all functionality is provided by 
 * static methods.
 * 
 * Features provided:
 * <ul>
 * <li>access to the workbench.</li>
 * </ul>
 * <p>
 *
 * @see IWorkbench
 */
public final class PlatformUI {
	/**
	 * Identifies the workbench plugin.
	 */
	public static final String PLUGIN_ID = "org.eclipse.ui";//$NON-NLS-1$
	
	private static IWorkbench instance; 
/**
 * Block instantiation.
 */
private PlatformUI() {
}
/**
 * Returns the workbench interface.
 */
public static IWorkbench getWorkbench() {
	if (instance == null) {
		instance = WorkbenchPlugin.getDefault().getWorkbench();
	}
	return instance;
}
}
