/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * Tests TreeViewer's VIRTUAL support. Do not add to the suites yet!
 * @since 3.2
 */
public class VirtualLazyTreeViewerTest extends ViewerTestCase {
	
	private class LazyTreeContentProvider implements ILazyTreeContentProvider {

		public void updateElement(Object parent, int index) {
			updateElementCallCount++;
			String parentString = (String) parent;
			Object childElement = parentString + "-" + index;
			getTreeViewer().replace(parent, index, childElement);
			getTreeViewer().setChildCount(childElement, 10);
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
		
	}

	private int updateElementCallCount = 0;
	
	public VirtualLazyTreeViewerTest(String name) {
		super(name);
	}

	public TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}
	
	protected void setInput() {
		String letterR = "R";
		getTreeViewer().setInput(letterR);
		getTreeViewer().setChildCount(letterR, 10);
	}

	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(fShell, SWT.VIRTUAL);
		TreeViewer treeViewer = new TreeViewer(tree);
		treeViewer.setContentProvider(new LazyTreeContentProvider());
		return treeViewer;
	}

	public void testCreation() {
		processEvents();
		assertTrue("tree should have items", getTreeViewer().getTree().getItemCount() > 0);
		assertTrue("call to updateElement expected", updateElementCallCount > 0);
		assertEquals("R-0", getTreeViewer().getTree().getItem(0).getText());
	}

	public void testExpand() {
		Tree tree = getTreeViewer().getTree();
		getTreeViewer().expandToLevel("R-0", 1);
		assertEquals(10, tree.getItem(0).getItemCount());
	}
}
