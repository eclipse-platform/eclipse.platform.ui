/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;

/**
 * This class provides convenience access to many of the resources required
 * by the workbench. The class stores some images as descriptors, and
 * some are stored as real Images in the registry.  This is a pure
 * speed-space tradeoff.  The trick for users of this class is that
 * images obtained from the registry (using getImage()), don't require
 * disposal since they are shared, while images obtained using
 * getImageDescriptor() will require disposal.  Consult the declareImages
 * method to see if a given image is declared as a registry image or
 * just as a descriptor.  If you change an image from being stored
 * as a descriptor to a registry image, or vice-versa, make sure to
 * check all users of the image to ensure they are calling
 * the correct getImage... method and handling disposal correctly.
 *
 *  Images:
 *      - use getImage(key) to access cached images from the registry.
 *      - Less common images are found by calling getImageDescriptor(key)
 *          where key can be found in IWorkbenchGraphicConstants
 *
 *      This class initializes the image registry by declaring all of the required
 *      graphics. This involves creating image descriptors describing
 *      how to create/find the image should it be needed.
 *      The image is not actually allocated until requested.
 *
 *      Some Images are also made available to other plugins by being
 *      placed in the descriptor table of the SharedImages class.
 *
 *      Where are the images?
 *          The images (typically gifs) are found the plugins install directory
 *
 *      How to add a new image
 *          Place the gif file into the appropriate directories.
 *          Add a constant to IWorkbenchGraphicConstants following the conventions
 *          Add the declaration to this file
 */
public /*final*/ class WorkbenchImages {
	private static Map descriptors = new HashMap();
	private static ImageRegistry imageRegistry;
	
	//Key: ImageDescriptor
	//Value: Image
	private static ReferenceCounter imageCache = new ReferenceCounter();

	/* Declare Common paths */

	// Subdirectory (under the package containing this class) where 16 color images are
	private static final URL URL_BASIC = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();

	public final static String ICONS_PATH = "icons/full/";//$NON-NLS-1$
	
	private final static String PATH_CTOOL = ICONS_PATH+"ctool16/"; //Colored toolbar icons - hover.//$NON-NLS-1$
	private final static String PATH_ETOOL = ICONS_PATH+"etool16/"; //Enabled toolbar icons.//$NON-NLS-1$
	private final static String PATH_DTOOL = ICONS_PATH+"dtool16/"; //Disabled toolbar icons.//$NON-NLS-1$
	
	private final static String PATH_CLOCALTOOL = ICONS_PATH+"clcl16/"; //Colored local toolbar icons - hover.//$NON-NLS-1$
	private final static String PATH_ELOCALTOOL = ICONS_PATH+"elcl16/"; //Enabled local toolbar icons.//$NON-NLS-1$
	private final static String PATH_DLOCALTOOL = ICONS_PATH+"dlcl16/"; //Disabled local toolbar icons.//$NON-NLS-1$
	
	private final static String PATH_CVIEW = ICONS_PATH+"cview16/"; //Colored view icons.//$NON-NLS-1$
	private final static String PATH_EVIEW = ICONS_PATH+"eview16/"; //View icons//$NON-NLS-1$

	
	//private final static String PATH_PROD = ICONS_PATH+"prod/";	//Product images
	private final static String PATH_OBJECT = ICONS_PATH+"obj16/"; //Model object icons//$NON-NLS-1$
	private final static String PATH_DND = ICONS_PATH+"dnd/";  //DND icons//$NON-NLS-1$
	private final static String PATH_WIZBAN = ICONS_PATH+"wizban/"; //Wizard icons//$NON-NLS-1$
	
	//private final static String PATH_STAT = ICONS_PATH+"stat/";
	//private final static String PATH_MISC = ICONS_PATH+"misc/";
	//private final static String PATH_OVERLAY = ICONS_PATH+"ovr16/";
	
/**
 * Returns the image cache used internally by the workbench.
 */
public static ReferenceCounter getImageCache() {
	return imageCache;
}
/**
 * Declare an ImageDescriptor in the descriptor table.
 * @param key   The key to use when registering the image
 * @param path  The path where the image can be found. This path is relative to where
 *              this plugin class is found (i.e. typically the packages directory)
 */
private final static void declareImage(String key,String path) {
	URL url = null;
	try {
		url = new URL(URL_BASIC, path);
	} catch (MalformedURLException e) {
	}
	ImageDescriptor desc = ImageDescriptor.createFromURL(url);
	descriptors.put(key, desc);
}
private final static void declareImages() {

							
	// toolbar buttons for wizards
	declareRegistryImage(ISharedImages.IMG_TOOL_NEW_WIZARD, PATH_ETOOL+"new_wiz.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_NEW_WIZARD_HOVER, PATH_CTOOL+"new_wiz.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED, PATH_DTOOL+"new_wiz.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR, PATH_ETOOL+"pin_editor.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_HOVER, PATH_CTOOL+"pin_editor.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_DISABLED, PATH_DTOOL+"pin_editor.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ, PATH_CTOOL+"import_wiz.gif");//$NON-NLS-1$
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_HOVER, PATH_CTOOL+"import_wiz.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_DISABLED, PATH_DTOOL+"import_wiz.gif");
	
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ, PATH_CTOOL+"export_wiz.gif");//$NON-NLS-1$
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_HOVER, PATH_CTOOL+"export_wiz.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_DISABLED, PATH_DTOOL+"export_wiz.gif");

	// other toolbar buttons
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC, PATH_ETOOL+"build_exec.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_HOVER, PATH_CTOOL+"build_exec.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_DISABLED, PATH_DTOOL+"build_exec.gif");//$NON-NLS-1$

