package org.eclipse.ui.externaltools.internal.ant.view;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;

/**
 * Content provider which provides a list of ant targets chosen by the user 
 */
public class TargetListContentProvider implements IStructuredContentProvider {

	List targets = new ArrayList();

	/**
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return targets.toArray();
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * Returns the user's currently selected targets. The list contains
	 * <code>TargetNode</code> objects.
	 * 
	 * @return List the user's currently selected targets
	 */
	public List getTargets() {
		return targets;
	}

	/**
	 * Adds the given target to the list of selected targets. Targets will
	 * appear in the list as often as they are added.
	 * 
	 * @param target the target to add
	 */
	public void addTarget(TargetNode target) {
		targets.add(target);
	}

	/**
	 * Removes the given target from the list of selected targets.
	 * 
	 * @param target the target to remove
	 */
	public void removeTarget(TargetNode target) {
		targets.remove(target);
	}
	
	/**
	 * Moves the given target up in the list of active targets. Has no effect if
	 * the given target is already the first target in the list.
	 * 
	 * @param target the target to move up
	 */
	public void moveUpTarget(TargetNode target) {
		int index = targets.indexOf(target);
		if (index == 0) {
			return;
		}
		targets.set(index, targets.get(index - 1));
		targets.set(index - 1, target);
	}
	
	/**
	 * Moves the given target down in the list of active targets. Has no effect
	 * if the given target is already the last target in the list.
	 *
	 * @param target the target to move down
	 */
	public void moveDownTarget(TargetNode target) {
		int index = targets.indexOf(target);
		if (index == targets.size() - 1) {
			return;
		}
		targets.set(index, targets.get(index + 1));
		targets.set(index + 1, target);
	}

}
