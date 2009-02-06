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

import org.eclipse.jface.viewers.CheckboxTableViewer;

/**
 * @since 3.3
 * 
 */
public class CheckboxTableViewerUpdater extends TableViewerUpdater {
	private final CheckboxTableViewer checkboxViewer;

	CheckboxTableViewerUpdater(CheckboxTableViewer viewer) {
		super(viewer);
		checkboxViewer = viewer;
	}

	public void move(Object element, int oldPosition, int newPosition) {
		if (isElementOrderPreserved()) {
			boolean wasChecked = checkboxViewer.getChecked(element);
			boolean wasGrayed = checkboxViewer.getGrayed(element);
			super.move(element, oldPosition, newPosition);
			checkboxViewer.setChecked(element, wasChecked);
			checkboxViewer.setGrayed(element, wasGrayed);
		}
	}
}
