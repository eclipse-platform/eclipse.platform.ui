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
package org.eclipse.ui.internal;

/**
 * This class defines constants for looking up resources that are available
 * only within the Eclipse UI and Eclipse UI Standard Components projects.
 *
 * See ISharedGraphicConstants for a description of how the keys are named.
 */
public interface IWorkbenchGraphicConstants {

	/*** Constants for Images ***/

	// toolbar buttons for wizards

	public final static String IMG_CTOOL_IMPORT_WIZ = "IMG_CTOOL_IMPORT_WIZ"; //$NON-NLS-1$
	public final static String IMG_CTOOL_IMPORT_WIZ_HOVER = "IMG_CTOOL_IMPORT_WIZ_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_IMPORT_WIZ_DISABLED = "IMG_CTOOL_IMPORT_WIZ_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_EXPORT_WIZ = "IMG_CTOOL_EXPORT_WIZ"; //$NON-NLS-1$
	public final static String IMG_CTOOL_EXPORT_WIZ_HOVER = "IMG_CTOOL_EXPORT_WIZ_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_EXPORT_WIZ_DISABLED = "IMG_CTOOL_EXPORT_WIZ_DISABLED"; //$NON-NLS-1$

	// other toolbar buttons
	public final static String IMG_CTOOL_BUILD_EXEC = "IMG_CTOOL_BUILD_EXEC" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_BUILD_EXEC_HOVER = "IMG_CTOOL_BUILD_EXEC_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_BUILD_EXEC_DISABLED = "IMG_CTOOL_BUILD_EXEC_DISABLED" ; //$NON-NLS-1$
			
	public final static String IMG_CTOOL_CLOSE_EDIT = "IMG_CTOOL_CLOSE_EDIT" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_CLOSE_EDIT_HOVER = "IMG_CTOOL_CLOSE_EDIT_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_CLOSE_EDIT_DISABLED = "IMG_CTOOL_CLOSE_EDIT_DISABLED" ; //$NON-NLS-1$

	public final static String IMG_CTOOL_SAVE_EDIT = "IMG_CTOOL_SAVE_EDIT" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SAVE_EDIT_HOVER = "IMG_CTOOL_SAVE_EDIT_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SAVE_EDIT_DISABLED = "IMG_CTOOL_SAVE_EDIT_DISABLED" ; //$NON-NLS-1$

	public final static String IMG_CTOOL_SAVEAS_EDIT = "IMG_CTOOL_SAVEAS_EDIT" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SAVEAS_EDIT_HOVER = "IMG_CTOOL_SAVEAS_EDIT_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SAVEAS_EDIT_DISABLED = "IMG_CTOOL_SAVEAS_EDIT_DISABLED" ; //$NON-NLS-1$

	public final static String IMG_CTOOL_SAVEALL_EDIT = "IMG_CTOOL_SAVEALL_EDIT" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SAVEALL_EDIT_HOVER = "IMG_CTOOL_SAVEALL_EDIT_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SAVEALL_EDIT_DISABLED = "IMG_CTOOL_SAVEALL_EDIT_DISABLED" ; //$NON-NLS-1$
	
	public final static String IMG_CTOOL_PRINT_EDIT = "IMG_CTOOL_PRINT_EDIT" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_PRINT_EDIT_HOVER = "IMG_CTOOL_PRINT_EDIT_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_PRINT_EDIT_DISABLED = "IMG_CTOOL_PRINT_EDIT_DISABLED" ; //$NON-NLS-1$

	public final static String IMG_CTOOL_SEARCH_SRC = "IMG_CTOOL_SEARCH_SRC" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SEARCH_SRC_HOVER = "IMG_CTOOL_SEARCH_SRC_HOVER" ; //$NON-NLS-1$
	public final static String IMG_CTOOL_SEARCH_SRC_DISABLED = "IMG_CTOOL_SEARCH_SRC_DISABLED" ; //$NON-NLS-1$

	public final static String IMG_CTOOL_REFRESH_NAV = "IMG_CTOOL_REFRESH_NAV"; //$NON-NLS-1$
	public final static String IMG_CTOOL_REFRESH_NAV_HOVER = "IMG_CTOOL_REFRESH_NAV_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_REFRESH_NAV_DISABLED = "IMG_CTOOL_REFRESH_NAV_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_STOP_NAV = "IMG_CTOOL_STOP_NAV"; //$NON-NLS-1$
	public final static String IMG_CTOOL_STOP_NAV_HOVER = "IMG_CTOOL_STOP_NAV_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_STOP_NAV_DISABLED = "IMG_CTOOL_STOP_NAV_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_HOME_NAV = "IMG_CTOOL_HOME_NAV"; //$NON-NLS-1$
	public final static String IMG_CTOOL_HOME_NAV_HOVER = "IMG_CTOOL_HOME_NAV_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_HOME_NAV_DISABLED = "IMG_CTOOL_HOME_NAV_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_NEXT_NAV = "IMG_CTOOL_NEXT_NAV"; //$NON-NLS-1$

