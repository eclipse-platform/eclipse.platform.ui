package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * This class defines constants for looking up resources that are available
 * only within the Eclipse UI and Eclipse UI Standard Components projects.
 *
 * See ISharedGraphicConstants for a description of how the keys are named.
 */
public interface IWorkbenchGraphicConstants {

	/*** Constants for Images ***/

	// toolbar buttons for wizards

	public final static String IMG_CTOOL_NEW_WIZ = "IMG_CTOOL_NEW_WIZ";
	public final static String IMG_CTOOL_NEW_WIZ_HOVER = "IMG_CTOOL_NEW_WIZ_HOVER";
	public final static String IMG_CTOOL_NEW_WIZ_DISABLED = "IMG_CTOOL_NEW_WIZ_DIS";
	
	public final static String IMG_CTOOL_IMPORT_WIZ = "IMG_CTOOL_IMPORT_WIZ";
	public final static String IMG_CTOOL_IMPORT_WIZ_HOVER = "IMG_CTOOL_IMPORT_WIZ_HOVER";
	public final static String IMG_CTOOL_IMPORT_WIZ_DISABLED = "IMG_CTOOL_IMPORT_WIZ_DISABLED";

	public final static String IMG_CTOOL_EXPORT_WIZ = "IMG_CTOOL_EXPORT_WIZ";
	public final static String IMG_CTOOL_EXPORT_WIZ_HOVER = "IMG_CTOOL_EXPORT_WIZ_HOVER";
	public final static String IMG_CTOOL_EXPORT_WIZ_DISABLED = "IMG_CTOOL_EXPORT_WIZ_DISABLED";

	// other toolbar buttons
	public final static String IMG_CTOOL_BUILD_EXEC = "IMG_CTOOL_BUILD_EXEC" ;
	public final static String IMG_CTOOL_BUILD_EXEC_HOVER = "IMG_CTOOL_BUILD_EXEC_HOVER" ;
	public final static String IMG_CTOOL_BUILD_EXEC_DISABLED = "IMG_CTOOL_BUILD_EXEC_DISABLED" ;
			
	public final static String IMG_CTOOL_CLOSE_EDIT = "IMG_CTOOL_CLOSE_EDIT" ;
	public final static String IMG_CTOOL_CLOSE_EDIT_HOVER = "IMG_CTOOL_CLOSE_EDIT_HOVER" ;
	public final static String IMG_CTOOL_CLOSE_EDIT_DISABLED = "IMG_CTOOL_CLOSE_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_SAVE_EDIT = "IMG_CTOOL_SAVE_EDIT" ;
	public final static String IMG_CTOOL_SAVE_EDIT_HOVER = "IMG_CTOOL_SAVE_EDIT_HOVER" ;
	public final static String IMG_CTOOL_SAVE_EDIT_DISABLED = "IMG_CTOOL_SAVE_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_SAVEAS_EDIT = "IMG_CTOOL_SAVEAS_EDIT" ;
	public final static String IMG_CTOOL_SAVEAS_EDIT_HOVER = "IMG_CTOOL_SAVEAS_EDIT_HOVER" ;
	public final static String IMG_CTOOL_SAVEAS_EDIT_DISABLED = "IMG_CTOOL_SAVEAS_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_SAVEALL_EDIT = "IMG_CTOOL_SAVEALL_EDIT" ;
	public final static String IMG_CTOOL_SAVEALL_EDIT_HOVER = "IMG_CTOOL_SAVEALL_EDIT_HOVER" ;
	public final static String IMG_CTOOL_SAVEALL_EDIT_DISABLED = "IMG_CTOOL_SAVEALL_EDIT_DISABLED" ;
	
