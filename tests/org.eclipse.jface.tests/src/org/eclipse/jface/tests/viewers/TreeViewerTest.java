/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Test;

public class TreeViewerTest extends AbstractTreeViewerTest {

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		fTreeViewer = new TreeViewer(parent);
		fTreeViewer.setContentProvider(new TestModelContentProvider());
		return fTreeViewer;
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		TreeItem ti = (TreeItem) fViewer.testFindItem(first);
		Tree tree = ti.getParent();
		return tree.getItemCount();
	}

	/**
	 * getItemCount method comment.
	 */
	@Override
	protected int getItemCount(TestElement element) {
		return element.getChildCount();
	}

	@Override
	protected String getItemText(int at) {
		Tree tree = (Tree) fTreeViewer.getControl();
		return tree.getItems()[at].getText();
	}

	@Test
	public void testAutoExpandOnSingleChildThroughEvent() {
		TestElement modelRoot = TestElement.createModel(5, 1);
		TestElement trivialPathRoot = modelRoot.getFirstChild();
		fViewer.setInput(modelRoot);

		fTreeViewer.setAutoExpandOnSingleChildLevels(2);
		Tree tree = (Tree) fTreeViewer.getControl();
		TreeItem itemToExpand = ((Tree) fViewer.getControl()).getItem(0);

		Event event = new Event();
		event.item = itemToExpand;
		tree.notifyListeners(SWT.Expand, event);

		assertTrue("The expanded widget child is not expanded", fTreeViewer.getExpandedState(trivialPathRoot));
		assertTrue("The first child of the trivial path was not auto-expanded",
				fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()));
		assertFalse("Trivial path is expanded further than specified depth ",
				fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild().getFirstChild()));
	}

}
