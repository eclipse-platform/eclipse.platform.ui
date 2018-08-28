/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
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