	public final static String IMG_CTOOL_UNDO_EDIT = "IMG_CTOOL_UNDO_EDIT" ;
	public final static String IMG_CTOOL_UNDO_EDIT_HOVER = "IMG_CTOOL_UNDO_EDIT_HOVER" ;
	public final static String IMG_CTOOL_UNDO_EDIT_DISABLED = "IMG_CTOOL_UNDO_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_REDO_EDIT = "IMG_CTOOL_REDO_EDIT" ;
	public final static String IMG_CTOOL_REDO_EDIT_HOVER = "IMG_CTOOL_REDO_EDIT_HOVER" ;
	public final static String IMG_CTOOL_REDO_EDIT_DISABLED= "IMG_CTOOL_REDO_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_CUT_EDIT = "IMG_CTOOL_CUT_EDIT" ;
	public final static String IMG_CTOOL_CUT_EDIT_HOVER = "IMG_CTOOL_CUT_EDIT_HOVER" ;
	public final static String IMG_CTOOL_CUT_EDIT_DISABLED = "IMG_CTOOL_CUT_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_COPY_EDIT = "IMG_CTOOL_COPY_EDIT" ;
	public final static String IMG_CTOOL_COPY_EDIT_HOVER = "IMG_CTOOL_COPY_EDIT_HOVER" ;
	public final static String IMG_CTOOL_COPY_EDIT_DISABLED = "IMG_CTOOL_COPY_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_DELETE_EDIT = "IMG_CTOOL_DELETE_EDIT" ;
	public final static String IMG_CTOOL_DELETE_EDIT_HOVER = "IMG_CTOOL_DELETE_EDIT_HOVER" ;
	public final static String IMG_CTOOL_DELETE_EDIT_DISABLED = "IMG_CTOOL_DELETE_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_PASTE_EDIT = "IMG_CTOOL_PASTE_EDIT" ;
	public final static String IMG_CTOOL_PASTE_EDIT_HOVER = "IMG_CTOOL_PASTE_EDIT_HOVER" ;
	public final static String IMG_CTOOL_PASTE_EDIT_DISABLED= "IMG_CTOOL_PASTE_EDIT_DISABLED" ;

	public final static String IMG_CTOOL_SEARCH_SRC = "IMG_CTOOL_SEARCH_SRC" ;
	public final static String IMG_CTOOL_SEARCH_SRC_HOVER = "IMG_CTOOL_SEARCH_SRC_HOVER" ;
	public final static String IMG_CTOOL_SEARCH_SRC_DISABLED = "IMG_CTOOL_SEARCH_SRC_DISABLED" ;


	public final static String IMG_CTOOL_REFRESH_NAV = "IMG_CTOOL_REFRESH_NAV";
	public final static String IMG_CTOOL_REFRESH_NAV_HOVER = "IMG_CTOOL_REFRESH_NAV_HOVER";
	public final static String IMG_CTOOL_REFRESH_NAV_DISABLED = "IMG_CTOOL_REFRESH_NAV_DISABLED";

	public final static String IMG_CTOOL_FORWARD_NAV = "IMG_CTOOL_FORWARD_NAV";
	public final static String IMG_CTOOL_FORWARD_NAV_HOVER = "IMG_CTOOL_FORWARD_NAV_HOVER";
	public final static String IMG_CTOOL_FORWARD_NAV_DISABLED = "IMG_CTOOL_FORWARD_NAV_DISABLED";

	public final static String IMG_CTOOL_BACKWARD_NAV = "IMG_CTOOL_BACKWARD_NAV";
	public final static String IMG_CTOOL_BACKWARD_NAV_HOVER = "IMG_CTOOL_BACKWARD_NAV_HOVER";
	public final static String IMG_CTOOL_BACKWARD_NAV_DISABLED = "IMG_CTOOL_BACKWARD_NAV_DISABLED";

	public final static String IMG_CTOOL_STOP_NAV = "IMG_CTOOL_STOP_NAV";
	public final static String IMG_CTOOL_STOP_NAV_HOVER = "IMG_CTOOL_STOP_NAV_HOVER";
	public final static String IMG_CTOOL_STOP_NAV_DISABLED = "IMG_CTOOL_STOP_NAV_DISABLED";

	public final static String IMG_CTOOL_HOME_NAV = "IMG_CTOOL_HOME_NAV";
	public final static String IMG_CTOOL_HOME_NAV_HOVER = "IMG_CTOOL_HOME_NAV_HOVER";
	public final static String IMG_CTOOL_HOME_NAV_DISABLED = "IMG_CTOOL_HOME_NAV_DISABLED";

