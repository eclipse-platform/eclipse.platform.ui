package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;
import java.util.*;

/**
 * A fake action for the action set dialog.
 */
public class FakeAction implements IAdaptable, IWorkbenchAdapter {
	private String label;
	private ImageDescriptor imageDesc;
/**
 * ActionSetContent constructor comment.
 */
public FakeAction(String label, ImageDescriptor imageDesc) {
	this.label = label;
	this.imageDesc = imageDesc;
}
/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) 
		return this;
	return null;
}
/**
 * Returns the children of this object.  When this object
 * is displayed in a tree, the returned objects will be this
 * element's children.  Returns an empty array if this
 * object has no children.
 *
 * @param object The object to get the children for.
 */
public Object[] getChildren(Object o) {
	return new Object[0];
}
/**
 * Returns an image descriptor to be used for displaying an object in the workbench.
 * Returns <code>null</code> if there is no appropriate image.
 *
 * @param object The object to get an image descriptor for.
 */
public ImageDescriptor getImageDescriptor(Object object) {
	return imageDesc;
}
/**
 * Returns the label text for this element.  This is typically
 * used to assign a label to this object when displayed
 * in the UI.  Returns an empty string if there is no appropriate
 * label text for this object.
 *
 * @param object The object to get a label for.
 */
public String getLabel(Object o) {
	return label;
}
/**
 * Returns the logical parent of the given object in its tree.
 * Returns <code>null</code> if there is no parent, or if this object doesn't
 * belong to a tree.
 *
 * @param object The object to get the parent for.
 */
public Object getParent(Object o) {
	return null;
}
}
