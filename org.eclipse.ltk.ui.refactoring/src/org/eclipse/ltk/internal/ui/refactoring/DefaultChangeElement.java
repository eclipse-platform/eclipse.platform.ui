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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;

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
	
	public void feedInput(IChangePreviewViewer viewer, List categories) throws CoreException {
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
		if (fChildren == null) {
			if (fChange instanceof CompositeChange) {
				List children= new ArrayList();
				getFlattendedChildren(children, this, (CompositeChange)fChange);
				fChildren= (ChangeElement[])children.toArray(new ChangeElement[children.size()]);
			} else {
				IChangeElementChildrenCreator creator= (IChangeElementChildrenCreator)fChange.getAdapter(IChangeElementChildrenCreator.class);
				if (creator != null) {
					// sets the children as a side effect
					creator.createChildren(this);
				} else if (fChange instanceof TextEditBasedChange) {
					TextEditBasedChangeGroup[] groups= getSortedChangeGroups((TextEditBasedChange)fChange);
					fChildren= new ChangeElement[groups.length];
					for (int i= 0; i < groups.length; i++) {
						fChildren[i]= new TextEditChangeElement(this, groups[i]);
					}
				} else {
					fChildren= EMPTY_CHILDREN;
				}
			}
		}
		return fChildren;
	}
	
	public boolean hasOneGroupCategory(List categories) {
		if (fChange instanceof TextEditBasedChange) {
			return ((TextEditBasedChange)fChange).hasOneGroupCategory(categories);
		} else {
			ChangeElement[] children= getChildren();
			for (int i= 0; i < children.length; i++) {
				if (children[i].hasOneGroupCategory(categories))
					return true;
			}
			return false;
		}
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
	
	private static class OffsetComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			TextEditBasedChangeGroup c1= (TextEditBasedChangeGroup)o1;
			TextEditBasedChangeGroup c2= (TextEditBasedChangeGroup)o2;
			int p1= getOffset(c1);
			int p2= getOffset(c2);
			if (p1 < p2)
				return -1;
			if (p1 > p2)
				return 1;
			// same offset
			return 0;	
		}
		private int getOffset(TextEditBasedChangeGroup edit) {
			return edit.getRegion().getOffset();
		}
	}
	
	private TextEditBasedChangeGroup[] getSortedChangeGroups(TextEditBasedChange change) {
		TextEditBasedChangeGroup[] groups= change.getChangeGroups();
		List result= new ArrayList(groups.length);
		for (int i= 0; i < groups.length; i++) {
			if (!groups[i].getTextEditGroup().isEmpty())
				result.add(groups[i]);
		}
		Comparator comparator= new OffsetComparator();
		Collections.sort(result, comparator);
		return (TextEditBasedChangeGroup[])result.toArray(new TextEditBasedChangeGroup[result.size()]);
	}
	
	private void getFlattendedChildren(List result, DefaultChangeElement parent, CompositeChange focus) {
		Change[] changes= focus.getChildren();
		for (int i= 0; i < changes.length; i++) {
			Change change= changes[i];
			if (change instanceof CompositeChange && ((CompositeChange)change).isSynthetic()) {
				getFlattendedChildren(result, parent, (CompositeChange)change);
			} else {
				result.add(new DefaultChangeElement(parent, change));
			}
		}
	}
}