//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_CLOSE_EDIT, PATH_CTOOL+"close_edit.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_CLOSE_EDIT_HOVER, PATH_CTOOL+"close_edit.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_CLOSE_EDIT_DISABLED, PATH_DTOOL+"close_edit.gif");

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT, PATH_ETOOL+"save_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_HOVER, PATH_CTOOL+"save_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_DISABLED, PATH_DTOOL+"save_edit.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT, PATH_ETOOL+"saveas_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_HOVER, PATH_CTOOL+"saveas_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_DISABLED, PATH_DTOOL+"saveas_edit.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT, PATH_ETOOL+"saveall_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_HOVER, PATH_CTOOL+"saveall_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_DISABLED, PATH_DTOOL+"saveall_edit.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_UNDO, PATH_ETOOL+"undo_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_UNDO_HOVER, PATH_CTOOL+"undo_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_UNDO_DISABLED, PATH_DTOOL+"undo_edit.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_REDO, PATH_ETOOL+"redo_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_REDO_HOVER, PATH_CTOOL+"redo_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_REDO_DISABLED, PATH_DTOOL+"redo_edit.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_CUT, PATH_ETOOL+"cut_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_CUT_HOVER, PATH_CTOOL+"cut_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_CUT_DISABLED, PATH_DTOOL+"cut_edit.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_COPY, PATH_ETOOL+"copy_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_COPY_HOVER, PATH_CTOOL+"copy_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_COPY_DISABLED, PATH_DTOOL+"copy_edit.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_PASTE, PATH_ETOOL+"paste_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_PASTE_HOVER, PATH_CTOOL+"paste_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_PASTE_DISABLED, PATH_DTOOL+"paste_edit.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_DELETE, PATH_ETOOL+"delete_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_DELETE_HOVER, PATH_CTOOL+"delete_edit.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_DELETE_DISABLED, PATH_DTOOL+"delete_edit.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT, PATH_ETOOL+"print_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_HOVER, PATH_CTOOL+"print_edit.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_DISABLED, PATH_DTOOL+"print_edit.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC, PATH_ETOOL+"search_src.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_HOVER, PATH_CTOOL+"search_src.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_DISABLED, PATH_DTOOL+"search_src.gif");//$NON-NLS-1$

//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_REFRESH_NAV, PATH_CTOOL+"refresh_nav.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_REFRESH_NAV_HOVER, PATH_CTOOL+"refresh_nav.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_REFRESH_NAV_DISABLED, PATH_DTOOL+"refresh_nav.gif");

