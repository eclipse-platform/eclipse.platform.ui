/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.cheatsheets.composite.explorer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheetTask;
import org.eclipse.ui.cheatsheets.ICompositeCheatSheet;

class TreeContentProvider implements ITreeContentProvider {
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ICompositeCheatSheet) {
			final Object[] rootTask = {((ICompositeCheatSheet) parentElement).getRootTask()};
			return rootTask;
		}
		if (parentElement instanceof ICompositeCheatSheetTask)
			return ((ICompositeCheatSheetTask) parentElement).getSubtasks();
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ICompositeCheatSheet)
			return true;
		if (element instanceof ICompositeCheatSheetTask)
			return ((ICompositeCheatSheetTask) element).getSubtasks().length > 0;
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