	public final static String IMG_CTOOL_PREVIOUS_NAV = "IMG_CTOOL_PREVIOUS_NAV"; //$NON-NLS-1$
	
	public final static String IMG_CTOOL_NEW_PAGE = "IMG_CTOOL_NEW_PAGE"; //$NON-NLS-1$
	public final static String IMG_CTOOL_NEW_PAGE_HOVER = "IMG_CTOOL_NEW_PAGE_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_NEW_PAGE_DISABLED = "IMG_CTOOL_NEW_PAGE_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_SET_PAGE = "IMG_CTOOL_SET_PAGE"; //$NON-NLS-1$
	public final static String IMG_CTOOL_SET_PAGE_HOVER = "IMG_CTOOL_SET_PAGE_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_SET_PAGE_DISABLED = "IMG_CTOOL_SET_PAGE_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_NEW_WND = "IMG_CTOOL_NEW_WND"; //$NON-NLS-1$
	public final static String IMG_CTOOL_NEW_WND_HOVER = "IMG_CTOOL_NEW_WND_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_NEW_WND_DISABLED = "IMG_CTOOL_NEW_WND_DISABLED"; //$NON-NLS-1$

	public final static String IMG_CTOOL_PIN_EDITOR = "IMG_CTOOL_PIN_EDITOR"; //$NON-NLS-1$
	public final static String IMG_CTOOL_PIN_EDITOR_HOVER = "IMG_CTOOL_PIN_EDITOR_HOVER"; //$NON-NLS-1$
	public final static String IMG_CTOOL_PIN_EDITOR_DISABLED = "IMG_CTOOL_PIN_EDITOR_DISABLED"; //$NON-NLS-1$
		
	public final static String IMG_CTOOL_DEF_PERSPECTIVE = "IMG_CTOOL_DEF_PERSPECTIVE"; //$NON-NLS-1$
	public final static String IMG_CTOOL_DEF_PERSPECTIVE_HOVER = "IMG_CTOOL_DEF_PERSPECTIVE_HOVER"; //$NON-NLS-1$
	
	// local toolbars
	public final static String IMG_LCL_CLOSE_VIEW = "IMG_LCL_CLOSE_VIEW" ; //$NON-NLS-1$
	public final static String IMG_LCL_CLOSE_VIEW_HOVER = "IMG_LCL_CLOSE_VIEW_HOVER"; //$NON-NLS-1$	
	public final static String IMG_LCL_PIN_VIEW = "IMG_LCL_PIN_VIEW" ; //$NON-NLS-1$
	public final static String IMG_LCL_PIN_VIEW_HOVER = "IMG_LCL_PIN_VIEW_HOVER"; //$NON-NLS-1$
	public final static String IMG_LCL_MIN_VIEW = "IMG_LCL_MIN_VIEW" ; //$NON-NLS-1$
	public final static String IMG_LCL_MIN_VIEW_HOVER = "IMG_LCL_MIN_VIEW_HOVER"; //$NON-NLS-1$
	public final static String IMG_LCL_GOTOOBJ_TSK = "IMG_LCL_GOTOOBJ_TSK"; //$NON-NLS-1$
	public final static String IMG_LCL_ADDTSK_TSK = "IMG_LCL_ADDTSK_TSK"; //$NON-NLS-1$
	public final static String IMG_LCL_REMTSK_TSK = "IMG_LCL_REMTSK_TSK"; //$NON-NLS-1$
	public final static String IMG_LCL_SHOWCOMPLETE_TSK = "IMG_LCL_SHOWCOMPLETE_TSK"; //$NON-NLS-1$
	public final static String IMG_LCL_VIEW_MENU = "IMG_LCL_VIEW_MENU"; //$NON-NLS-1$
	public final static String IMG_LCL_VIEW_MENU_HOVER = "IMG_LCL_VIEW_MENU_HOVER"; //$NON-NLS-1$
	public final static String IMG_LCL_SELECTED_MODE = "IMG_LCL_SELECTED_MODE"; //$NON-NLS-1$
	public final static String IMG_LCL_SHOWCHILD_MODE = "IMG_LCL_SHOWCHILD_MODE"; //$NON-NLS-1$

	public final static String IMG_LCL_TREE_MODE = "IMG_LCL_TREE_MODE"; //$NON-NLS-1$
		
	public final static String IMG_LCL_DEFAULTS_PS = "IMG_LCL_DEFAULTS_PS"; //$NON-NLS-1$
	public final static String IMG_LCL_FILTER_PS = "IMG_LCL_FILTER_PS"; //$NON-NLS-1$
	public final static String IMG_LCL_REMBKMRK_TSK = "IMG_LCL_REMBKMRK_TSK"; //$NON-NLS-1$

	public final static String IMG_LCL_SHOWSYNC_RN = "IMG_LCL_SHOWSYNC_RN"; //$NON-NLS-1$

	//wizard images
	public final static String IMG_WIZBAN_NEW_WIZ = "IMG_WIZBAN_NEW_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_NEWPRJ_WIZ = "IMG_WIZBAN_NEWPRJ_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_NEWFOLDER_WIZ = "IMG_WIZBAN_NEWFOLDER_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_NEWFILE_WIZ = "IMG_WIZBAN_NEWFILE_WIZ"; //$NON-NLS-1$

