/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                               - fix for bug 178280, 183999, 184609
 * 	   Ruediger Herrmann <ruediger.herrmann@gmx.de> - fix for bug 395890
 *     IBM Corporation - API refactoring and general maintenance
 *******************************************************************************/

package org.eclipse.jface.layout;


import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * The TreeColumnLayout is the {@link Layout} used to maintain {@link TreeColumn} sizes in a
 * {@link Tree}.
 *
 * <p>
 * <b>You can only add the {@link Layout} to a container whose <i>only</i>
 * child is the {@link Tree} control you want the {@link Layout} applied to.
 * Don't assign the layout directly the {@link Tree}</b>
 * </p>
 *
 * @since 3.3
 */
public class TreeColumnLayout extends AbstractColumnLayout {
	private boolean addListener = true;

	private static class TreeLayoutListener implements TreeListener {

		@Override
		public void treeCollapsed(TreeEvent e) {
			update((Tree) e.widget);
		}

		@Override
		public void treeExpanded(TreeEvent e) {
			update((Tree) e.widget);
		}

		private void update(final Tree tree) {
			tree.getDisplay().asyncExec(() -> {
				if (!tree.isDisposed()) {
					tree.update();
					tree.getParent().layout();
				}
			});
		}

	}

	private static final TreeLayoutListener listener = new TreeLayoutListener();

	/**
	 * Creates a new tree column layout.
	 */
	public TreeColumnLayout() {
	}

	/**
	 * Creates a new tree column layout.
	 *
	 * @param adjustForScrollBar
	 *            <code>true</code> if the layout should reserve space for the
	 *            vertical scroll bar
	 * @since 3.12
	 */
	public TreeColumnLayout(boolean adjustForScrollBar) {
		super(adjustForScrollBar);
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		super.layout(composite, flushCache);
		if( addListener ) {
			addListener=false;
			((Tree)getControl(composite)).addTreeListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.5
	 */
	@Override
	protected int getColumnCount(Scrollable tree) {
		return ((Tree) tree).getColumnCount();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.5
	 */
	@Override
	protected void setColumnWidths(Scrollable tree, int[] widths) {
		TreeColumn[] columns = ((Tree) tree).getColumns();
		for (int i = 0; i < widths.length; i++) {
			columns[i].setWidth(widths[i]);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.5
	 */
	@Override
	protected ColumnLayoutData getLayoutData(Scrollable tableTree, int columnIndex) {
		TreeColumn column = ((Tree) tableTree).getColumn(columnIndex);
		return (ColumnLayoutData) column.getData(LAYOUT_DATA);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.5
	 */
	@Override
	protected void updateColumnData(Widget column) {
		TreeColumn tColumn = (TreeColumn) column;
		Tree t = tColumn.getParent();

		if( ! IS_GTK || t.getColumn(t.getColumnCount()-1) != tColumn ){
			tColumn.setData(LAYOUT_DATA,new ColumnPixelData(tColumn.getWidth()));
			layout(t.getParent(), true);
		}
	}
}
