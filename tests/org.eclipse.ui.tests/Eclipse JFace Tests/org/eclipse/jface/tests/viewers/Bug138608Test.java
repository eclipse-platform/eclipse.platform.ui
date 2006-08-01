/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ivailo Abadjiev (bug 151857)
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * Description of the bug:
 * Initially tree is populated by way shown below and is completely expanded.
 * 
 * root
 *     |-a
 *        |-c
 *        |-d
 *     |-b
 *        |-c
 * 
 * Then 'd' model element is added as child of 'b' in model and through
 * add(parent,child) method of TreeViewer to tree.
 * 
 * The problem - It seems that calling add(parent,child) has no desired efect.
 * 'd' model element is not shown as child of 'b'!
 * 
 * @since 3.2
 * 
 */
public class Bug138608Test extends ViewerTestCase {

	private TreeContentProvider contentProvider;

	/**
	 * @param name
	 */
	public Bug138608Test(String name) {
		super(name);
	}

	protected StructuredViewer createViewer(Composite parent) {
		final TreeViewer viewer = new TreeViewer(parent);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		contentProvider = new TreeContentProvider();
		LabelProvider labelProvider = new LabelProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		return viewer;
	}

	protected void setUpModel() {
		// don't do anything here - we are not using the normal fModel and
		// fRootElement
	}

	protected void setInput() {
		getTreeViewer().setInput(contentProvider.root);
		getTreeViewer().expandAll();
	}

	/**
	 * @return
	 */
	private TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}

	public void testBug138608() {
		processEvents();
		// Add 'd' as child of 'b' in data model first

		contentProvider.root.getChildren()[1].setChildren(contentProvider.root
				.getChildren()[0].getChildren());
		// Then add 'd' as child of 'b' in tree itself
		// THE PROBLEM IS HERE - after this call Tree will not show his
		// new child

		getTreeViewer().add(contentProvider.root.getChildren()[1],
				contentProvider.root.getChildren()[1].getChildren()[1]);

		assertEquals("expected two children of node b", 2, getTreeViewer()
				.getTree().getItem(1).getItemCount());

		getTreeViewer().add(contentProvider.root.getChildren()[1],
				contentProvider.root.getChildren()[1].getChildren()[1]);

		assertEquals("expected two children of node b", 2, getTreeViewer()
				.getTree().getItem(1).getItemCount());

	}

	public void tearDown() {
		contentProvider = null;
		super.tearDown();
	}

	private static class TreeContentProvider implements ITreeContentProvider {

		public TreeNode root = new TreeNode("root");

		public TreeContentProvider() {
			TreeNode d = new TreeNode("d");
			TreeNode c = new TreeNode("c");
			TreeNode b = new TreeNode("b");
			TreeNode a = new TreeNode("a");
			// build initial hierarchy
			root.setChildren(new TreeNode[] { a, b });
			a.setChildren(new TreeNode[] { c, d });
			b.setChildren(new TreeNode[] { c });
		}

		public Object[] getChildren(Object parentElement) {
			return ((TreeNode) parentElement).getChildren();
		}

		public Object getParent(Object element) {
			return ((TreeNode) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return ((TreeNode) element).hasChildren();
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

}
