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
package org.eclipse.debug.internal.ui.preferences;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.debug.internal.ui.preferences.DebugActionGroupsManager.DebugActionGroup;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DebugActionGroupsContentProvider implements IStructuredContentProvider {
	
	private CheckboxTableViewer fViewer;
	
	public DebugActionGroupsContentProvider(CheckboxTableViewer viewer) {
		fViewer = viewer;
		populateTable();
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object element) {
		Collection allViewActionSets =
			DebugActionGroupsManager.getDefault().fDebugActionGroups.values();
		return allViewActionSets.toArray();
	}
	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	protected void populateTable() {
		Collection allViewActionSets =
			DebugActionGroupsManager.getDefault().fDebugActionGroups.values();
		Iterator iterator = allViewActionSets.iterator();
		while (iterator.hasNext()) {
			DebugActionGroup set = (DebugActionGroup) iterator.next();
			fViewer.add(set);
			fViewer.setChecked(set, set.isVisible());
		}
	}
}
