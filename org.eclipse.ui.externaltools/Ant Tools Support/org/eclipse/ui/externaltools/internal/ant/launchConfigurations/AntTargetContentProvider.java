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
package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class AntTargetContentProvider implements IStructuredContentProvider {

	protected List elements = new ArrayList();
	protected TableViewer viewer;

	public void add(Object o) {
		elements.add(o);
		viewer.add(o);
	}
	
	public void addAll(List list) {
		elements.addAll(list);
		viewer.add(list.toArray());
	}

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (elements.isEmpty()) {
			return new Object[0];
		} else {
			return (Object[]) elements.toArray(new Object[elements.size()]);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		elements.clear();
		if (newInput != null && ((Object[]) newInput).length != 0) {
			elements.addAll(Arrays.asList((Object[]) newInput));
		}
	}
	
	/**
	 * Removes the given target from the list of targets. Has no effect if the
	 * given target does not exist in the list.
	 * 
	 * @param target the target to remove
	 */
	public void removeTarget(Object target) {
		elements.remove(target);
		viewer.remove(target);
	}

	/**
	 * Moves the given target up in the list of active targets. Has no effect if
	 * the given target is already the first target in the list or the given
	 * index is invalid.
	 * 
	 * @param index the index of the target to move up
	 */
	public void moveUpTarget(int index) {
		Object target = elements.get(index);
		if (index == 0 || target == null) {
			return;
		}
		elements.set(index, elements.get(index - 1));
		elements.set(index - 1, target);
	}

	/**
	 * Moves the given target down in the list of active targets. Has no effect
	 * if the given target is already the last target in the list or the given
	 * index is invalid.
	 *
	 * @param index the index of the target to move down
	 */
	public void moveDownTarget(int index) {
		Object target = elements.get(index);
		if (index == elements.size() - 1 || target == null) {
			return;
		}
		elements.set(index, elements.get(index + 1));
		elements.set(index + 1, target);
	}
}
