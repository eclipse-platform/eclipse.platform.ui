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

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.registry.ActionSetCategory;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

public class ActionSetContentProvider implements ITreeContentProvider {
	private ActionSetDialogInput input;
/**
 * ActionSetRegistryContentProvider constructor comment.
 */
public ActionSetContentProvider() {
	super();
}
public void dispose() {
	input = null;
}
/**
 * Returns the child elements of the given parent element.
 */
public Object[] getChildren(Object element) {
	if (element instanceof ActionSetDialogInput) {
		return ((ActionSetDialogInput)element).getCategories();
	}
	if (element instanceof ActionSetCategory) {
		ActionSetCategory cat = (ActionSetCategory)element;
		ArrayList list = cat.getActionSets();
		if (list == null)
			return new Object[0];
		else
			return list.toArray();
	}
	return new Object[0];
}
/**
 * Return the children of an element.
 */
public Object[] getElements(Object element) {
	return getChildren(element);
}
/**
 * Returns the parent for the given element, or <code>null</code> 
 * indicating that the parent can't be computed. 
 */
public Object getParent(Object element) {
	if (element instanceof ActionSetCategory)
		return input;
	if (element instanceof IActionSetDescriptor) {
		IActionSetDescriptor desc = (IActionSetDescriptor)element;
		if (input != null) {
			return input.findCategory(desc.getCategory());
		}
	}
	return null;
}
/**
 * Returns whether the given element has children.
 */
public boolean hasChildren(Object element) {
	if (element instanceof ActionSetDialogInput)
		return true;
	if (element instanceof ActionSetCategory)
		return true;
	return false;
}
/**
 * Sets the input.
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	if (newInput instanceof ActionSetDialogInput) {
		input = (ActionSetDialogInput)newInput;
	}
}
}