//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_STOP_NAV, PATH_CTOOL+"stop_nav.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_STOP_NAV_HOVER, PATH_CTOOL+"stop_nav.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_STOP_NAV_DISABLED, PATH_DTOOL+"stop_nav.gif");

	declareRegistryImage(ISharedImages.IMG_TOOL_FORWARD, PATH_ELOCALTOOL+"forward_nav.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_FORWARD_HOVER, PATH_CLOCALTOOL+"forward_nav.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_FORWARD_DISABLED, PATH_DLOCALTOOL+"forward_nav.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_BACK, PATH_ELOCALTOOL+"backward_nav.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_BACK_HOVER, PATH_CLOCALTOOL+"backward_nav.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_BACK_DISABLED, PATH_DLOCALTOOL+"backward_nav.gif");//$NON-NLS-1$

	declareRegistryImage(ISharedImages.IMG_TOOL_UP, PATH_ELOCALTOOL+"up_nav.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_UP_HOVER, PATH_CLOCALTOOL+"up_nav.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_TOOL_UP_DISABLED, PATH_DLOCALTOOL+"up_nav.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_HOME_NAV, PATH_CLOCALTOOL+"home_nav.gif");//$NON-NLS-1$
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_HOME_NAV_HOVER, PATH_CLOCALTOOL+"home_nav.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_HOME_NAV_DISABLED, PATH_DLOCALTOOL+"home_nav.gif");

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEXT_NAV, PATH_CTOOL+"next_nav.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_PREVIOUS_NAV, PATH_CTOOL+"prev_nav.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE, PATH_EVIEW+"new_persp.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE_HOVER, PATH_CVIEW+"new_persp.gif");//$NON-NLS-1$
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_PAGE_DISABLED, PATH_DTOOL+"new_page.gif");

//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SET_PAGE, PATH_CTOOL+"set_page.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SET_PAGE_HOVER, PATH_CTOOL+"set_page.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_SET_PAGE_DISABLED, PATH_DTOOL+"set_page.gif");

//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WND,PATH_CTOOL+"new_wnd.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WND_HOVER, PATH_CTOOL+"new_wnd.gif");
//	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WND_DISABLED, PATH_DTOOL+"new_wnd.gif");

	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE,PATH_EVIEW+"default_persp.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE_HOVER,PATH_CVIEW+"default_persp.gif");//$NON-NLS-1$
	
	// *TASKLIST* View icons are in the view code now.
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_GOTOOBJ_TSK, PATH_LOCALTOOL+"gotoobj_tsk.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_ADDTSK_TSK, PATH_LOCALTOOL+"addtsk_tsk.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_REMTSK_TSK, PATH_LOCALTOOL+"remtsk_tsk.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_SHOWCOMPLETE_TSK, PATH_LOCALTOOL+"showcomplete_tsk.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_SELECTED_MODE, PATH_LOCALTOOL+"selected_mode.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_SHOWCHILD_MODE, PATH_LOCALTOOL+"showchild_mode.gif");
	
	// *PROPERTY* View icons are in the view code now.
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_DEFAULTS_PS, PATH_LOCALTOOL+"defaults_ps.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_TREE_MODE, PATH_LOCALTOOL+"tree_mode.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_FILTER_PS, PATH_LOCALTOOL+"filter_ps.gif");
	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_REMBKMRK_TSK, PATH_LOCALTOOL+"rembkmrk_tsk.gif");

	//declareImage(IWorkbenchGraphicConstants.IMG_LCL_SHOWSYNC_RN, PATH_LOCALTOOL+"showsync_rn.gif");

	// view images
	declareImage(IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC, PATH_CVIEW+"defaultview_misc.gif");//$NON-NLS-1$

	// wizard images
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ, PATH_WIZBAN+"new_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN+"newprj_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_NEWFOLDER_WIZ, PATH_WIZBAN+"newfolder_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN+"newfile_wiz.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ, PATH_WIZBAN+"import_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORTDIR_WIZ, PATH_WIZBAN+"importdir_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORTZIP_WIZ, PATH_WIZBAN+"importzip_wiz.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ, PATH_WIZBAN+"export_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORTDIR_WIZ, PATH_WIZBAN+"exportdir_wiz.gif");//$NON-NLS-1$
	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORTZIP_WIZ, PATH_WIZBAN+"exportzip_wiz.gif");//$NON-NLS-1$

	declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ, PATH_WIZBAN+"workset_wiz.gif");//$NON-NLS-1$

	// dialog images
	declareImage(IWorkbenchGraphicConstants.IMG_DLGBAN_SAVEAS_DLG, PATH_WIZBAN+"saveas_dlg.gif");//$NON-NLS-1$

	/* Cache the commonly used ones */
	
	// object images -- these are also shared images.
	declareRegistryImage(ISharedImages.IMG_OBJ_FILE, PATH_OBJECT+"file_obj.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJ_FOLDER, PATH_OBJECT+"fldr_obj.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJ_PROJECT, PATH_OBJECT+"prj_obj.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT+"cprj_obj.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJ_ELEMENT, PATH_OBJECT+"elements_obj.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OPEN_MARKER, PATH_CLOCALTOOL+"gotoobj_tsk.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_DEF_VIEW, PATH_CVIEW+"defaultview_misc.gif");//$NON-NLS-1$

	// view toolbar images
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW, PATH_ELOCALTOOL+"close_view.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_HOVER, PATH_CLOCALTOOL+"close_view.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW, PATH_ELOCALTOOL+"pin_view.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW_HOVER, PATH_CLOCALTOOL+"pin_view.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW, PATH_ELOCALTOOL+"min_view.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_HOVER, PATH_CLOCALTOOL+"min_view.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU, PATH_ELOCALTOOL+"view_menu.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER, PATH_CLOCALTOOL+"view_menu.gif");//$NON-NLS-1$
	
	// task objects
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_HPRIO_TSK, PATH_OBJECT+"hprio_tsk.gif");
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_MPRIO_TSK, PATH_OBJECT+"mprio_tsk.gif");
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_LPRIO_TSK, PATH_OBJECT+"lprio_tsk.gif");

	declareRegistryImage(ISharedImages.IMG_OBJS_ERROR_TSK, PATH_OBJECT+"error_tsk.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJS_WARN_TSK, PATH_OBJECT+"warn_tsk.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJS_INFO_TSK, PATH_OBJECT+"info_tsk.gif");//$NON-NLS-1$
	declareRegistryImage(ISharedImages.IMG_OBJS_TASK_TSK, PATH_OBJECT+"taskmrk_tsk.gif");//$NON-NLS-1$
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_BRKPT_TSK, PATH_OBJECT+"brkptmrk_tsk.gif");
	declareRegistryImage(ISharedImages.IMG_OBJS_BKMRK_TSK, PATH_OBJECT+"bkmrk_tsk.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_COMPLETE_TSK, PATH_OBJECT+"complete_tsk.gif");   //$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_INCOMPLETE_TSK, PATH_OBJECT+"incomplete_tsk.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_WELCOME_ITEM, PATH_OBJECT+"welcome_item.gif");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_WELCOME_BANNER, PATH_OBJECT+"welcome_banner.gif");//$NON-NLS-1$

	// synchronization indicator objects
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_WBET_STAT, PATH_OVERLAY+"wbet_stat.gif");
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_SBET_STAT, PATH_OVERLAY+"sbet_stat.gif");
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_CONFLICT_STAT, PATH_OVERLAY+"conflict_stat.gif");

	// content locality indicator objects
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_NOTLOCAL_STAT, PATH_STAT+"notlocal_stat.gif");
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_LOCAL_STAT, PATH_STAT+"local_stat.gif");
	//declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_FILLLOCAL_STAT, PATH_STAT+"filllocal_stat.gif");
	
	// cursor icons for direct manipulation in PartPresentation
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_LEFT_SOURCE, PATH_DND+"left_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_LEFT_MASK, PATH_DND+"left_mask.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_RIGHT_SOURCE, PATH_DND+"right_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_RIGHT_MASK, PATH_DND+"right_mask.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOP_SOURCE, PATH_DND+"top_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOP_MASK, PATH_DND+"top_mask.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_BOTTOM_SOURCE, PATH_DND+"bottom_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_BOTTOM_MASK, PATH_DND+"bottom_mask.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_INVALID_SOURCE, PATH_DND+"invalid_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_INVALID_MASK, PATH_DND+"invalid_mask.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_STACK_SOURCE, PATH_DND+"stack_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_STACK_MASK, PATH_DND+"stack_mask.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_OFFSCREEN_SOURCE, PATH_DND+"offscreen_source.bmp");//$NON-NLS-1$
	declareRegistryImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_OFFSCREEN_MASK, PATH_DND+"offscreen_mask.bmp");//$NON-NLS-1$
}
/**
 * Declare an Image in the registry table.
 * @param key   The key to use when registering the image
 * @param path  The path where the image can be found. This path is relative to where
 *              this plugin class is found (i.e. typically the packages directory)
 */