	public final static String IMG_WIZBAN_IMPORT_WIZ = "IMG_WIZBAN_IMPORT_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_IMPORTDIR_WIZ = "IMG_WIZBAN_IMPORTDIR_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_IMPORTZIP_WIZ = "IMG_WIZBAN_IMPORTZIP_WIZ"; //$NON-NLS-1$

	public final static String IMG_WIZBAN_EXPORT_WIZ = "IMG_WIZBAN_EXPORT_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_EXPORTDIR_WIZ =  "IMG_WIZBAN_EXPORTDIR_WIZ"; //$NON-NLS-1$
	public final static String IMG_WIZBAN_EXPORTZIP_WIZ = "IMG_WIZBAN_EXPORTZIP_WIZ"; //$NON-NLS-1$
	
	public final static String IMG_WIZBAN_RESOURCEWORKINGSET_WIZ = "IMG_WIZBAN_EXPORTZIP_WIZ"; //$NON-NLS-1$	

	public final static String IMG_VIEW_DEFAULTVIEW_MISC = "IMG_VIEW_DEFAULTVIEW_MISC"; //$NON-NLS-1$
	
	// dialog images
	public final static String IMG_DLGBAN_SAVEAS_DLG = "IMG_DLGBAN_SAVEAS_DLG"; //$NON-NLS-1$

	// task objects
	public final static String IMG_OBJS_HPRIO_TSK = "IMG_OBJS_HPRIO_TSK"; //$NON-NLS-1$
	public final static String IMG_OBJS_MPRIO_TSK = "IMG_OBJS_MPRIO_TSK"; //$NON-NLS-1$
	public final static String IMG_OBJS_LPRIO_TSK = "IMG_OBJS_LPRIO_TSK"; //$NON-NLS-1$
	public final static String IMG_OBJS_COMPLETE_TSK = "IMG_OBJS_COMPLETE_TSK"; //$NON-NLS-1$
	public final static String IMG_OBJS_INCOMPLETE_TSK = "IMG_OBJS_INCOMPLETE_TSK"; //$NON-NLS-1$
	public final static String IMG_OBJS_BRKPT_TSK = "IMG_OBJS_BRKPT_TSK"; //$NON-NLS-1$
		
	// product
	public final static String IMG_OBJS_DEFAULT_PROD = "IMG_OBJS_DEFAULT_PROD"; //$NON-NLS-1$

	// welcome
	public final static String IMG_OBJS_WELCOME_ITEM = "IMG_OBJS_WELCOME_ITEM"; //$NON-NLS-1$
	public final static String IMG_OBJS_WELCOME_BANNER = "IMG_OBJS_WELCOME_BANNER"; //$NON-NLS-1$

	// synchronization indicator objects
	public final static String IMG_OBJS_WBET_STAT = "IMG_OBJS_WBET_STAT"; //$NON-NLS-1$
	public final static String IMG_OBJS_SBET_STAT = "IMG_OBJS_SBET_STAT"; //$NON-NLS-1$
	public final static String IMG_OBJS_CONFLICT_STAT = "IMG_OBJS_CONFLICT_STAT"; //$NON-NLS-1$

	// local content indicator objects
	public final static String IMG_OBJS_NOTLOCAL_STAT = "IMG_OBJS_NOTLOCAL_STAT"; //$NON-NLS-1$
	public final static String IMG_OBJS_LOCAL_STAT = "IMG_OBJS_LOCAL_STAT"; //$NON-NLS-1$
	public final static String IMG_OBJS_FILLLOCAL_STAT = "IMG_OBJS_FILLLOCAL_STAT"; //$NON-NLS-1$

	// part direct manipulation objects
	public final static String IMG_OBJS_DND_LEFT_SOURCE = "IMG_OBJS_DND_LEFT_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_LEFT_MASK = "IMG_OBJS_DND_LEFT_MASK"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_RIGHT_SOURCE = "IMG_OBJS_DND_RIGHT_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_RIGHT_MASK = "IMG_OBJS_DND_RIGHT_MASK"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_TOP_SOURCE = "IMG_OBJS_DND_TOP_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_TOP_MASK = "IMG_OBJS_DND_TOP_MASK"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_BOTTOM_SOURCE = "IMG_OBJS_DND_BOTTOM_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_BOTTOM_MASK = "IMG_OBJS_DND_BOTTOM_MASK"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_INVALID_SOURCE = "IMG_OBJS_DND_INVALID_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_INVALID_MASK = "IMG_OBJS_DND_INVALID_MASK"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_STACK_SOURCE = "IMG_OBJS_DND_STACK_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_STACK_MASK = "IMG_OBJS_DND_STACK_MASK"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_OFFSCREEN_SOURCE = "IMG_OBJS_DND_OFFSCREEN_SOURCE"; //$NON-NLS-1$
	public final static String IMG_OBJS_DND_OFFSCREEN_MASK = "IMG_OBJS_DND_OFFSCREEN_MASK"; //$NON-NLS-1$
}
