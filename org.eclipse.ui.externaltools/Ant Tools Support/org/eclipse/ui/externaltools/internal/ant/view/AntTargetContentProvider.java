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
package org.eclipse.ui.externaltools.internal.ant.view;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.externaltools.internal.ant.view.elements.TargetNode;

/**
 * Content provider which provides a list of ant targets chosen by the user 
 */
public class AntTargetContentProvider implements IStructuredContentProvider {

	/**
	 * The collection of currently active targets
	 */
	private List targets = new ArrayList();

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
	 * Removes the given target from the list of selected targets. Has no effect
	 * if the given index is invalid.
	 * 
	 * @param index the index of the the target to remove
	 */
	public void removeTarget(int index) {
		if (targets.size() > index && index >= 0) {
			targets.remove(index);
		}
	}
	
	/**
	 * Moves the given target up in the list of active targets. Has no effect if
	 * the given target is already the first target in the list or the given
	 * index is invalid.
	 * 
	 * @param index the index of the target to move up
	 */
	public void moveUpTarget(int index) {
		Object target= targets.get(index);
		if (index == 0 || target == null) {
			return;
		}
		targets.set(index, targets.get(index - 1));
		targets.set(index - 1, target);
	}
	
	/**
	 * Moves the given target down in the list of active targets. Has no effect
	 * if the given target is already the last target in the list or the given
	 * index is invalid.
	 *
	 * @param index the index of the target to move down
	 */
	public void moveDownTarget(int index) {
		Object target= targets.get(index);
		if (index == targets.size() - 1 || target == null) {
			return;
		}
		targets.set(index, targets.get(index + 1));
		targets.set(index + 1, target);
	}

}
