package org.eclipse.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import java.util.Iterator;

/**
 * This adapter interface provides visual presentation and hierarchical structure
 * for workbench elements, allowing them to be displayed in the UI
 * without having to know the concrete type of the element.
 * <p>
 * There is an associate label provider and content provider for showing
 * elements with a registered workbench adapter in JFace structured viewers.
 * </p>
 * @see WorkbenchLabelProvider
 * @see WorkbenchContentProvider
 */
public interface IWorkbenchAdapter {
/**
 * Returns the children of this object.  When this object
 * is displayed in a tree, the returned objects will be this
 * element's children.  Returns an empty array if this
 * object has no children.
 *
 * @param object The object to get the children for.
 */
public Object[] getChildren(Object o);
/**
 * Returns an image descriptor to be used for displaying an object in the workbench.
 * Returns <code>null</code> if there is no appropriate image.
 *
 * @param object The object to get an image descriptor for.
 */
public ImageDescriptor getImageDescriptor(Object object);
/**
 * Returns the label text for this element.  This is typically
 * used to assign a label to this object when displayed
 * in the UI.  Returns an empty string if there is no appropriate
 * label text for this object.
 *
 * @param object The object to get a label for.
 */
public String getLabel(Object o);
/**
 * Returns the logical parent of the given object in its tree.
 * Returns <code>null</code> if there is no parent, or if this object doesn't
 * belong to a tree.
 *
 * @param object The object to get the parent for.
 */
public Object getParent(Object o);
}
