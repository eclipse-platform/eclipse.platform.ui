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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IActionSet;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A fake view action set.
 */
public abstract class FakeActionSetDescriptor implements 
	IAdaptable, IWorkbenchAdapter, IActionSetDescriptor
{
	private String id;
	private Object data;
	private String category;
	private Object [] children;
/**
 * Construct a new action set
 */
public FakeActionSetDescriptor(String id, Object data) {
	this.id = id;
	this.data = data;
}
/**
 * Creates a new action set from this descriptor.
 * <p>
 * [Issue: Consider throwing WorkbenchException rather than CoreException.]
 * </p>
 *
 * @return the new action set
 * @exception CoreException if the action set cannot be created
 */
public IActionSet createActionSet() throws CoreException {
	return null;
}
/**
 * Returns the action image descriptor.
 */
protected abstract ImageDescriptor getActionImageDescriptor();
/**
 * Returns the action text.
 */
protected abstract String getActionLabel();
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
 * Returns the category of this action set.
 * This is the value of its <code>"category"</code> attribute.
 *
 * @return a non-empty category name or <cod>null</code> if none specified
 */
public String getCategory() {
	return category;
}
/**
 * Returns the children of this object.  When this object
 * is displayed in a tree, the returned objects will be this
 * element's children.  Returns an empty array if this
 * object has no children.
 *
 * @param object The object to get the children for.
 */
public Object[] getChildren() {
	if (children == null) {
		children = new Object[1];
		children[0] = new FakeAction(getActionLabel(), 
			getActionImageDescriptor());
	}
	return children;
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
	if (children == null) {
		children = new Object[1];
		children[0] = new FakeAction(getLabel(this), 
			getActionImageDescriptor());
	}
	return children;
}
/**
 * Returns the data
 */
public Object getData() {
	return data;
}
/**
 * Returns the description of this action set.
 * This is the value of its <code>"description"</code> attribute.
 *
 * @return the description
 */
public String getDescription() {
	return null;
}
/**
 * Returns the id of this action set. 
 * This is the value of its <code>"id"</code> attribute.
 *
 * @return the action set id
 */
public String getId() {
	return id;
}
/**
 * Returns an image descriptor to be used for displaying an object in the workbench.
 * Returns <code>null</code> if there is no appropriate image.
 *
 * @param object The object to get an image descriptor for.
 */
public ImageDescriptor getImageDescriptor(Object object) {
	return null;
}
/**
 * Returns the label text for this element.  This is typically
 * used to assign a label to this object when displayed
 * in the UI.  Returns an empty string if there is no appropriate
 * label text for this object.
 *
 * @param object The object to get a label for.
 */
public String getLabel() {
	return getActionLabel();
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
	return getActionLabel();
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
/**
 * Returns whether this action set is initially visible.
 */
public boolean isInitiallyVisible() {
	return false;
}
/**
 * Sets the category.
 */
public void setCategory(String cat) {
	category = cat;
}
}
