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
package org.eclipse.debug.internal.ui;

import org.eclipse.debug.ui.IDebugUIConstants;

 
public interface IInternalDebugUIConstants {
	
	public static final String DIALOGSTORE_LASTEXTJAR= "org.eclipse.debug.ui.lastextjar"; //$NON-NLS-1$
		
	//Folders
	public static final String ID_NAVIGATOR_FOLDER_VIEW= "org.eclipse.debug.internal.ui.NavigatorFolderView"; //$NON-NLS-1$
	public static final String ID_TOOLS_FOLDER_VIEW= "org.eclipse.debug.internal.ui.ToolsFolderView"; //$NON-NLS-1$
	public static final String ID_CONSOLE_FOLDER_VIEW= "org.eclipse.debug.internal.ui.ConsoleFolderView"; //$NON-NLS-1$
	public static final String ID_OUTLINE_FOLDER_VIEW= "org.eclipse.debug.internal.ui.OutlineFolderView"; //$NON-NLS-1$

	//Current stack frame instruction pointer
	public static final String INSTRUCTION_POINTER= "org.eclipse.debug.ui.instructionPointer"; //$NON-NLS-1$
	
	// tool images
	public static final String IMG_LCL_COLLAPSE_ALL = "IMG_LCL_COLLAPSE_ALL"; //$NON-NLS-1$
	
	// disabled local tool images
	public static final String IMG_DLCL_CLEAR= "IMG_DLCL_CLEAR"; //$NON-NLS-1$
	public static final String IMG_DLCL_LOCK= "IMG_DLCL_LOCK"; //$NON-NLS-1$
	public static final String IMG_DLCL_DETAIL_PANE= "IMG_DLCL_DETAIL_PANE"; //$NON-NLS-1$
	public static final String IMG_DLCL_CHANGE_VARIABLE_VALUE= "IMG_DLCL_CHANGE_VARIABLE_VALUE"; //$NON-NLS-1$
	public static final String IMG_DLCL_TYPE_NAMES= "IMG_DLCL_TYPE_NAMES"; //$NON-NLS-1$
	public static final String IMG_DLCL_VARIABLES_CONTENT_PROVIDERS= "IMG_DLCL_VARIABLES_CONTENT_PROVIDERS"; //$NON-NLS-1$
	public static final String IMG_DLCL_DETAIL_PANE_UNDER= "IMG_DLCL_DETAIL_PANE_UNDER"; //$NON-NLS-1$
	public static final String IMG_DLCL_DETAIL_PANE_RIGHT= "IMG_DLCL_DETAIL_PANE_RIGHT"; //$NON-NLS-1$
	public static final String IMG_DLCL_DETAIL_PANE_HIDE= "IMG_DLCL_DETAIL_PANE_HIDE"; //$NON-NLS-1$
	public static final String IMG_DLCL_COLLAPSE_ALL = "IMG_DLCL_COLLAPSE_ALL"; //$NON-NLS-1$
	
	// enabled local tool images	
	public static final String IMG_ELCL_CLEAR= "IMG_ELCL_CLEAR"; //$NON-NLS-1$
	public static final String IMG_ELCL_LOCK= "IMG_ELCL_LOCK"; //$NON-NLS-1$
	public static final String IMG_ELCL_DETAIL_PANE= "IMG_ELCL_DETAIL_PANE"; //$NON-NLS-1$
	public static final String IMG_ELCL_CHANGE_VARIABLE_VALUE= "IMG_ELCL_CHANGE_VARIABLE_VALUE"; //$NON-NLS-1$
	public static final String IMG_ELCL_TYPE_NAMES= "IMG_ELCL_TYPE_NAMES"; //$NON-NLS-1$
	public static final String IMG_ELCL_VARIABLES_CONTENT_PROVIDERS= "IMG_ELCL_VARIABLES_CONTENT_PROVIDERS"; //$NON-NLS-1$
	public static final String IMG_ELCL_DETAIL_PANE_UNDER= "IMG_ELCL_DETAIL_PANE_UNDER"; //$NON-NLS-1$
	public static final String IMG_ELCL_DETAIL_PANE_RIGHT= "IMG_ELCL_DETAIL_PANE_RIGHT"; //$NON-NLS-1$
	public static final String IMG_ELCL_DETAIL_PANE_HIDE= "IMG_ELCL_DETAIL_PANE_HIDE"; //$NON-NLS-1$
	public static final String IMG_ELCL_COLLAPSE_ALL = "IMG_ELCL_COLLAPSE_ALL"; //$NON-NLS-1$
		
	// object images
	public static final String IMG_OBJS_INSTRUCTION_POINTER_TOP = "IMG_OBJS_INSTRUCTION_POINTER_TOP"; //$NON-NLS-1$
	public static final String IMG_OBJS_INSTRUCTION_POINTER = "IMG_OBJS_INSTRUCTION_POINTER"; //$NON-NLS-1$
	public static final String IMG_OBJS_COMMON_TAB = "IMG_OBJS_COMMON_TAB"; //$NON-NLS-1$
	public static final String IMG_OBJS_REFRESH_TAB = "IMG_OBJS_REFRESH_TAB"; //$NON-NLS-1$
	public static final String IMG_OBJS_PERSPECTIVE_TAB = "IMG_OBJS_PERSPECTIVE_TAB"; //$NON-NLS-1$
	
	// internal preferenes
	/**
	 * XML for perspective settings - see PerspectiveManager.
	 * @since 3.0
	 */
	public static final String PREF_LAUNCH_PERSPECTIVES = IDebugUIConstants.PLUGIN_ID + ".PREF_LAUNCH_PERSPECTIVES"; //$NON-NLS-1$
	 
	/** Transparent overlay image identifier. */
	public static final String IMG_OVR_TRANSPARENT = "IMG_OVR_TRANSPARENT";  //$NON-NLS-1$
	
	/**
	 * Editor Id for the "Source Not Found" editor
	 */
	public static final String ID_SOURCE_NOT_FOUND_EDITOR = "org.eclipse.debug.ui.NoSourceFoundEditor"; //$NON-NLS-1$
	
	/**
	 * The name of the font to use for the Console. 
	 * Since 2.1 this font is managed via the workbench font preference page
	 * and is no longer stored as a debug preference.
	 */ 
 	public static final String CONSOLE_FONT= "org.eclipse.debug.ui.ConsoleFont"; //$NON-NLS-1$
	
	/**
	 * The name of the font to use for detail panes. This font is managed via
	 * the workbench font preference page.
	 * 
	 * @since 2.1
	 */ 
	public static final String DETAIL_PANE_FONT= "org.eclipse.debug.ui.DetailPaneFont"; //$NON-NLS-1$
	
}
