/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 226292)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.CheckboxTreeViewer;

/**
 * @since 3.3
 * 
 */
public class CheckboxTreeViewerUpdater extends TreeViewerUpdater {
	private final CheckboxTreeViewer checkboxViewer;

	CheckboxTreeViewerUpdater(CheckboxTreeViewer viewer) {
		super(viewer);
		checkboxViewer = viewer;
	}

	public void move(Object parent, Object element, int oldPosition,
			int newPosition) {
		if (isElementOrderPreserved()) {
			boolean wasChecked = checkboxViewer.getChecked(element);
			boolean wasGrayed = checkboxViewer.getGrayed(element);
			super.move(parent, element, oldPosition, newPosition);
			checkboxViewer.setChecked(element, wasChecked);
			checkboxViewer.setGrayed(element, wasGrayed);
		}
	}
}
