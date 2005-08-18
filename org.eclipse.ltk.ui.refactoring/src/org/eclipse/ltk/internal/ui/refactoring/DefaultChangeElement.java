/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.util.Assert;

import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

public class DefaultChangeElement extends ChangeElement {
	
	private Change fChange;
	private ChangeElement[] fChildren;

	/**
	 * Creates a new <code>DefaultChangeElement</code> for the given
	 * change.
	 * 
	 * @param parent the change element's parent or <code>null
	 * 	</code> if the change element doesn't have a parent
	 * @param change the actual change. Argument must not be
	 * 	<code>null</code>
	 */
	public DefaultChangeElement(ChangeElement parent, Change change) {
		super(parent);
		fChange= change;
		Assert.isNotNull(fChange);
	}

	/**
	 * Returns the underlying <code>IChange</code> object.
	 * 
	 * @return the underlying change
	 */
	public Change getChange() {
		return fChange;
	}
	
	public Object getModifiedElement() {
		return fChange;
	}
	
	public ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException {
		return ChangePreviewViewerDescriptor.get(fChange);
	}
	
	public void feedInput(IChangePreviewViewer viewer) throws CoreException {
		viewer.setInput(new ChangePreviewViewerInput(fChange));
	}
	
	/* non Java-doc
	 * @see ChangeElement#setActive
	 */
	public void setEnabled(boolean enabled) {
		fChange.setEnabled(enabled);
	}
	
	public void setEnabledShallow(boolean enabled) {
		fChange.setEnabledShallow(enabled);
	}
	
	/* non Java-doc
	 * @see ChangeElement.getActive
	 */
	public int getActive() {
		if (fChange instanceof CompositeChange || fChange instanceof TextEditBasedChange)
			return getCompositeChangeActive();
		else
			return getDefaultChangeActive();
	}
	
	/* non Java-doc
	 * @see ChangeElement.getChildren
	 */	
	public ChangeElement[] getChildren() {
		return fChildren;
	}
	
	/**
	 * Sets the children.
	 * 
	 * @param children the children of this node. Must not be <code>null</code>
	 */
	public void setChildren(ChangeElement[] children) {
		Assert.isNotNull(children);
		fChildren= children;
	}

	private int getDefaultChangeActive() {
		int result= fChange.isEnabled() ? ACTIVE : INACTIVE;
		if (fChildren != null) {
			for (int i= 0; i < fChildren.length; i++) {
				result= ACTIVATION_TABLE[fChildren[i].getActive()][result];
				if (result == PARTLY_ACTIVE)
					break;
			}
		}
		return result;
	}
	
	private int getCompositeChangeActive() {		
		if (fChildren != null && fChildren.length > 0) {
			int result= fChildren[0].getActive();
			for (int i= 1; i < fChildren.length; i++) {
				result= ACTIVATION_TABLE[fChildren[i].getActive()][result];
				if (result == PARTLY_ACTIVE)
					break;
			}
			return result;
		} else {
			return ACTIVE;
		}
	}
}

