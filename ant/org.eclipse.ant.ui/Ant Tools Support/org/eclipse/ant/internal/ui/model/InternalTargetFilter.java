/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class InternalTargetFilter extends ViewerFilter {

	private int fFiltered = 0;

	/**
	 * Returns whether the given target is an internal target. Internal targets are targets which have no description. The default target is never
	 * considered internal.
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		boolean result = true;
		if (viewer instanceof CheckboxTableViewer) {
			if (((CheckboxTableViewer) viewer).getChecked(element)) {
				// do not filter out (selected) checked items
				return true;
			}
		}
		if (element instanceof AntTargetNode) {
			result = !((AntTargetNode) element).isInternal();
		}
		if (!result) {
			fFiltered++;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#filter(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object[])
	 */
	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		fFiltered = 0;
		return super.filter(viewer, parent, elements);
	}

	public int getNumberOfTargetsFiltered() {
		return fFiltered;
	}
}