	public final static String IMG_CTOOL_NEW_PAGE = "IMG_CTOOL_NEW_PAGE";
	public final static String IMG_CTOOL_NEW_PAGE_HOVER = "IMG_CTOOL_NEW_PAGE_HOVER";
	public final static String IMG_CTOOL_NEW_PAGE_DISABLED = "IMG_CTOOL_NEW_PAGE_DISABLED";

	public final static String IMG_CTOOL_SET_PAGE = "IMG_CTOOL_SET_PAGE";
	public final static String IMG_CTOOL_SET_PAGE_HOVER = "IMG_CTOOL_SET_PAGE_HOVER";
	public final static String IMG_CTOOL_SET_PAGE_DISABLED = "IMG_CTOOL_SET_PAGE_DISABLED";

	public final static String IMG_CTOOL_NEW_WND = "IMG_CTOOL_NEW_WND";
	public final static String IMG_CTOOL_NEW_WND_HOVER = "IMG_CTOOL_NEW_WND_HOVER";
	public final static String IMG_CTOOL_NEW_WND_DISABLED = "IMG_CTOOL_NEW_WND_DISABLED";
		
	public final static String IMG_CTOOL_DEF_PERSPECTIVE = "IMG_CTOOL_DEF_PERSPECTIVE";
	public final static String IMG_CTOOL_DEF_PERSPECTIVE_HOVER = "IMG_CTOOL_DEF_PERSPECTIVE_HOVER";
	
	// local toolbars
	public final static String IMG_LCL_CLOSE_VIEW = "IMG_LCL_CLOSE_VIEW" ;
	public final static String IMG_LCL_PIN_VIEW = "IMG_LCL_PIN_VIEW" ;
	public final static String IMG_LCL_MIN_VIEW = "IMG_LCL_MIN_VIEW" ;
	public final static String IMG_LCL_GOTOOBJ_TSK = "IMG_LCL_GOTOOBJ_TSK";
	public final static String IMG_LCL_ADDTSK_TSK = "IMG_LCL_ADDTSK_TSK";
	public final static String IMG_LCL_REMTSK_TSK = "IMG_LCL_REMTSK_TSK";
	public final static String IMG_LCL_SHOWCOMPLETE_TSK = "IMG_LCL_SHOWCOMPLETE_TSK";
	public final static String IMG_LCL_VIEW_MENU = "IMG_LCL_VIEW_MENU";
	public final static String IMG_LCL_SELECTED_MODE = "IMG_LCL_SELECTED_MODE";
	public final static String IMG_LCL_SHOWCHILD_MODE = "IMG_LCL_SHOWCHILD_MODE";

	public final static String IMG_LCL_TREE_MODE = "IMG_LCL_TREE_MODE";
		
	public final static String IMG_LCL_DEFAULTS_PS = "IMG_LCL_DEFAULTS_PS";
	public final static String IMG_LCL_FILTER_PS = "IMG_LCL_FILTER_PS";
	public final static String IMG_LCL_REMBKMRK_TSK = "IMG_LCL_REMBKMRK_TSK";

	public final static String IMG_LCL_SHOWSYNC_RN = "IMG_LCL_SHOWSYNC_RN";

	//wizard images
	public final static String IMG_WIZBAN_NEW_WIZ = "IMG_WIZBAN_NEW_WIZ";
	public final static String IMG_WIZBAN_NEWPRJ_WIZ = "IMG_WIZBAN_NEWPRJ_WIZ";
	public final static String IMG_WIZBAN_NEWFOLDER_WIZ = "IMG_WIZBAN_NEWFOLDER_WIZ";
	public final static String IMG_WIZBAN_NEWFILE_WIZ = "IMG_WIZBAN_NEWFILE_WIZ";

	public final static String IMG_WIZBAN_IMPORT_WIZ = "IMG_WIZBAN_IMPORT_WIZ";
	public final static String IMG_WIZBAN_IMPORTDIR_WIZ = "IMG_WIZBAN_IMPORTDIR_WIZ";
	public final static String IMG_WIZBAN_IMPORTZIP_WIZ = "IMG_WIZBAN_IMPORTZIP_WIZ";