private final static void declareRegistryImage(String key,String path) {
	URL url = null;
	try {
		url = new URL(URL_BASIC, path);
	} catch (MalformedURLException e) {
	}
	ImageDescriptor desc = ImageDescriptor.createFromURL(url);
	descriptors.put(key, desc);
	imageRegistry.put(key, desc);
}
/**
 * Returns the image stored in the workbench plugin's image registry 
 * under the given symbolic name.  If there isn't any value associated 
 * with the name then <code>null</code> is returned.  
 *
 * The returned Image is managed by the workbench plugin's image registry.  
 * Callers of this method must not dispose the returned image.
 *
 * This method is essentially a convenient short form of
 * WorkbenchImages.getImageRegistry.get(symbolicName).
 */
public static Image getImage(String symbolicName) {
	return getImageRegistry().get(symbolicName);
}
/**
 * Returns the image descriptor stored under the given symbolic name.
 * If there isn't any value associated with the name then <code>null
 * </code> is returned.
 *
 * The class also "caches" commonly used images in the image registry.
 * If you are looking for one of these common images it is recommended you use 
 * the getImage() method instead.
 */
public static ImageDescriptor getImageDescriptor(String symbolicName) {
	return (ImageDescriptor)descriptors.get(symbolicName);
}
/**
 * Convenience Method.
 * Returns an ImageDescriptor whose path, relative to the plugin containing 
 * the <code>extension</code> is <code>subdirectoryAndFilename</code>.
 * If there isn't any value associated with the name then <code>null
 * </code> is returned.
 *
 * This method is convenience and only intended for use by the workbench because it
 * explicitly uses the workbench's registry for caching/retrieving images from other
 * extensions -- other plugins must user their own registry. 
 * This convenience method is subject to removal.
 *
 * Note:
 * subdirectoryAndFilename must not have any leading "." or path separators / or \
 * ISV's should use  icons/mysample.gif and not ./icons/mysample.gif
 *
 * Note:
 * This consults the plugin for extension and obtains its installation location.
 * all requested images are assumed to be in a directory below and relative to that
 * plugins installation directory.
 */
