package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
