/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Fraenkel (fraenkel@us.ibm.com) - contributed a fix for:
 *       o Go to next match on last match does nothing
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=58311)
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;
import org.eclipse.jface.viewers.TableViewer;

public class TableViewerNavigator implements INavigate {
	private TableViewer fViewer;
	public TableViewerNavigator(TableViewer viewer) {
		fViewer = viewer;
	}
	public void navigateNext(boolean forward) {
		int itemCount = fViewer.getTable().getItemCount();
		if (itemCount == 0)
			return;
		int[] selection = fViewer.getTable().getSelectionIndices();
		int nextIndex = 0;
		if (selection.length > 0) {
			if (forward) {
				nextIndex = selection[selection.length - 1] + 1;
				if (nextIndex >= itemCount)
					nextIndex = 0;
			} else {
				nextIndex = selection[0] - 1;
				if (nextIndex < 0)
					nextIndex = itemCount - 1;
			}
		}
		fViewer.getTable().setSelection(nextIndex);
		fViewer.getTable().showSelection();
	}
}
