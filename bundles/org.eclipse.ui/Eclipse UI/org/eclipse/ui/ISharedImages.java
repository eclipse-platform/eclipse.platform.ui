package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.WorkbenchImages;
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
	public final static String IMG_OBJ_FILE = "IMG_OBJ_FILE";

	/**
	 * Identifies a folder image.
	 */
	public final static String IMG_OBJ_FOLDER = "IMG_OBJ_FOLDER";

	/**
	 * Identifies a project image.
	 */
	public final static String IMG_OBJ_PROJECT = "IMG_OBJ_PROJECT";

	/**
	 * Identifies a closed project image.
	 */
	public final static String IMG_OBJ_PROJECT_CLOSED = "IMG_OBJ_PROJECT_CLOSED";
	
	/**
	 * Identifies an element image.
	 */
	public final static String IMG_OBJ_ELEMENT = "IMG_OBJ_ELEMENTS";

	/**
	 * Identifies the image used for "open marker".
	 */
	public final static String IMG_OPEN_MARKER = "IMG_OPEN_MARKER";
	
	/**
	 * Identifies the default image used for views.
	 */
	public final static String IMG_DEF_VIEW = "IMG_DEF_VIEW";

	/**
	 * Identifies the default image used to indicate errors.
	 */
	public final static String IMG_OBJS_ERROR_TSK = "IMG_OBJS_ERROR_TSK";

	/**
	 * Identifies the default image used to indicate warnings.
	 */
	public final static String IMG_OBJS_WARN_TSK = "IMG_OBJS_WARN_TSK";

	/**
	 * Identifies the default image used to indicate information only.
	 */
	public final static String IMG_OBJS_INFO_TSK = "IMG_OBJS_INFO_TSK";

	/**
	 * Identifies the default image used to indicate a task.
	 */
	public final static String IMG_OBJS_TASK_TSK = "IMG_OBJS_TASK_TSK";
	/**
	 * Identifies the default image used to indicate a bookmark.
	 */
	public final static String IMG_OBJS_BKMRK_TSK = "IMG_OBJS_BKMRK_TSK";
/**
 * Retrieves the specified image from the workbench plugin's image registry.
 * Note: The returned <code>Image</code> is managed by the workbench; clients
 * must <b>not</b> dispose of the returned image.
 *
 * @param symbolicName the symbolic name of the image (constants found in
 *    this interface)
 * @return the image, or <code>null</code> if not found
 */
public Image getImage(String symbolicName);
/**
 * Retrieves the image descriptor for specified image from the workbench's
 * image registry. Unlike <code>Image</code>s, image descriptors themselves do
 * not need to be disposed.
 *
 * @param symbolicName the symbolic name of the image (constants found in
 *    this interface)
 * @return the image descriptor, or <code>null</code> if not found
 */
public ImageDescriptor getImageDescriptor(String symbolicName);
}