public static ImageDescriptor getImageDescriptorFromExtension(IExtension extension, String subdirectoryAndFilename) {
	Assert.isNotNull(extension);
	Assert.isNotNull(subdirectoryAndFilename);
	return getImageDescriptorFromPlugin(extension.getDeclaringPluginDescriptor(),subdirectoryAndFilename);
}
/**
 * Convenience Method.
 * Return an ImageDescriptor whose path relative to the plugin described 
 * by <code>pluginDescriptor</code> or one of its fragments is 
 * <code>subdirectoryAndFilename</code>.
 * Returns <code>null</code>if no image could be found.
 *
 * This method is convenience and only intended for use by the workbench because it
 * explicitly uses the workbench's registry for caching/retrieving images from other
 * extensions -- other plugins must user their own registry. 
 * This convenience method is subject to removal.
 *
 * Note:
 * subdirectoryAndFilename must not have any leading "." or path separators / or \
 * ISV's should use  icons/mysample.gif and not ./icons/mysample.gif
 *
 * Note:
 * This consults the plugin for extension and obtains its installation location.
 * all requested images are assumed to be in a directory below and relative to that
 * plugins installation directory or one of its fragments.
 */
public static ImageDescriptor getImageDescriptorFromPlugin(IPluginDescriptor pluginDescriptor, String subdirectoryAndFilename) {
	Assert.isNotNull(pluginDescriptor);
	Assert.isNotNull(subdirectoryAndFilename);
	URL fullPathString = pluginDescriptor.find(new Path(subdirectoryAndFilename));
	if (fullPathString != null) {
		return ImageDescriptor.createFromURL(fullPathString);
	}
	URL path = pluginDescriptor.getInstallURL();
	try {
		fullPathString = new URL(path,subdirectoryAndFilename);
		return ImageDescriptor.createFromURL(fullPathString);
	} catch (MalformedURLException e) {
	}
	return null;
}
/**
 * Convenience Method.
 * Returns an ImageDescriptor whose path, relative to the plugin (within given id)
 * is <code>subdirectoryAndFilename</code>.
 * If there isn't any value associated with the name then <code>null
 * </code> is returned.
 *
 * This method is convenience and only intended for use by the workbench because it
 * explicitly uses the workbench's registry for caching/retrieving images from other
 * extensions -- other plugins must user their own registry. 
 * This convenience method is subject to removal.
 *
 * Note:
 * subdirectoryAndFilename must not have any leading "." or path separators / or \
 * ISV's should use  icons/mysample.gif and not ./icons/mysample.gif
 *
 * Note:
 * This consults the plugin for extension and obtains its installation location.
 * all requested images are assumed to be in a directory below and relative to that
 * plugins installation directory.
 */
