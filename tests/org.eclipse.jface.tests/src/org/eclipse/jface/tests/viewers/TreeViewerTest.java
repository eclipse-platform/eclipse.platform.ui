/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Test;

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

		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot), "The expanded widget child is not expanded");
		assertTrue(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild()), "The first child of the trivial path was not auto-expanded");
		assertFalse(fTreeViewer.getExpandedState(trivialPathRoot.getFirstChild().getFirstChild()), "Trivial path is expanded further than specified depth ");
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
			assertTrue(recursiveExpandedElements.contains(currentElement), "expansion for child was not processed: " + currentElement);
			elements.addAll(Arrays.asList(currentElement.getChildren()));
		}
	}

	@Test
	public void testExpandCollapseToLevel() {
		TestElement rootElement = TestElement.createModel(3, 1);
		TestElement firstChild = rootElement.getChildAt(0);
		TestElement secondChild = firstChild.getChildAt(0);
		TestElement lastChild = secondChild.getChildAt(0);

		fTreeViewer.setInput(rootElement);

		/*
		 * First block shows everything works with
		 * collapseToLevel(AbstractTreeViewer.ALL_LEVELS);
		 */
		fTreeViewer.expandToLevel(rootElement, AbstractTreeViewer.ALL_LEVELS);
		processEvents();
		assertTrue(fTreeViewer.getExpandedState(firstChild), "1st should be expanded");
		assertTrue(fTreeViewer.getExpandedState(secondChild), "2nd should be expanded");
		assertFalse(fTreeViewer.getExpandedState(lastChild), "3rd should be always collapsed");

		fTreeViewer.collapseToLevel(firstChild, AbstractTreeViewer.ALL_LEVELS);
		processEvents();
		assertFalse(fTreeViewer.getExpandedState(firstChild), "1st should be collapsed");
		assertFalse(fTreeViewer.getExpandedState(secondChild), "2nd should be collapsed");
		assertFalse(fTreeViewer.getExpandedState(lastChild), "3rd should be always collapsed");

		/*
		 * Main block shows regression with collapseToLevel(number);
		 */
		fTreeViewer.expandToLevel(rootElement, AbstractTreeViewer.ALL_LEVELS);
		processEvents();
		assertTrue(fTreeViewer.getExpandedState(firstChild), "1st should be expanded");
		assertTrue(fTreeViewer.getExpandedState(secondChild), "2nd should be expanded");
		assertFalse(fTreeViewer.getExpandedState(lastChild), "3rd should be always collapsed");

		fTreeViewer.collapseToLevel(firstChild, 2);
		processEvents();
		assertFalse(fTreeViewer.getExpandedState(firstChild), "1st should be collapsed");
		assertFalse(fTreeViewer.getExpandedState(secondChild), "2nd should be collapsed");
		assertFalse(fTreeViewer.getExpandedState(lastChild), "3rd should be always collapsed");
	}

	/**
	 * Removing the same element twice should not produce a dummy tree-item.
	 */
	@Test
	public void testIssue3525() {
		TestElement modelRoot = TestElement.createModel(2, 1);
		TestElement modelParent = modelRoot.getChildAt(0);
		TestElement modelChild = modelParent.getChildAt(0);
		fTreeViewer.setInput(modelRoot);
		fTreeViewer.expandAll();
		processEvents();
		TreeItem widgetParent = (TreeItem) fTreeViewer.testFindItem(modelParent);
		TreeItem widgetChild = (TreeItem) fTreeViewer.testFindItem(modelChild);
		assertNotNull(widgetParent);
		assertNotNull(widgetChild);
		assertArrayEquals(widgetParent.getItems(), new TreeItem[] { widgetChild });

		// This workaround is needed because of TreeViewerWithLimitCompatibilityTest
		// When calling setDisplayIncrementally(...) with a positive number, you are
		// no longer able to remove elements from the viewer without first removing
		// them from the model
		modelParent.fChildren.remove(modelChild);
		fTreeViewer.remove(modelChild);
		modelParent.fChildren.add(modelChild);
		processEvents();
		widgetParent = (TreeItem) fTreeViewer.testFindItem(modelParent);
		widgetChild = (TreeItem) fTreeViewer.testFindItem(modelChild);
		assertNotNull(widgetParent);
		assertNull(widgetChild);
		assertArrayEquals(widgetParent.getItems(), new TreeItem[0]);

		fTreeViewer.remove(modelChild);
		processEvents();
		widgetParent = (TreeItem) fTreeViewer.testFindItem(modelParent);
		widgetChild = (TreeItem) fTreeViewer.testFindItem(modelChild);
		assertNotNull(widgetParent);
		assertNull(widgetChild);
		assertArrayEquals(widgetParent.getItems(), new TreeItem[0]);
	}
}