	public final static String IMG_WIZBAN_EXPORT_WIZ = "IMG_WIZBAN_EXPORT_WIZ";
	public final static String IMG_WIZBAN_EXPORTDIR_WIZ =  "IMG_WIZBAN_EXPORTDIR_WIZ";
	public final static String IMG_WIZBAN_EXPORTZIP_WIZ = "IMG_WIZBAN_EXPORTZIP_WIZ";

	public final static String IMG_VIEW_DEFAULTVIEW_MISC = "IMG_VIEW_DEFAULTVIEW_MISC";
	


	// task objects
	public final static String IMG_OBJS_HPRIO_TSK = "IMG_OBJS_HPRIO_TSK";
	public final static String IMG_OBJS_MPRIO_TSK = "IMG_OBJS_MPRIO_TSK";
	public final static String IMG_OBJS_LPRIO_TSK = "IMG_OBJS_LPRIO_TSK";
	public final static String IMG_OBJS_COMPLETE_TSK = "IMG_OBJS_COMPLETE_TSK";
	public final static String IMG_OBJS_INCOMPLETE_TSK = "IMG_OBJS_INCOMPLETE_TSK";
	public final static String IMG_OBJS_BRKPT_TSK = "IMG_OBJS_BRKPT_TSK";
		
	// product
	public final static String IMG_OBJS_DEFAULT_PROD = "IMG_OBJS_DEFAULT_PROD";

	// welcome
	public final static String IMG_OBJS_WELCOME_ITEM = "IMG_OBJS_WELCOME_ITEM";
	public final static String IMG_OBJS_WELCOME_BANNER = "IMG_OBJS_WELCOME_BANNER";

	// synchronization indicator objects
	public final static String IMG_OBJS_WBET_STAT = "IMG_OBJS_WBET_STAT";
	public final static String IMG_OBJS_SBET_STAT = "IMG_OBJS_SBET_STAT";
	public final static String IMG_OBJS_CONFLICT_STAT = "IMG_OBJS_CONFLICT_STAT";

	// local content indicator objects
	public final static String IMG_OBJS_NOTLOCAL_STAT = "IMG_OBJS_NOTLOCAL_STAT";
	public final static String IMG_OBJS_LOCAL_STAT = "IMG_OBJS_LOCAL_STAT";
	public final static String IMG_OBJS_FILLLOCAL_STAT = "IMG_OBJS_FILLLOCAL_STAT";

	// part direct manipulation objects
	public final static String IMG_OBJS_DND_LEFT_SOURCE = "IMG_OBJS_DND_LEFT_SOURCE";
	public final static String IMG_OBJS_DND_LEFT_MASK = "IMG_OBJS_DND_LEFT_MASK";
	public final static String IMG_OBJS_DND_RIGHT_SOURCE = "IMG_OBJS_DND_RIGHT_SOURCE";
	public final static String IMG_OBJS_DND_RIGHT_MASK = "IMG_OBJS_DND_RIGHT_MASK";
	public final static String IMG_OBJS_DND_TOP_SOURCE = "IMG_OBJS_DND_TOP_SOURCE";
	public final static String IMG_OBJS_DND_TOP_MASK = "IMG_OBJS_DND_TOP_MASK";
	public final static String IMG_OBJS_DND_BOTTOM_SOURCE = "IMG_OBJS_DND_BOTTOM_SOURCE";
	public final static String IMG_OBJS_DND_BOTTOM_MASK = "IMG_OBJS_DND_BOTTOM_MASK";
	public final static String IMG_OBJS_DND_INVALID_SOURCE = "IMG_OBJS_DND_INVALID_SOURCE";
	public final static String IMG_OBJS_DND_INVALID_MASK = "IMG_OBJS_DND_INVALID_MASK";
	public final static String IMG_OBJS_DND_STACK_SOURCE = "IMG_OBJS_DND_STACK_SOURCE";
	public final static String IMG_OBJS_DND_STACK_MASK = "IMG_OBJS_DND_STACK_MASK";
	public final static String IMG_OBJS_DND_OFFSCREEN_SOURCE = "IMG_OBJS_DND_OFFSCREEN_SOURCE";
	public final static String IMG_OBJS_DND_OFFSCREEN_MASK = "IMG_OBJS_DND_OFFSCREEN_MASK";
}
