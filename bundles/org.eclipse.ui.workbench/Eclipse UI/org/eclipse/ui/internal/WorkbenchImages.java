/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;
import org.eclipse.ui.internal.util.BundleUtility;

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
public/*final*/class WorkbenchImages {

    private static Map descriptors = new HashMap();

    private static ImageRegistry imageRegistry = null;

    //Key: ImageDescriptor
    //Value: Image
    private static ReferenceCounter imageCache = new ReferenceCounter();

    /* Declare Common paths */

    public final static String ICONS_PATH = "icons/full/";//$NON-NLS-1$

    private final static String PATH_ETOOL = ICONS_PATH + "etool16/"; //Enabled toolbar icons.//$NON-NLS-1$

    private final static String PATH_DTOOL = ICONS_PATH + "dtool16/"; //Disabled toolbar icons.//$NON-NLS-1$

    private final static String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; //Enabled local toolbar icons.//$NON-NLS-1$

    private final static String PATH_DLOCALTOOL = ICONS_PATH + "dlcl16/"; //Disabled local toolbar icons.//$NON-NLS-1$

    private final static String PATH_EVIEW = ICONS_PATH + "eview16/"; //View icons//$NON-NLS-1$

    //private final static String PATH_PROD = ICONS_PATH+"prod/";	//Product images
    private final static String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons//$NON-NLS-1$

    private final static String PATH_POINTER = ICONS_PATH + "pointer/"; //Pointer icons//$NON-NLS-1$

    private final static String PATH_WIZBAN = ICONS_PATH + "wizban/"; //Wizard icons//$NON-NLS-1$

    //private final static String PATH_STAT = ICONS_PATH+"stat/";
    //private final static String PATH_MISC = ICONS_PATH+"misc/";
    //private final static String PATH_OVERLAY = ICONS_PATH+"ovr16/";

    static {
        initializeImageRegistry();
    }

    /**
     * Returns the image cache used internally by the workbench.
     */
    public static ReferenceCounter getImageCache() {
        return imageCache;
    }

    /**
     * Declares a workbench image given the path of the image file (relative to
     * the workbench plug-in). This is a helper method that creates the image
     * descriptor and passes it to the main <code>declareImage</code> method.
     * 
     * @param symbolicName the symbolic name of the image
     * @param path the path of the image file relative to the base of the workbench
     * plug-ins install directory
     * @param shared <code>true</code> if this is a shared image, and
     * <code>false</code> if this is not a shared image
     */
    private final static void declareImage(String key, String path,
            boolean shared) {
        URL url = BundleUtility.find(PlatformUI.PLUGIN_ID, path);
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        declareImage(key, desc, shared);
    }

    /**
     * Declares all the workbench's images, including both "shared" ones and
     * internal ones.
     */
    private final static void declareImages() {

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR,
                PATH_ETOOL + "pin_editor.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_PIN_EDITOR_DISABLED,
                PATH_DTOOL + "pin_editor.gif", false); //$NON-NLS-1$

        // other toolbar buttons

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVE_EDIT, PATH_ETOOL
                + "save_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVE_EDIT_HOVER,
                PATH_ETOOL + "save_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVE_EDIT_DISABLED,
                PATH_DTOOL + "save_edit.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEAS_EDIT,
                PATH_ETOOL + "saveas_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEAS_EDIT_HOVER,
                PATH_ETOOL + "saveas_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEAS_EDIT_DISABLED,
                PATH_DTOOL + "saveas_edit.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEALL_EDIT,
                PATH_ETOOL + "saveall_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_SAVEALL_EDIT_HOVER,
                PATH_ETOOL + "saveall_edit.gif", false); //$NON-NLS-1$
        declareImage(
                IWorkbenchGraphicConstants.IMG_ETOOL_SAVEALL_EDIT_DISABLED,
                PATH_DTOOL + "saveall_edit.gif", false); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_UNDO,
                PATH_ETOOL + "undo_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_UNDO_HOVER, PATH_ETOOL
                + "undo_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_UNDO_DISABLED, PATH_DTOOL
                + "undo_edit.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_REDO,
                PATH_ETOOL + "redo_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_REDO_HOVER, PATH_ETOOL
                + "redo_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_REDO_DISABLED, PATH_DTOOL
                + "redo_edit.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_CUT,
                PATH_ETOOL + "cut_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_CUT_HOVER, PATH_ETOOL
                + "cut_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_CUT_DISABLED, PATH_DTOOL
                + "cut_edit.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_COPY,
                PATH_ETOOL + "copy_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_COPY_HOVER, PATH_ETOOL
                + "copy_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_COPY_DISABLED, PATH_DTOOL
                + "copy_edit.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_PASTE, PATH_ETOOL
                + "paste_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_PASTE_HOVER, PATH_ETOOL
                + "paste_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_PASTE_DISABLED, PATH_DTOOL
                + "paste_edit.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_DELETE, PATH_ETOOL
                + "delete_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_DELETE_HOVER, PATH_ETOOL
                + "delete_edit.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_DELETE_DISABLED, PATH_DTOOL
                + "delete_edit.gif", true); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_PRINT_EDIT,
                PATH_ETOOL + "print_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_PRINT_EDIT_HOVER,
                PATH_ETOOL + "print_edit.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_PRINT_EDIT_DISABLED,
                PATH_DTOOL + "print_edit.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_HELP_CONTENTS,
                PATH_ETOOL + "help_contents.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_FORWARD, PATH_ELOCALTOOL
                + "forward_nav.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_FORWARD_HOVER, PATH_ELOCALTOOL
                + "forward_nav.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_FORWARD_DISABLED, PATH_DLOCALTOOL
                + "forward_nav.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_BACK, PATH_ELOCALTOOL
                + "backward_nav.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_BACK_HOVER, PATH_ELOCALTOOL
                + "backward_nav.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_BACK_DISABLED, PATH_DLOCALTOOL
                + "backward_nav.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_TOOL_UP,
                PATH_ELOCALTOOL + "up_nav.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_UP_HOVER, PATH_ELOCALTOOL
                + "up_nav.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_TOOL_UP_DISABLED, PATH_DLOCALTOOL
                + "up_nav.gif", true); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE, PATH_EVIEW
                + "new_persp.gif", false); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE_HOVER,
                PATH_EVIEW + "new_persp.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_HOME_NAV,
                PATH_ELOCALTOOL + "home_nav.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_ETOOL_DEF_PERSPECTIVE,
                PATH_EVIEW + "default_persp.gif", false); //$NON-NLS-1$
        declareImage(
                IWorkbenchGraphicConstants.IMG_ETOOL_DEF_PERSPECTIVE_HOVER,
                PATH_EVIEW + "default_persp.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_WIZBAN_WORKINGSET_WIZ,
                PATH_WIZBAN + "workset_wiz.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_VIEW_DEFAULTVIEW_MISC,
                PATH_EVIEW + "defaultview_misc.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_DLGBAN_SAVEAS_DLG,
                PATH_WIZBAN + "saveas_wiz.gif", false); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_OBJ_FONT, PATH_OBJECT
                + "font.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJ_THEME_CATEGORY,
                PATH_OBJECT + "theme_category.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY, PATH_OBJECT
                + "activity.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY_CATEGORY,
                PATH_OBJECT + "activity_category.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_OBJ_FILE,
                PATH_OBJECT + "file_obj.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_OBJ_FOLDER,
                PATH_OBJECT + "fldr_obj.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_OBJ_ELEMENT, PATH_OBJECT
                + "elements_obj.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_DEF_VIEW, PATH_EVIEW
                + "defaultview_misc.gif", true); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW,
                PATH_ELOCALTOOL + "close_view.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW_HOVER,
                PATH_ELOCALTOOL + "close_view.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW,
                PATH_ELOCALTOOL + "pin_view.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_PIN_VIEW_HOVER,
                PATH_ELOCALTOOL + "pin_view.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW,
                PATH_ELOCALTOOL + "min_view.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_MIN_VIEW_HOVER,
                PATH_ELOCALTOOL + "min_view.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU,
                PATH_ELOCALTOOL + "view_menu.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_VIEW_MENU_HOVER,
                PATH_ELOCALTOOL + "view_menu.gif", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_LCL_LINKTO_HELP,
                PATH_ELOCALTOOL + "linkto_help.gif", true); //$NON-NLS-1$

        declareImage(ISharedImages.IMG_OBJS_ERROR_TSK, PATH_OBJECT
                + "error_tsk.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_OBJS_WARN_TSK, PATH_OBJECT
                + "warn_tsk.gif", true); //$NON-NLS-1$
        declareImage(ISharedImages.IMG_OBJS_INFO_TSK, PATH_OBJECT
                + "info_tsk.gif", true); //$NON-NLS-1$

        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_LEFT_SOURCE,
                PATH_POINTER + "left_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_LEFT_MASK,
                PATH_POINTER + "left_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_RIGHT_SOURCE,
                PATH_POINTER + "right_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_RIGHT_MASK,
                PATH_POINTER + "right_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOP_SOURCE,
                PATH_POINTER + "top_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOP_MASK,
                PATH_POINTER + "top_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_BOTTOM_SOURCE,
                PATH_POINTER + "bottom_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_BOTTOM_MASK,
                PATH_POINTER + "bottom_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_INVALID_SOURCE,
                PATH_POINTER + "invalid_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_INVALID_MASK,
                PATH_POINTER + "invalid_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_STACK_SOURCE,
                PATH_POINTER + "stack_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_STACK_MASK,
                PATH_POINTER + "stack_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_OFFSCREEN_SOURCE,
                PATH_POINTER + "offscreen_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_OFFSCREEN_MASK,
                PATH_POINTER + "offscreen_mask.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOFASTVIEW_SOURCE,
                PATH_POINTER + "tofastview_source.bmp", true); //$NON-NLS-1$
        declareImage(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOFASTVIEW_MASK,
                PATH_POINTER + "tofastview_mask.bmp", true); //$NON-NLS-1$		
    }

    /**
     * Declares a workbench image.
     * <p>
     * The workbench remembers the given image descriptor under the given name,
     * and makes the image available to plug-ins via
     * {@link org.eclipse.ui.ISharedImages IWorkbench.getSharedImages()}.
     * For "shared" images, the workbench remembers the image descriptor and
     * will manages the image object create from it; clients retrieve "shared"
     * images via
     * {@link org.eclipse.ui.ISharedImages#getImage ISharedImages.getImage()}.
     * For the other, "non-shared" images, the workbench remembers only the
     * image descriptor; clients retrieve the image descriptor via
     * {@link org.eclipse.ui.ISharedImages#getImageDescriptor
     * ISharedImages.getImageDescriptor()} and are entirely
     * responsible for managing the image objects they create from it.
     * (This is made confusing by the historical fact that the API interface
     *  is called "ISharedImages".)
     * </p>
     * 
     * @param symbolicName the symbolic name of the image
     * @param descriptor the image descriptor
     * @param shared <code>true</code> if this is a shared image, and
     * <code>false</code> if this is not a shared image
     * @see org.eclipse.ui.ISharedImages#getImage
     * @see org.eclipse.ui.ISharedImages#getImageDescriptor
     */
    public static void declareImage(String symbolicName,
            ImageDescriptor descriptor, boolean shared) {
        descriptors.put(symbolicName, descriptor);
        if (shared) {
            imageRegistry.put(symbolicName, descriptor);
        }
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
        return (ImageDescriptor) descriptors.get(symbolicName);
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

    public static ImageDescriptor getImageDescriptorFromProgram(
            String filename, int offset) {
        Assert.isNotNull(filename);
        String key = filename + "*" + offset; //use * as it is not a valid filename character//$NON-NLS-1$
        ImageDescriptor desc = getImageDescriptor(key);
        if (desc == null) {
            desc = new ProgramImageDescriptor(filename, offset);
            descriptors.put(key, desc);
        }
        return desc;
    }

    /**
     * Returns the ImageRegistry.
     */
    public static ImageRegistry getImageRegistry() {
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