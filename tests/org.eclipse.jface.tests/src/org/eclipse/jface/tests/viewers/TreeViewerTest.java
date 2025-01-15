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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
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

	@Test
	public void testInternalExpandToLevelRecursive() {
		// internalExpandToLevel is recursive by contract, so we track all processed
		// elements in a subtype to validate contractual recursive execution
		List<Object> recursiveExpandedElements = new ArrayList<>();
		TreeViewer viewer = new TreeViewer(fShell) {
			@Override
			protected void internalExpandToLevel(Widget widget, int level) {
				if (widget != this.getTree()) {
					recursiveExpandedElements.add(widget.getData());
				}
				super.internalExpandToLevel(widget, level);
			}
		};
		TestElement rootElement = TestElement.createModel(2, 5);
		viewer.setContentProvider(new TestModelContentProvider());
		viewer.setInput(rootElement);

		viewer.expandToLevel(AbstractTreeViewer.ALL_LEVELS);

		Queue<TestElement> elements = new ConcurrentLinkedQueue<>(Arrays.asList(rootElement.getChildren()));
		while (!elements.isEmpty()) {
			TestElement currentElement = elements.poll();
			assertTrue("expansion for child was not processed: " + currentElement,
					recursiveExpandedElements.contains(currentElement));
			elements.addAll(Arrays.asList(currentElement.getChildren()));
		}
	}

}
