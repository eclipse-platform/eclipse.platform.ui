/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * A registry for common images used by the workbench which may be useful to
 * other plug-ins.
 * <p>
 * This class provides <code>Image</code> and <code>ImageDescriptor</code>s for
 * each named image in the interface. All <code>Image</code> objects provided by
 * this class are managed by this class and must never be disposed by other
 * clients.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ISharedImages {
	/**
	 * Identifies the error overlay image.
	 *
	 * @since 3.4
	 */
	String IMG_DEC_FIELD_ERROR = "IMG_DEC_FIELD_ERROR"; //$NON-NLS-1$

	/**
	 * Identifies the warning overlay image.
	 *
	 * @since 3.4
	 */
	String IMG_DEC_FIELD_WARNING = "IMG_DEC_FIELD_WARNING"; //$NON-NLS-1$

	/**
	 * Identifies the default image used for views.
	 */
	String IMG_DEF_VIEW = "IMG_DEF_VIEW"; //$NON-NLS-1$

	/**
	 * Identifies the collapse all image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_COLLAPSEALL = "IMG_ELCL_COLLAPSEALL"; //$NON-NLS-1$

	/**
	 * Identifies the collapse all image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_COLLAPSEALL_DISABLED = "IMG_ELCL_COLLAPSEALL_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the remove image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_REMOVE = "IMG_ELCL_REMOVE"; //$NON-NLS-1$

	/**
	 * Identifies the remove image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_REMOVE_DISABLED = "IMG_ELCL_REMOVE_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the remove all image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_REMOVEALL = "IMG_ELCL_REMOVEALL"; //$NON-NLS-1$

	/**
	 * Identifies the remove all image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_REMOVEALL_DISABLED = "IMG_ELCL_REMOVEALL_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the stop image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_STOP = "IMG_ELCL_STOP"; //$NON-NLS-1$

	/**
	 * Identifies the stop image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_STOP_DISABLED = "IMG_ELCL_STOP_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the synchronize image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_SYNCED = "IMG_ELCL_SYNCED"; //$NON-NLS-1$

	/**
	 * Identifies the synchronize image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ELCL_SYNCED_DISABLED = "IMG_ELCL_SYNCED_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the clear image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_CLEAR = "IMG_ETOOL_CLEAR"; //$NON-NLS-1$

	/**
	 * Identifies the clear image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_CLEAR_DISABLED = "IMG_ETOOL_CLEAR_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the default perspective image.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_DEF_PERSPECTIVE = "IMG_ETOOL_DEF_PERSPECTIVE"; //$NON-NLS-1$

	/**
	 * Identifies the delete image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_DELETE = "IMG_ETOOL_DELETE"; //$NON-NLS-1$

	/**
	 * Identifies the delete image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_DELETE_DISABLED = "IMG_ETOOL_DELETE_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the home image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_HOME_NAV = "IMG_ETOOL_HOME_NAV"; //$NON-NLS-1$

	/**
	 * Identifies the home image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_HOME_NAV_DISABLED = "IMG_ETOOL_HOME_NAV_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the print image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_PRINT_EDIT = "IMG_ETOOL_PRINT_EDIT"; //$NON-NLS-1$

	/**
	 * Identifies the print image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_PRINT_EDIT_DISABLED = "IMG_ETOOL_PRINT_EDIT_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the save image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_SAVE_EDIT = "IMG_ETOOL_SAVE_EDIT"; //$NON-NLS-1$

	/**
	 * Identifies the save image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_SAVE_EDIT_DISABLED = "IMG_ETOOL_SAVE_EDIT_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the save all image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_SAVEALL_EDIT = "IMG_ETOOL_SAVEALL_EDIT"; //$NON-NLS-1$

	/**
	 * Identifies the save all image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_SAVEALL_EDIT_DISABLED = "IMG_ETOOL_SAVEALL_EDIT_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the save as image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_SAVEAS_EDIT = "IMG_ETOOL_SAVEAS_EDIT"; //$NON-NLS-1$

	/**
	 * Identifies the save as image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_ETOOL_SAVEAS_EDIT_DISABLED = "IMG_ETOOL_SAVEAS_EDIT_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the help image.
	 *
	 * @since 3.4
	 */
	String IMG_LCL_LINKTO_HELP = "IMG_LCL_LINKTO_HELP"; //$NON-NLS-1$

	/**
	 * Identifies the add image.
	 *
	 * @since 3.4
	 */
	String IMG_OBJ_ADD = "IMG_OBJ_ADD"; //$NON-NLS-1$

	/**
	 * Identifies an element image.
	 */
	String IMG_OBJ_ELEMENT = "IMG_OBJ_ELEMENTS"; //$NON-NLS-1$

	/**
	 * Identifies a file image.
	 */
	String IMG_OBJ_FILE = "IMG_OBJ_FILE"; //$NON-NLS-1$

	/**
	 * Identifies a folder image.
	 */
	String IMG_OBJ_FOLDER = "IMG_OBJ_FOLDER"; //$NON-NLS-1$

	/**
	 * Identifies a project image.
	 *
	 * @deprecated in 3.0. This image is IDE-specific, and is therefore found only
	 *             in IDE configurations. IDE-specific tools should use
	 *             <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT</code>
	 *             instead.
	 */
	@Deprecated
	String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT"; //$NON-NLS-1$

	/**
	 * Identifies a closed project image.
	 *
	 * @deprecated in 3.0. This image is IDE-specific, and is therefore found only
	 *             in IDE configurations. IDE-specific tools should use
	 *             <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED</code>
	 *             instead.
	 */
	@Deprecated
	String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED"; //$NON-NLS-1$

	/**
	 * Identifies the default image used to indicate a bookmark.
	 *
	 * @deprecated in 3.0. This image is IDE-specific, and is therefore found only
	 *             in IDE configurations. IDE-specific tools should use
	 *             <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJS_BKMRK_TSK</code>
	 *             instead.
	 */
	@Deprecated
	String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK"; //$NON-NLS-1$

	/**
	 * Identifies the default image used to indicate errors.
	 */
	String IMG_OBJS_ERROR_TSK = "IMG_OBJS_ERROR_TSK"; //$NON-NLS-1$

	/**
	 * Identifies the default image used to indicate information only.
	 */
	String IMG_OBJS_INFO_TSK = "IMG_OBJS_INFO_TSK"; //$NON-NLS-1$

	/**
	 * Identifies the default image used to indicate a task.
	 *
	 * @deprecated in 3.0. This image is IDE-specific, and is therefore found only
	 *             in IDE configurations. IDE-specific tools should use
	 *             <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJS_TASK_TSK</code>
	 *             instead.
	 */
	@Deprecated
	String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK"; //$NON-NLS-1$

	/**
	 * Identifies the default image used to indicate warnings.
	 */
	String IMG_OBJS_WARN_TSK = "IMG_OBJS_WARN_TSK"; //$NON-NLS-1$

	/**
	 * Identifies the image used for "open marker".
	 *
	 * @deprecated in 3.0. This image is IDE-specific, and is therefore found only
	 *             in IDE configurations. IDE-specific tools should use
	 *             <code>org.eclipse.ui.ide.IDE.SharedImages.IMG_OPEN_MARKER</code>
	 *             instead.
	 */
	@Deprecated
	String IMG_OPEN_MARKER = "IMG_OPEN_MARKER"; //$NON-NLS-1$

	/**
	 * Identifies the back image in the enabled state.
	 */
	String IMG_TOOL_BACK = "IMG_TOOL_BACK"; //$NON-NLS-1$

	/**
	 * Identifies the back image in the disabled state.
	 */
	String IMG_TOOL_BACK_DISABLED = "IMG_TOOL_BACK_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the back image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_BACK</code>.
	 *             Enabled images are now in color. The workbench itself no longer
	 *             uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_BACK_HOVER = "IMG_TOOL_BACK_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the copy image in the enabled state.
	 */
	String IMG_TOOL_COPY = "IMG_TOOL_COPY"; //$NON-NLS-1$

	/**
	 * Identifies the copy image in the disabled state.
	 */
	String IMG_TOOL_COPY_DISABLED = "IMG_TOOL_COPY_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the copy image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_COPY</code>.
	 *             Enabled images are now in color. The workbench itself no longer
	 *             uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_COPY_HOVER = "IMG_TOOL_COPY_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the cut image in the enabled state.
	 */
	String IMG_TOOL_CUT = "IMG_TOOL_CUT"; //$NON-NLS-1$

	/**
	 * Identifies the cut image in the disabled state.
	 */
	String IMG_TOOL_CUT_DISABLED = "IMG_TOOL_CUT_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the cut image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_CUT</code>.
	 *             Enabled images are now in color. The workbench itself no longer
	 *             uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_CUT_HOVER = "IMG_TOOL_CUT_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the delete image in the enabled state.
	 *
	 * @since 3.4
	 */
	String IMG_TOOL_DELETE = "IMG_TOOL_DELETE"; //$NON-NLS-1$

	/**
	 * Identifies the delete image in the disabled state.
	 *
	 * @since 3.4
	 */
	String IMG_TOOL_DELETE_DISABLED = "IMG_TOOL_DELETE_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the delete image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as
	 *             <code>IMG_TOOL_DELETE</code>. Enabled images are now in color.
	 *             The workbench itself no longer uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_DELETE_HOVER = "IMG_TOOL_DELETE_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the forward image in the enabled state.
	 */
	String IMG_TOOL_FORWARD = "IMG_TOOL_FORWARD"; //$NON-NLS-1$

	/**
	 * Identifies the forward image in the disabled state.
	 */
	String IMG_TOOL_FORWARD_DISABLED = "IMG_TOOL_FORWARD_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the forward image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as
	 *             <code>IMG_TOOL_FORWARD</code>. Enabled images are now in color.
	 *             The workbench itself no longer uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_FORWARD_HOVER = "IMG_TOOL_FORWARD_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the new wizard image in the enabled state.
	 */
	String IMG_TOOL_NEW_WIZARD = "IMG_TOOL_NEW_WIZARD"; //$NON-NLS-1$

	/**
	 * Identifies the new wizard image in the disabled state.
	 */
	String IMG_TOOL_NEW_WIZARD_DISABLED = "IMG_TOOL_NEW_WIZARD_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the new wizard image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as
	 *             <code>IMG_TOOL_NEW_WIZARD</code>. Enabled images are now in
	 *             color. The workbench itself no longer uses the hover image
	 *             variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_NEW_WIZARD_HOVER = "IMG_TOOL_NEW_WIZARD_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the paste image in the enabled state.
	 */
	String IMG_TOOL_PASTE = "IMG_TOOL_PASTE"; //$NON-NLS-1$

	/**
	 * Identifies the paste image in the disabled state.
	 */
	String IMG_TOOL_PASTE_DISABLED = "IMG_TOOL_PASTE_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the paste image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as
	 *             <code>IMG_TOOL_PASTE</code>. Enabled images are now in color. The
	 *             workbench itself no longer uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_PASTE_HOVER = "IMG_TOOL_PASTE_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the redo image in the enabled state.
	 */
	String IMG_TOOL_REDO = "IMG_TOOL_REDO"; //$NON-NLS-1$

	/**
	 * Identifies the redo image in the disabled state.
	 */
	String IMG_TOOL_REDO_DISABLED = "IMG_TOOL_REDO_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the redo image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_REDO</code>.
	 *             Enabled images are now in color. The workbench itself no longer
	 *             uses the hover image variants.
	 *
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_REDO_HOVER = "IMG_TOOL_REDO_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the undo image in the enabled state.
	 */
	String IMG_TOOL_UNDO = "IMG_TOOL_UNDO"; //$NON-NLS-1$

	/**
	 * Identifies the undo image in the disabled state.
	 */
	String IMG_TOOL_UNDO_DISABLED = "IMG_TOOL_UNDO_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the undo image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_UNDO</code>.
	 *             Enabled images are now in color. The workbench itself no longer
	 *             uses the hover image variants.
	 *
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_UNDO_HOVER = "IMG_TOOL_UNDO_HOVER"; //$NON-NLS-1$

	/**
	 * Identifies the up image in the enabled state.
	 */
	String IMG_TOOL_UP = "IMG_TOOL_UP"; //$NON-NLS-1$

	/**
	 * Identifies the up image in the disabled state.
	 */
	String IMG_TOOL_UP_DISABLED = "IMG_TOOL_UP_DISABLED"; //$NON-NLS-1$

	/**
	 * Identifies the up image in the hover (colored) state.
	 *
	 * @deprecated in 3.0. This image is now the same as <code>IMG_TOOL_UP</code>.
	 *             Enabled images are now in color. The workbench itself no longer
	 *             uses the hover image variants.
	 * @noreference Marked for deletion see
	 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=559593
	 */
	@Deprecated
	String IMG_TOOL_UP_HOVER = "IMG_TOOL_UP_HOVER"; //$NON-NLS-1$

	// The following set of constants represent the image pairs that are used
	// to construct cursors for drag and drop operations within the workbench
	// Each cursor is represented by two images; the 'source' and the 'mask'
	// These need to be combined using the following code snippet:
	// source = getImageDescriptor(sourceId);
	// mask = getImageDescriptor(maskId);
	// cursor = new Cursor(display, source.getImageData(), mask.getImageData(), 16,
	// 16);

	/**
	 * Cursor 'source' for the left arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_LEFT_SOURCE = "IMG_OBJS_DND_LEFT_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the left arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_LEFT_MASK = "IMG_OBJS_DND_LEFT_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the right arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_RIGHT_SOURCE = "IMG_OBJS_DND_RIGHT_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the right arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_RIGHT_MASK = "IMG_OBJS_DND_RIGHT_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the up arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_TOP_SOURCE = "IMG_OBJS_DND_TOP_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the up arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_TOP_MASK = "IMG_OBJS_DND_TOP_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the down arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_BOTTOM_SOURCE = "IMG_OBJS_DND_BOTTOM_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the down arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_BOTTOM_MASK = "IMG_OBJS_DND_BOTTOM_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the 'no drop' arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_INVALID_SOURCE = "IMG_OBJS_DND_INVALID_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the 'no drop' arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_INVALID_MASK = "IMG_OBJS_DND_INVALID_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the 'in stack' arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_STACK_SOURCE = "IMG_OBJS_DND_STACK_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the 'in stack' arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_STACK_MASK = "IMG_OBJS_DND_STACK_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the 'off-screen' (detached window) arrow cursor. For
	 * cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_OFFSCREEN_SOURCE = "IMG_OBJS_DND_OFFSCREEN_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the 'off-screen' (detached window) arrow cursor. For cursor
	 * construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_OFFSCREEN_MASK = "IMG_OBJS_DND_OFFSCREEN_MASK"; //$NON-NLS-1$

	/**
	 * Cursor 'source' for the 'fast-view' arrow cursor. For cursor construction
	 * see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_TOFASTVIEW_SOURCE = "IMG_OBJS_DND_TOFASTVIEW_SOURCE"; //$NON-NLS-1$
	/**
	 * Cursor 'mask' for the 'fast-view' arrow cursor. For cursor construction see:
	 *
	 * @see Cursor#Cursor(Device, ImageData, ImageData, int, int)
	 * @since 3.5
	 */
	String IMG_OBJS_DND_TOFASTVIEW_MASK = "IMG_OBJS_DND_TOFASTVIEW_MASK"; //$NON-NLS-1$

	/**
	 * Retrieves the specified image from the workbench plugin's image registry.
	 * Note: The returned <code>Image</code> is managed by the workbench; clients
	 * must <b>not</b> dispose of the returned image.
	 *
	 * @param symbolicName the symbolic name of the image; there are constants
	 *                     declared in this interface for build-in images that come
	 *                     with the workbench
	 * @return the image, or <code>null</code> if not found
	 */
	Image getImage(String symbolicName);

	/**
	 * Retrieves the image descriptor for specified image from the workbench's image
	 * registry. Unlike <code>Image</code>s, image descriptors themselves do not
	 * need to be disposed.
	 *
	 * @param symbolicName the symbolic name of the image; there are constants
	 *                     declared in this interface for build-in images that come
	 *                     with the workbench
	 * @return the image descriptor, or <code>null</code> if not found
	 */
	ImageDescriptor getImageDescriptor(String symbolicName);
}
