/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422040
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 426365
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;
import org.eclipse.ui.internal.util.BundleUtility;

/**
 * This class provides convenience access to many of the resources required by
 * the workbench. The class stores some images as descriptors, and some are
 * stored as real Images in the registry. This is a pure speed-space tradeoff.
 * The trick for users of this class is that images obtained from the registry
 * (using getImage()), don't require disposal since they are shared, while
 * images obtained using getImageDescriptor() will require disposal. Consult the
 * declareImages method to see if a given image is declared as a registry image
 * or just as a descriptor. If you change an image from being stored as a
 * descriptor to a registry image, or vice-versa, make sure to check all users
 * of the image to ensure they are calling the correct getImage... method and
 * handling disposal correctly.
 *
 * Images: - use getImage(key) to access cached images from the registry. - Less
 * common images are found by calling getImageDescriptor(key) where key can be
 * found in IWorkbenchGraphicConstants
 *
 * This class initializes the image registry by declaring all of the required
 * graphics. This involves creating image descriptors describing how to
 * create/find the image should it be needed. The image is not actually
 * allocated until requested.
 *
 * Some Images are also made available to other plugins by being placed in the
 * descriptor table of the SharedImages class.
 *
 * Where are the images? The images (typically SVG file) are found the plugins
 * install directory
 *
 * How to add a new image Place the SVG file into the appropriate directories.
 * Add a constant to IWorkbenchGraphicConstants following the conventions Add
 * the declaration to this file
 */
