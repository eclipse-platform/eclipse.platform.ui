/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * A registry for common images used by the workbench which may be useful 
 * to other plug-ins.
 * <p>
 * This class provides <code>Image</code> and <code>ImageDescriptor</code>s
 * for each named image in the interface.  All <code>Image</code> objects provided 
 * by this class are managed by this class and must never be disposed 
 * by other clients.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface ISharedImages {
    /**
     * Identifies a file image.
     */
    public final static String IMG_OBJ_FILE = "IMG_OBJ_FILE"; //$NON-NLS-1$

    /**
     * Identifies a folder image.
     */
    public final static String IMG_OBJ_FOLDER = "IMG_OBJ_FOLDER"; //$NON-NLS-1$

    /**
     * Identifies a project image.
     * 
     * @deprecated in 3.0. This image is IDE-specific, and is therefore found
     * only in IDE configurations. IDE-specific tools should use 
     * <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT</code> instead.
     */
    public final static String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT"; //$NON-NLS-1$

    /**
     * Identifies a closed project image.
     * 
     * @deprecated in 3.0. This image is IDE-specific, and is therefore found
     * only in IDE configurations. IDE-specific tools should use 
     * <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED</code> instead.
     */
    public final static String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED"; //$NON-NLS-1$

    /**
     * Identifies an element image.
     */
    public final static String IMG_OBJ_ELEMENT = "IMG_OBJ_ELEMENTS"; //$NON-NLS-1$

    /**
     * Identifies the image used for "open marker".
     * 
     * @deprecated in 3.0. This image is IDE-specific, and is therefore found
     * only in IDE configurations. IDE-specific tools should use 
     * <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OPEN_MARKER</code> instead.
     */
    public final static String IMG_OPEN_MARKER = "IMG_OPEN_MARKER"; //$NON-NLS-1$

    /**
     * Identifies the default image used for views.
     */
    public final static String IMG_DEF_VIEW = "IMG_DEF_VIEW"; //$NON-NLS-1$

    /**
     * Identifies the default image used to indicate errors.
     */
    public final static String IMG_OBJS_ERROR_TSK = "IMG_OBJS_ERROR_TSK"; //$NON-NLS-1$

    /**
     * Identifies the default image used to indicate warnings.
     */
    public final static String IMG_OBJS_WARN_TSK = "IMG_OBJS_WARN_TSK"; //$NON-NLS-1$

    /**
     * Identifies the default image used to indicate information only.
     */
    public final static String IMG_OBJS_INFO_TSK = "IMG_OBJS_INFO_TSK"; //$NON-NLS-1$

    /**
     * Identifies the default image used to indicate a task.
     * 
     * @deprecated in 3.0. This image is IDE-specific, and is therefore found
     * only in IDE configurations. IDE-specific tools should use 
     * <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJS_TASK_TSK</code> instead.
     */
    public final static String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK"; //$NON-NLS-1$

    /**
     * Identifies the default image used to indicate a bookmark.
     * 
     * @deprecated in 3.0. This image is IDE-specific, and is therefore found
     * only in IDE configurations. IDE-specific tools should use 
     * <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJS_BKMRK_TSK</code> instead.
     */
    public final static String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK"; //$NON-NLS-1$

    /**
     * Identifies the new wizard image in the enabled state.
     */
    public final static String IMG_TOOL_NEW_WIZARD = "IMG_TOOL_NEW_WIZARD"; //$NON-NLS-1$

    /**
     * Identifies the new wizard image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_NEW_WIZARD</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_NEW_WIZARD_HOVER = "IMG_TOOL_NEW_WIZARD_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the new wizard image in the disabled state.
     */
    public final static String IMG_TOOL_NEW_WIZARD_DISABLED = "IMG_TOOL_NEW_WIZARD_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the undo image in the enabled state.
     */
    public final static String IMG_TOOL_UNDO = "IMG_TOOL_UNDO"; //$NON-NLS-1$

    /**
     * Identifies the undo image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_UNDO</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_UNDO_HOVER = "IMG_TOOL_UNDO_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the undo image in the disabled state.
     */
    public final static String IMG_TOOL_UNDO_DISABLED = "IMG_TOOL_UNDO_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the redo image in the enabled state.
     */
    public final static String IMG_TOOL_REDO = "IMG_TOOL_REDO"; //$NON-NLS-1$

    /**
     * Identifies the redo image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_REDO</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_REDO_HOVER = "IMG_TOOL_REDO_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the redo image in the disabled state.
     */
    public final static String IMG_TOOL_REDO_DISABLED = "IMG_TOOL_REDO_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the cut image in the enabled state.
     */
    public final static String IMG_TOOL_CUT = "IMG_TOOL_CUT"; //$NON-NLS-1$

    /**
     * Identifies the cut image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_CUT</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_CUT_HOVER = "IMG_TOOL_CUT_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the cut image in the disabled state.
     */
    public final static String IMG_TOOL_CUT_DISABLED = "IMG_TOOL_CUT_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the copy image in the enabled state.
     */
    public final static String IMG_TOOL_COPY = "IMG_TOOL_COPY"; //$NON-NLS-1$

    /**
     * Identifies the copy image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_COPY</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_COPY_HOVER = "IMG_TOOL_COPY_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the copy image in the disabled state.
     */
    public final static String IMG_TOOL_COPY_DISABLED = "IMG_TOOL_COPY_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the paste image in the enabled state.
     */
    public final static String IMG_TOOL_PASTE = "IMG_TOOL_PASTE"; //$NON-NLS-1$

    /**
     * Identifies the paste image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_PASTE</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_PASTE_HOVER = "IMG_TOOL_PASTE_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the paste image in the disabled state.
     */
    public final static String IMG_TOOL_PASTE_DISABLED = "IMG_TOOL_PASTE_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the delete image in the enabled state.
     */
    public final static String IMG_TOOL_DELETE = "IMG_TOOL_DELETE"; //$NON-NLS-1$

    /**
     * Identifies the delete image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_DELETE</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_DELETE_HOVER = "IMG_TOOL_DELETE_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the delete image in the disabled state.
     */
    public final static String IMG_TOOL_DELETE_DISABLED = "IMG_TOOL_DELETE_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the forward image in the enabled state.
     */
    public final static String IMG_TOOL_FORWARD = "IMG_TOOL_FORWARD"; //$NON-NLS-1$

    /**
     * Identifies the forward image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_FORWARD</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_FORWARD_HOVER = "IMG_TOOL_FORWARD_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the forward image in the disabled state.
     */
    public final static String IMG_TOOL_FORWARD_DISABLED = "IMG_TOOL_FORWARD_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the back image in the enabled state.
     */
    public final static String IMG_TOOL_BACK = "IMG_TOOL_BACK"; //$NON-NLS-1$

    /**
     * Identifies the back image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_BACK</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_BACK_HOVER = "IMG_TOOL_BACK_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the back image in the disabled state.
     */
    public final static String IMG_TOOL_BACK_DISABLED = "IMG_TOOL_BACK_DISABLED"; //$NON-NLS-1$

    /**
     * Identifies the up image in the enabled state.
     */
    public final static String IMG_TOOL_UP = "IMG_TOOL_UP"; //$NON-NLS-1$

    /**
     * Identifies the up image in the hover (colored) state.
     * 
     * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_UP</code>.
     *   Enabled images are now in color.  The workbench itself no longer uses the hover image variants.
     */
    public final static String IMG_TOOL_UP_HOVER = "IMG_TOOL_UP_HOVER"; //$NON-NLS-1$

    /**
     * Identifies the up image in the disabled state.
     */
    public final static String IMG_TOOL_UP_DISABLED = "IMG_TOOL_UP_DISABLED"; //$NON-NLS-1$
    
    /**
     * Retrieves the specified image from the workbench plugin's image registry.
     * Note: The returned <code>Image</code> is managed by the workbench; clients
     * must <b>not</b> dispose of the returned image.
     *
     * @param symbolicName the symbolic name of the image; there are constants
     * declared in this interface for build-in images that come with the workbench
     * @return the image, or <code>null</code> if not found
     */
    public Image getImage(String symbolicName);

    /**
     * Retrieves the image descriptor for specified image from the workbench's
     * image registry. Unlike <code>Image</code>s, image descriptors themselves do
     * not need to be disposed.
     *
     * @param symbolicName the symbolic name of the image; there are constants
     * declared in this interface for build-in images that come with the workbench
     * @return the image descriptor, or <code>null</code> if not found
     */
    public ImageDescriptor getImageDescriptor(String symbolicName);
}
