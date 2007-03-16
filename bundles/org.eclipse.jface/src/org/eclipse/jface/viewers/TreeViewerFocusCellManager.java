/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @since 3.3
 *
 */
public class TreeViewerFocusCellManager extends SWTFocusCellManager {
	private static final CellNavigationStrategy TREE_NAVIGATE = new CellNavigationStrategy() {
		public void collapse(ColumnViewer viewer, ViewerCell cellToCollapse,
				Event event) {
			if (cellToCollapse != null) {
				((TreeItem) cellToCollapse.getItem()).setExpanded(false);
			}
		}

		public void expand(ColumnViewer viewer, ViewerCell cellToExpand,
				Event event) {
			if (cellToExpand != null) {
				TreeViewer v = (TreeViewer) viewer;
				v.setExpandedState(v
						.getTreePathFromItem((Item)cellToExpand.getItem()), true);
			}
		}

		public boolean isCollapseEvent(ColumnViewer viewer,
				ViewerCell cellToCollapse, Event event) {
			return cellToCollapse != null
					&& ((TreeItem) cellToCollapse.getItem()).getExpanded()
					&& cellToCollapse.getColumnIndex() == 0
					&& event.keyCode == SWT.ARROW_LEFT;
		}

		public boolean isExpandEvent(ColumnViewer viewer,
				ViewerCell cellToExpand, Event event) {
			return cellToExpand != null
					&& ((TreeItem) cellToExpand.getItem()).getItemCount() > 0
					&& !((TreeItem) cellToExpand.getItem()).getExpanded()
					&& cellToExpand.getColumnIndex() == 0
					&& event.keyCode == SWT.ARROW_RIGHT;
		}
	};
	
	/**
	 * @param viewer
	 * @param focusDrawingDelegate
	 */
	public TreeViewerFocusCellManager(TreeViewer viewer,
			FocusCellHighlighter focusDrawingDelegate) {
		super(viewer, focusDrawingDelegate,TREE_NAVIGATE);
	}

	ViewerCell getInitialFocusCell() {
		Tree tree = (Tree) getViewer().getControl();
		
		if( tree.getItemCount() > 0 ) {
			return getViewer().getViewerRowFromItem(tree.getItem(0)).getCell(0);
		}
		
		return null;
	}
}
