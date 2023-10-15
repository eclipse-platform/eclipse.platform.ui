/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 481490
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class SimpleTreeViewerTest extends ViewerTestCase {

	private TreeViewer treeViewer;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new TestModelContentProvider());
		return treeViewer;
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testSetTreePathViewerSorterOnNullInput() {
		treeViewer.setInput(null);
		treeViewer.setSorter(new TreePathViewerSorter());
	}

	@Test
	public void testNullLabel() {
		treeViewer.setLabelProvider(new ITableLabelProvider() {

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				return null;
			}

			@Override
			public void addListener(ILabelProviderListener listener) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
			}
		});
	}

	static class MyViewerSorter extends ViewerComparator {
		boolean inverted = false;

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (inverted) {
				return super.compare(viewer, e2, e1);
			}
			return super.compare(viewer, e1, e2);
		}
	}

	@Test
	public void testBug184441() {
		MyViewerSorter sorter = new MyViewerSorter();
		treeViewer.setComparator(sorter);
		ITreeContentProvider contentProvider = (ITreeContentProvider) treeViewer.getContentProvider();
		Object firstRoot = contentProvider.getElements(treeViewer.getInput())[0];
		Object childOfFirstRoot = contentProvider.getChildren(firstRoot)[0];
		treeViewer.setSelection(new StructuredSelection(childOfFirstRoot), true);
		final ISelectionChangedListener listener = event -> fail();
		treeViewer.addSelectionChangedListener(listener);
		sorter.inverted = true;
		treeViewer.refresh();
		treeViewer.removeSelectionChangedListener(listener);
	}

	@Test
	public void testBug184712() {
		class TableAndTreeLabelProvider extends LabelProvider implements ITableLabelProvider {
			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				return "wrong";
			}

			@Override
			public String getText(Object element) {
				return "right";
			}
		}
		treeViewer.setLabelProvider(new TableAndTreeLabelProvider());
		assertEquals("right", treeViewer.getTree().getItem(0).getText());
	}

	@Test
	public void test327004() {
		treeViewer.setInput(null);
		treeViewer.setContentProvider(new TreeNodeContentProvider());

		final TreeNode[] children = new TreeNode[5];
		children[0] = new TreeNode("0");
		children[1] = new TreeNode("1");
		children[2] = new TreeNode("1");
		children[3] = new TreeNode("1");
		children[4] = new TreeNode("1");
		treeViewer.setInput(children);

		ViewerFilter filter = new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element == children[0] || element == children[1] || element == children[2]
						|| element == children[4]) {
					return false;
				}
				return true;
			}
		};
		treeViewer.setFilters(filter);
		int i = treeViewer.getTree().getItemCount();

		assertEquals(4, i); // 4 because the filter doesn't work due to equal nodes
	}

}