public/* final */class WorkbenchImages {

	private static Map<String, ImageDescriptor> descriptors;

	private static ImageRegistry imageRegistry;

	/* Declare Common paths */

	public static final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$

	private static final String PATH_ETOOL = ICONS_PATH + "etool16/"; // Enabled toolbar icons.//$NON-NLS-1$

	private static final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; // Enabled local toolbar icons.//$NON-NLS-1$

	private static final String PATH_EVIEW = ICONS_PATH + "eview16/"; // View icons//$NON-NLS-1$

	private static final String PATH_OVERLAY = ICONS_PATH + "ovr16/"; //$NON-NLS-1$

	private static final String PATH_OBJECT = ICONS_PATH + "obj16/"; // Model object icons//$NON-NLS-1$

	private static final String PATH_POINTER = ICONS_PATH + "pointer/"; // Pointer icons//$NON-NLS-1$

	private static final String PATH_WIZBAN = ICONS_PATH + "wizban/"; // Wizard icons//$NON-NLS-1$

	private static final String PATH_PREF = ICONS_PATH + "pref/"; // Preferences icons//$NON-NLS-1$

	/**
	 * Declares a workbench image given the path of the image file (relative to the
	 * workbench plug-in). This is a helper method that creates the image descriptor
	 * and passes it to the main <code>declareImage</code> method.
	 *
	 * @param key    the symbolic name of the image
	 * @param path   the path of the image file relative to the base of the
	 *               workbench plug-ins install directory
	 * @param shared <code>true</code> if this is a shared image, and
	 *               <code>false</code> if this is not a shared image
	 */
	private static final void declareImage(String key, String path, boolean shared) {
		declareImage(key, null, path, shared);
	}

	private static final void declareImage(String key, String disabledKey, String path, boolean shared) {
		ImageDescriptor desc = ImageDescriptor.createFromURLSupplier(true,
				() -> BundleUtility.find(PlatformUI.PLUGIN_ID, path));
		declareImage(key, desc, shared);
		if (disabledKey != null) {
			ImageDescriptor disabledImageDescriptor = ImageDescriptor.createWithFlags(desc, SWT.IMAGE_DISABLE);
			declareImage(disabledKey, disabledImageDescriptor, shared);
		}
	}

	/**
	 * Declares all the workbench's images, including both "shared" ones and
	 * internal ones.
	 */
	@SuppressWarnings("removal")
	private static final void declareImages() {
		// Overlays
		declareImage(ISharedImages.IMG_DEC_FIELD_ERROR, PATH_OVERLAY + "error_ovr.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_DEC_FIELD_WARNING, PATH_OVERLAY + "warning_ovr.svg", true); //$NON-NLS-1$

		// Pinning
		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR,
				IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR_DISABLED, PATH_ETOOL + "pin_editor.svg", false); //$NON-NLS-1$

		// other toolbar buttons
		declareImage(ISharedImages.IMG_ETOOL_SAVE_EDIT, ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED, //
				PATH_ETOOL + "save_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_SAVEAS_EDIT, ISharedImages.IMG_ETOOL_SAVEAS_EDIT_DISABLED, //
				PATH_ETOOL + "saveas_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_SAVEALL_EDIT, ISharedImages.IMG_ETOOL_SAVEALL_EDIT_DISABLED, //
				PATH_ETOOL + "saveall_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_UNDO, ISharedImages.IMG_TOOL_UNDO_DISABLED, //
				PATH_ETOOL + "undo_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_REDO, ISharedImages.IMG_TOOL_REDO_DISABLED, //
				PATH_ETOOL + "redo_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_CUT, ISharedImages.IMG_TOOL_CUT_DISABLED, //
				PATH_ETOOL + "cut_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_COPY, ISharedImages.IMG_TOOL_COPY_DISABLED, //
				PATH_ETOOL + "copy_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_PASTE, ISharedImages.IMG_TOOL_PASTE_DISABLED, //
				PATH_ETOOL + "paste_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_DELETE, ISharedImages.IMG_TOOL_DELETE_DISABLED, //
				PATH_ETOOL + "delete_edit.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_DELETE, ISharedImages.IMG_ETOOL_DELETE_DISABLED, //
				PATH_ETOOL + "delete.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_CLEAR, ISharedImages.IMG_ETOOL_CLEAR_DISABLED, //
				PATH_ETOOL + "clear.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_NEW_WIZARD, ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED, //
				PATH_ETOOL + "new_wiz.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_PRINT_EDIT, ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED, //
				PATH_ETOOL + "print_edit.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS, PATH_ETOOL + "help_contents.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_SEARCH, PATH_ETOOL + "help_search.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_TIPS_AND_TRICKS, PATH_ETOOL + "tricks.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_FASTVIEW,
				IWorkbenchGraphicConstants.IMG_DTOOL_NEW_FASTVIEW, PATH_ETOOL + "new_fastview.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_RESTORE_TRIMPART, PATH_ETOOL + "fastview_restore.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_EDITOR_TRIMPART, PATH_ETOOL + "editor_area.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_FORWARD, ISharedImages.IMG_TOOL_FORWARD_DISABLED, //
				PATH_ELOCALTOOL + "forward_nav.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_BACK, ISharedImages.IMG_TOOL_BACK_DISABLED, //
				PATH_ELOCALTOOL + "backward_nav.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_TOOL_UP, ISharedImages.IMG_TOOL_UP_DISABLED, //
				PATH_ELOCALTOOL + "up_nav.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ELCL_SYNCED, ISharedImages.IMG_ELCL_SYNCED_DISABLED, //
				PATH_ELOCALTOOL + "synced.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ELCL_COLLAPSEALL, ISharedImages.IMG_ELCL_COLLAPSEALL_DISABLED, //
				PATH_ELOCALTOOL + "collapseall.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ELCL_REMOVE, ISharedImages.IMG_ELCL_REMOVE_DISABLED, //
				PATH_ELOCALTOOL + "remove.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ELCL_REMOVEALL, ISharedImages.IMG_ELCL_REMOVEALL_DISABLED, //
				PATH_ELOCALTOOL + "removeall.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ELCL_COLLAPSEALL, ISharedImages.IMG_ELCL_COLLAPSEALL_DISABLED, //
				PATH_ELOCALTOOL + "collapseall.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ELCL_STOP, ISharedImages.IMG_ELCL_STOP_DISABLED, //
				PATH_ELOCALTOOL + "stop.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE, PATH_EVIEW + "new_persp.svg", false); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_HOME_NAV, ISharedImages.IMG_ETOOL_HOME_NAV_DISABLED, //
				PATH_ELOCALTOOL + "home_nav.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE, PATH_EVIEW + "default_persp.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ, PATH_WIZBAN + "new_wiz.svg", false); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ, PATH_ETOOL + "import_wiz.svg", false); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ, PATH_ETOOL + "export_wiz.svg", false); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ, PATH_WIZBAN + "import_wiz.svg", false); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ, PATH_WIZBAN + "export_wiz.svg", false); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_PREF_WIZ, PATH_WIZBAN + "importpref_wiz.svg", false); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_PREF_WIZ, PATH_WIZBAN + "exportpref_wiz.svg", false); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_WORKINGSET_WIZ, PATH_WIZBAN + "workset_wiz.svg", false); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC, PATH_EVIEW + "defaultview_misc.svg", false); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_FONT, PATH_OBJECT + "font.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_THEME_CATEGORY, PATH_OBJECT + "theme_category.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY, PATH_OBJECT + "activity.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY_CATEGORY, PATH_OBJECT + "activity_category.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_WORKING_SETS, PATH_OBJECT + "workingsets.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_SEPARATOR, PATH_OBJECT + "separator.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_NODE, PATH_OBJECT + "generic_elements.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_ELEMENT, PATH_OBJECT + "generic_element.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_OBJ_ADD, PATH_OBJECT + "add_obj.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJ_FILE, PATH_OBJECT + "file_obj.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJ_FOLDER, PATH_OBJECT + "fldr_obj.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJ_ELEMENT, PATH_OBJECT + "elements_obj.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_DEF_VIEW, PATH_EVIEW + "defaultview_misc.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW, PATH_ELOCALTOOL + "close_view.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW, PATH_ELOCALTOOL + "min_view.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU, PATH_ELOCALTOOL + "view_menu.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_BUTTON_MENU, PATH_ELOCALTOOL + "button_menu.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_LCL_LINKTO_HELP, PATH_ELOCALTOOL + "linkto_help.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_RENDERED_VIEW_MENU, PATH_ELOCALTOOL + "view_menu.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_THIN, PATH_ELOCALTOOL + "thin_close_view.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_HIDE_TOOLBAR_THIN, PATH_ELOCALTOOL + "thin_hide_toolbar.svg", //$NON-NLS-1$
				true);
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_MAX_VIEW_THIN, PATH_ELOCALTOOL + "thin_max_view.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_THIN, PATH_ELOCALTOOL + "thin_min_view.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_RESTORE_VIEW_THIN, PATH_ELOCALTOOL + "thin_restore_view.svg", //$NON-NLS-1$
				true);
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_SHOW_TOOLBAR_THIN, PATH_ELOCALTOOL + "thin_show_toolbar.svg", //$NON-NLS-1$
				true);
		declareImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_THIN, PATH_ELOCALTOOL + "thin_view_menu.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_OBJS_ERROR_TSK, PATH_OBJECT + "error_tsk.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_WARN_TSK, PATH_OBJECT + "warn_tsk.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_INFO_TSK, PATH_OBJECT + "info_tsk.svg", true); //$NON-NLS-1$

		declareImage(ISharedImages.IMG_OBJS_DND_LEFT_SOURCE, PATH_POINTER + "left_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_LEFT_MASK, PATH_POINTER + "left_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_RIGHT_SOURCE, PATH_POINTER + "right_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_RIGHT_MASK, PATH_POINTER + "right_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_TOP_SOURCE, PATH_POINTER + "top_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_TOP_MASK, PATH_POINTER + "top_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_BOTTOM_SOURCE, PATH_POINTER + "bottom_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_BOTTOM_MASK, PATH_POINTER + "bottom_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_INVALID_SOURCE, PATH_POINTER + "invalid_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_INVALID_MASK, PATH_POINTER + "invalid_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_STACK_SOURCE, PATH_POINTER + "stack_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_STACK_MASK, PATH_POINTER + "stack_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_OFFSCREEN_SOURCE, PATH_POINTER + "offscreen_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_OFFSCREEN_MASK, PATH_POINTER + "offscreen_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_TOFASTVIEW_SOURCE, PATH_POINTER + "tofastview_source.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_TOFASTVIEW_MASK, PATH_POINTER + "tofastview_mask.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_LEFT, PATH_POINTER + "left.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_RIGHT, PATH_POINTER + "right.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_TOP, PATH_POINTER + "top.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_BOTTOM, PATH_POINTER + "bottom.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_INVALID, PATH_POINTER + "invalid.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_STACK, PATH_POINTER + "stack.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_OFFSCREEN, PATH_POINTER + "offscreen.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_OBJS_DND_TOFASTVIEW, PATH_POINTER + "tofastview.svg", true); //$NON-NLS-1$

		// signed jar images
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_YES, PATH_OBJECT + "signed_yes_tbl.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_NO, PATH_OBJECT + "signed_no_tbl.svg", true); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_OBJ_SIGNED_UNKNOWN, PATH_OBJECT + "signed_unkn_tbl.svg", true); //$NON-NLS-1$

		declareImage(IWorkbenchGraphicConstants.IMG_PREF_IMPORT, PATH_PREF + "import_wiz.svg", false); //$NON-NLS-1$
		declareImage(IWorkbenchGraphicConstants.IMG_PREF_EXPORT, PATH_PREF + "export_wiz.svg", false); //$NON-NLS-1$

		declareHoverImages();


	}

	/**
	 * Declares all the workbench's deprecated hover images, including both "shared"
	 * ones and internal ones.
	 *
	 * @deprecated As of 3.0, since the workbench itself no longer uses the hover
	 *             image variants
	 */
	@Deprecated
	private static final void declareHoverImages() {
		declareImage(ISharedImages.IMG_TOOL_UNDO_HOVER, PATH_ETOOL + "undo_edit.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_REDO_HOVER, PATH_ETOOL + "redo_edit.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_CUT_HOVER, PATH_ETOOL + "cut_edit.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_COPY_HOVER, PATH_ETOOL + "copy_edit.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_PASTE_HOVER, PATH_ETOOL + "paste_edit.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_FORWARD_HOVER, PATH_ELOCALTOOL + "forward_nav.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_DELETE_HOVER, PATH_ETOOL + "delete_edit.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_NEW_WIZARD_HOVER, PATH_ETOOL + "new_wiz.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_BACK_HOVER, PATH_ELOCALTOOL + "backward_nav.svg", true); //$NON-NLS-1$
		declareImage(ISharedImages.IMG_TOOL_UP_HOVER, PATH_ELOCALTOOL + "up_nav.svg", true); //$NON-NLS-1$
	}

	/**
	 * Declares a workbench image.
	 * <p>
	 * The workbench remembers the given image descriptor under the given name, and
	 * makes the image available to plug-ins via {@link org.eclipse.ui.ISharedImages
	 * IWorkbench.getSharedImages()}. For "shared" images, the workbench remembers
	 * the image descriptor and will manages the image object create from it;
	 * clients retrieve "shared" images via
	 * {@link org.eclipse.ui.ISharedImages#getImage ISharedImages.getImage()}. For
	 * the other, "non-shared" images, the workbench remembers only the image
	 * descriptor; clients retrieve the image descriptor via
	 * {@link org.eclipse.ui.ISharedImages#getImageDescriptor
	 * ISharedImages.getImageDescriptor()} and are entirely responsible for managing
	 * the image objects they create from it. (This is made confusing by the
	 * historical fact that the API interface is called "ISharedImages".)
	 * </p>
	 *
	 * @param symbolicName the symbolic name of the image
	 * @param descriptor   the image descriptor
	 * @param shared       <code>true</code> if this is a shared image, and
	 *                     <code>false</code> if this is not a shared image
	 * @see org.eclipse.ui.ISharedImages#getImage
	 * @see org.eclipse.ui.ISharedImages#getImageDescriptor
	 */
	public static void declareImage(String symbolicName, ImageDescriptor descriptor, boolean shared) {
		if (Policy.DEBUG_DECLARED_IMAGES) {
			Image image = descriptor.createImage(false);
			if (image == null) {
				WorkbenchPlugin.log("Image not found in WorkbenchImages.declaredImage().  symbolicName=" + symbolicName //$NON-NLS-1$
						+ " descriptor=" + descriptor, new Exception("stack dump")); //$NON-NLS-1$//$NON-NLS-2$
			} else {
				image.dispose();
			}
		}
		getDescriptors().put(symbolicName, descriptor);
		if (shared) {
			getImageRegistry().put(symbolicName, descriptor);
		}
	}

	/**
	 * Returns the map from symbolic name to ImageDescriptor.
	 *
	 * @return the map from symbolic name to ImageDescriptor.
	 */
	public static Map<String, ImageDescriptor> getDescriptors() {
		if (descriptors == null) {
			initializeImageRegistry();
		}
		return descriptors;
	}

	/**
	 * Returns the image stored in the workbench plugin's image registry under the
	 * given symbolic name. If there isn't any value associated with the name then
	 * <code>null</code> is returned.
	 *
	 * The returned Image is managed by the workbench plugin's image registry.
	 * Callers of this method must not dispose the returned image.
	 *
	 * This method is essentially a convenient short form of
	 * WorkbenchImages.getImageRegistry.get(symbolicName).
	 *
	 * @param symbolicName the symbolic name
	 * @return the image
	 */
	public static Image getImage(String symbolicName) {
		return getImageRegistry().get(symbolicName);
	}

	/**
	 * Returns the image descriptor stored under the given symbolic name. If there
	 * isn't any value associated with the name then <code>null
	 * </code> is returned.
	 *
	 * The class also "caches" commonly used images in the image registry. If you
	 * are looking for one of these common images it is recommended you use the
	 * getImage() method instead.
	 *
	 * @param symbolicName the symbolic name
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String symbolicName) {
		return getDescriptors().get(symbolicName);
	}

	/**
	 * Convenience Method. Returns an ImageDescriptor obtained from an external
	 * program. If there isn't any image then <code>null</code> is returned.
	 *
	 * This method is convenience and only intended for use by the workbench because
	 * it explicitly uses the workbench's registry for caching/retrieving images
	 * from other extensions -- other plugins must user their own registry. This
	 * convenience method is subject to removal.
	 *
	 * Note: This consults the plugin for extension and obtains its installation
	 * location. all requested images are assumed to be in a directory below and
	 * relative to that plugins installation directory.
	 *
	 * @param filename the file name
	 * @param offset   the offset
	 * @return the image descriptor
	 */

	public static ImageDescriptor getImageDescriptorFromProgram(String filename, int offset) {
		Assert.isNotNull(filename);
		String key = filename + "*" + offset; // use * as it is not a valid filename character//$NON-NLS-1$
		ImageDescriptor desc = getImageDescriptor(key);
		if (desc == null) {
			desc = new ProgramImageDescriptor(filename, offset);
			getDescriptors().put(key, desc);
		}
		return desc;
	}

	/**
	 * Returns the ImageRegistry.
	 *
	 * @return the image registry
	 */
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			initializeImageRegistry();
		}
		return imageRegistry;
	}

	/**
	 * Initialize the image registry by declaring all of the required graphics. This
	 * involves creating JFace image descriptors describing how to create/find the
	 * image should it be needed. The image is not actually allocated until
	 * requested.
	 *
	 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_
	 * Property Page Banners PROPBAN_ Enable toolbar ETOOL_ Disable toolbar DTOOL_
	 * Local enabled toolbar ELCL_ Local Disable toolbar DLCL_ Object large OBJL_
	 * Object small OBJS_ View VIEW_ Product images PROD_ Misc images MISC_
	 *
	 * Where are the images? The images (typically SVG files) are found in the same
	 * location as this plugin class. This may mean the same package directory as
	 * the package holding this class. The images are declared using this.getClass()
	 * to ensure they are looked up via this plugin class.
	 *
	 * @see ImageRegistry
	 */
	private static void initializeImageRegistry() {
		imageRegistry = new ImageRegistry();
		descriptors = new HashMap<>();
		declareImages();
	}

	/**
	 * Disposes and clears the workbench images. Called when the workbench is
	 * shutting down.
	 *
	 * @since 3.1
	 */
	public static void dispose() {
		if (imageRegistry != null) {
			imageRegistry.dispose();
			imageRegistry = null;
			descriptors = null;
		}
	}

	/**
	 * Get the workbench image with the given path relative to ICON_PATH.
	 *
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor getWorkbenchImageDescriptor(String relativePath) {
		return ImageDescriptor
				.createFromURLSupplier(true, () -> BundleUtility.find(PlatformUI.PLUGIN_ID, ICONS_PATH + relativePath));
	}
}