public static ImageDescriptor getImageDescriptorFromPluginID(String pluginId, String subdirectoryAndFilename) {
	Assert.isNotNull(pluginId);
	Assert.isNotNull(subdirectoryAndFilename);
	return getImageDescriptorFromPlugin(
				Platform.getPluginRegistry().getPluginDescriptor(pluginId),
				subdirectoryAndFilename);
}
/**
 * Convenience Method.
 * Returns an ImageDescriptor obtained from an external program.
 * If there isn't any image then <code>null</code> is returned.
 *
 * This method is convenience and only intended for use by the workbench because it
 * explicitly uses the workbench's registry for caching/retrieving images from other
 * extensions -- other plugins must user their own registry. 
 * This convenience method is subject to removal.
 *
 * Note:
 * This consults the plugin for extension and obtains its installation location.
 * all requested images are assumed to be in a directory below and relative to that
 * plugins installation directory.
 */

public static ImageDescriptor getImageDescriptorFromProgram(String filename, int offset) {
	Assert.isNotNull(filename);
	String key = filename + "*" + offset; //use * as it is not a valid filename character//$NON-NLS-1$
	ImageDescriptor desc = getImageDescriptor(key);
	if (desc == null) {
		desc = new ProgramImageDescriptor(filename,offset);
		descriptors.put(key,desc);
	}
	return desc;
}
/**
 * Returns the ImageRegistry.
 */

public static ImageRegistry getImageRegistry() {
	if (imageRegistry == null) {
		initializeImageRegistry();
	}
	return imageRegistry;
}
/**
 *  Initialize the image registry by declaring all of the required
 *  graphics. This involves creating JFace image descriptors describing
 *  how to create/find the image should it be needed.
 *  The image is not actually allocated until requested.
 *
 *  Prefix conventions
 *      Wizard Banners          WIZBAN_
 *      Preference Banners      PREF_BAN_
 *      Property Page Banners   PROPBAN_
 *      Color toolbar           CTOOL_
 *      Enable toolbar          ETOOL_
 *      Disable toolbar         DTOOL_
 *      Local enabled toolbar   ELCL_
 *      Local Disable toolbar   DLCL_
 *      Object large            OBJL_
 *      Object small            OBJS_
 *      View                    VIEW_
 *      Product images          PROD_
 *      Misc images             MISC_
 *
 *  Where are the images?
 *      The images (typically gifs) are found in the same location as this plugin class.
 *      This may mean the same package directory as the package holding this class.
 *      The images are declared using this.getClass() to ensure they are looked up via
 *      this plugin class.
 *  @see JFace's ImageRegistry
 */
public static ImageRegistry initializeImageRegistry() {
	imageRegistry = new ImageRegistry();
	declareImages();
	return imageRegistry;
}
}
