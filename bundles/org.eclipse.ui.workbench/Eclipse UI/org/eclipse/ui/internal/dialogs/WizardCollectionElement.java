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

import java.util.Iterator;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.IWorkbenchAdapter;
/**
 * Instances of this class are a collection of WizardCollectionElements,
 * thereby facilitating the definition of tree structures composed of 
 * these elements. Instances also store a list of wizards.
 */
public class WizardCollectionElement extends AdaptableList {
	private String id;
	private String name;
	private WizardCollectionElement	parent;
	private AdaptableList wizards = new AdaptableList();
/**
 * Creates a new <code>WizardCollectionElement</code>.  Parent can be null.
 *
 * @param name java.lang.String
 */
public WizardCollectionElement(String id, String name,WizardCollectionElement parent) {
	this.name = name;
	this.id = id;
	this.parent = parent;
}
/**
 * Adds a wizard collection to this collection.
 */
public AdaptableList add(IAdaptable a) {
	if (a instanceof WorkbenchWizardElement) {
		wizards.add(a);
	} else {
		super.add(a);
	}
	return this;
}
/**
 * Returns the wizard collection child object corresponding to the
 * passed path (relative to this object), or <code>null</code> if
 * such an object could not be found.
 *
 * @param searchPath org.eclipse.core.runtime.IPath
 * @return WizardCollectionElement
 */
public WizardCollectionElement findChildCollection(IPath searchPath) {
	Object[] children = getChildren(null);
	String searchString = searchPath.segment(0);
	for (int i = 0; i < children.length; ++i) {
		WizardCollectionElement currentCategory = (WizardCollectionElement)children[i];
		if (currentCategory.getLabel(null).equals(searchString)) {
			if (searchPath.segmentCount() == 1)
				return currentCategory;
				
			return currentCategory.findChildCollection(searchPath.removeFirstSegments(1));
		}
	}
	
	return null;
}
/**
 * Returns this collection's associated wizard object corresponding to the
 * passed id, or <code>null</code> if such an object could not be found.
 */
public WorkbenchWizardElement findWizard(String searchId,boolean recursive) {
	Object[] wizards = getWizards();
	for (int i = 0; i < wizards.length; ++i) {
		WorkbenchWizardElement currentWizard = (WorkbenchWizardElement)wizards[i];
		if (currentWizard.getID().equals(searchId))
			return currentWizard;
	}
	if(!recursive)
		return null;
	for (Iterator iterator = children.iterator(); iterator.hasNext();) {
		WizardCollectionElement child = (WizardCollectionElement) iterator.next();
		WorkbenchWizardElement result = child.findWizard(searchId,true);
		if(result != null)
			return result;
	}
	return null;
}

/**
 * Returns an object which is an instance of the given class
 * associated with this object. Returns <code>null</code> if
 * no such object can be found.
 */
public Object getAdapter(Class adapter) {
	if (adapter == IWorkbenchAdapter.class) {
		return this;
	}
	return Platform.getAdapterManager().getAdapter(this, adapter);
}
/**
 * Returns the unique ID of this element.
 */
public String getId() {
	return id;
}
/**
 * Returns the label for this collection.
 */
public String getLabel(Object o) {
	return name;
}
/**
 * Returns the logical parent of the given object in its tree.
 */
public Object getParent(Object o) {
	return parent;
}
/**
 * Returns a path representing this collection's ancestor chain.
 */
public IPath getPath() {
	if (parent == null)
		return new Path("");//$NON-NLS-1$
		
	return parent.getPath().append(name);
}
/**
 * Returns this collection element's associated collection of wizards.
 */
public Object[] getWizards() {
	return wizards.getChildren();
}
/**
 * Returns true if this element has no children and no wizards.
 */
public boolean isEmpty() {
	return size() == 0 && wizards.size() == 0;
}
/**
 * Sets this collection's unique id.
 */
public void setId(java.lang.String newId) {
	id = newId;
}
/**
 * Sets the collection of wizards associated with this collection element.
 */
public void setWizards(AdaptableList value) {
	wizards = value;
}
/**
 * For debugging purposes.
 */
public String toString() {
	StringBuffer buf = new StringBuffer("WizardCollection, ");//$NON-NLS-1$
	buf.append(children.size());
	buf.append(" children, ");//$NON-NLS-1$
	buf.append(wizards.size());
	buf.append(" wizards");//$NON-NLS-1$
	return buf.toString();
}
}
