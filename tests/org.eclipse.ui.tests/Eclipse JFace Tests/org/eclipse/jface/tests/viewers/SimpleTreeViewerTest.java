/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * @since 3.2
 * 
 */
public class SimpleTreeViewerTest extends ViewerTestCase {

	private TreeViewer treeViewer;

	/**
	 * @param name
	 */
	public SimpleTreeViewerTest(String name) {
		super(name);
	}

	protected StructuredViewer createViewer(Composite parent) {
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new TestModelContentProvider());
		return treeViewer;
	}

	public void testSetTreePathViewerSorterOnNullInput() {
		treeViewer.setInput(null);
		treeViewer.setSorter(new TreePathViewerSorter());
	}
	
	public void testNullLabel() {
		treeViewer.setLabelProvider(new ITableLabelProvider(){

			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				return null;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}});
	}

	static class MyViewerSorter extends ViewerSorter {
		boolean inverted = false;

		public int compare(Viewer viewer, Object e1, Object e2) {
			if (inverted) {
				return super.compare(viewer, e2, e1);
			}
			return super.compare(viewer, e1, e2);
		}
	}

	public void testBug184441() {
		MyViewerSorter sorter = new MyViewerSorter();
		treeViewer.setSorter(sorter);
		ITreeContentProvider contentProvider = (ITreeContentProvider) treeViewer
				.getContentProvider();
		Object firstRoot = contentProvider.getElements(treeViewer.getInput())[0];
		Object childOfFirstRoot = contentProvider.getChildren(firstRoot)[0];
		treeViewer.setSelection(new StructuredSelection(childOfFirstRoot), true);
		final ISelectionChangedListener listener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fail();
			}
		};
		treeViewer.addSelectionChangedListener(listener);
		sorter.inverted = true;
		treeViewer.refresh();
		treeViewer.removeSelectionChangedListener(listener);
	}

	public void testBug184712() {
		class TableAndTreeLabelProvider extends LabelProvider implements ITableLabelProvider {
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				return "wrong";
			}

			public String getText(Object element) {
				return "right";
			}
		}
		treeViewer.setLabelProvider(new TableAndTreeLabelProvider());
		assertEquals("right", treeViewer.getTree().getItem(0).getText());
	}

	public void test327004() {
		treeViewer.setInput(null);
		treeViewer.setContentProvider(new TreeNodeContentProvider());

		final TreeNode[] children= new TreeNode[5];
		children[0]= new TreeNode("0");
		children[1]= new TreeNode("1");
		children[2]= new TreeNode("1");
		children[3]= new TreeNode("1");
		children[4]= new TreeNode("1");
		treeViewer.setInput(children);

		ViewerFilter filter= new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element == children[0] || element == children[1] || element == children[2] || element == children[4])
					return false;
				return true;
			}
		};
		treeViewer.setFilters(new ViewerFilter[] { filter });
		int i= treeViewer.getTree().getItemCount();

		assertEquals(4, i); // 4 because the filter doesn't work due to equal nodes
	}

}
