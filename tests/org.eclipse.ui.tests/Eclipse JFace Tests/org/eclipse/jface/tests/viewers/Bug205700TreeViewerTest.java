/*******************************************************************************
 * Copyright (c) 2007 Lasse Knudsen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lasse Knudsen - initial API and implementation, bug 205700
 *     Boris Bokowski, IBM - additional test cases
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class Bug205700TreeViewerTest extends TestCase {

	private Shell shell;

	private TreeViewer viewer;

	private TreeNode rootNode;

	private final TreeNode child1 = new TreeNode("Child1");

	private final TreeNode child5 = new TreeNode("Child5");

	private final TreeNode child10 = new TreeNode("Child10");

	@Override
	protected void setUp() throws Exception {
		shell = new Shell();

		viewer = new TreeViewer(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		viewer.setContentProvider(new InternalContentProvider());
		viewer.setLabelProvider(new InternalLabelProvider());

		viewer.setInput(createInput());

		shell.open();
	}

	@Override
	protected void tearDown() throws Exception {
		shell.close();
	}

	public void testAddWithoutSorter() throws Exception {
		assertItemNames(new String[] { "Child1", "Child5", "Child10" });

		rootNode.add(new TreeNode("Child2"));
		rootNode.add(new TreeNode("Child3"));
		rootNode.add(new TreeNode("Child4"));
		rootNode.add(new TreeNode("Child6"));
		rootNode.add(new TreeNode("Child7"));
		rootNode.add(new TreeNode("Child8"));
		rootNode.add(new TreeNode("Child9"));

		viewer.add(rootNode, rootNode.getChildren().toArray());

		assertItemNames(new String[] { "Child1", "Child5", "Child10", "Child2",
				"Child3", "Child4", "Child6", "Child7", "Child8", "Child9" });
	}

	/**
	 * @param names
	 */
	private void assertItemNames(String[] names) {
		for (int i = 0; i < names.length; i++) {
			assertItemName(i, names[i]);
		}
	}

	/**
	 * @param index
	 * @param name
	 */
	private void assertItemName(int index, String name) {
		assertEquals("at " + index, name, viewer.getTree().getItem(index)
				.getText());
	}

	public void testAddWithSorter() throws Exception {
		assertItemNames(new String[] { "Child1", "Child5", "Child10" });
		viewer.setSorter(new ViewerSorter());
		assertItemNames(new String[] { "Child1", "Child10", "Child5" });

		rootNode.add(new TreeNode("Child2"));
		rootNode.add(new TreeNode("Child3"));
		rootNode.add(new TreeNode("Child4"));
		rootNode.add(new TreeNode("Child6"));
		rootNode.add(new TreeNode("Child7"));
		rootNode.add(new TreeNode("Child8"));
		rootNode.add(new TreeNode("Child9"));

		viewer.add(rootNode, rootNode.getChildren().toArray());

		assertItemNames(new String[] { "Child1", "Child10", "Child2", "Child3",
				"Child4", "Child5", "Child6", "Child7", "Child8", "Child9" });
	}

	public void testAddEquallySortedElements() throws Exception {
		assertItemNames(new String[] { "Child1", "Child5", "Child10" });
		viewer.setSorter(new ViewerSorter());
		assertItemNames(new String[] { "Child1", "Child10", "Child5" });

		// add before the existing "Child1" node
		rootNode.getChildren().add(0, new TreeNode("Child1"));

		viewer.add(rootNode, rootNode.getChildren().toArray());

		assertItemNames(new String[] { "Child1", "Child1", "Child10",
				"Child5" });
	}

	private Object createInput() {
		rootNode = new TreeNode("Root");

		rootNode.add(child1);
		rootNode.add(child5);
		rootNode.add(child10);

		return rootNode;
	}

	private class TreeNode {

		private final String name;

		private TreeNode parent = null;

		private final List children = new ArrayList();

		public TreeNode(String name) {
			this.name = name;
		}

		public void add(TreeNode newChild) {
			if (newChild != null) {
				children.add(newChild);
			}
		}

		public List getChildren() {
			return children;
		}

		public TreeNode getParent() {
			return parent;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return getName();
		}
	}

	private class InternalLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof TreeNode) {
				return ((TreeNode) element).getName();
			}
			return null;
		}
	}

	private class InternalContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof TreeNode) {
				return ((TreeNode) parentElement).getChildren().toArray();
			}
			return new Object[0];
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof TreeNode) {
				return ((TreeNode) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof TreeNode) {
				return !((TreeNode) element).getChildren().isEmpty();
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
			// nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing
		}
	}

}
