/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *     Tom Schindl - bug 151205
 *     Lucas Bullen (Red Hat Inc.) - Bug 493357
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VirtualLazyTreeViewerTest extends TreeViewerTest {

	protected volatile int setDataCalls = 0;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.VIRTUAL);
		tree.addListener(SWT.SetData, event -> setDataCalls++);
		fTreeViewer = new TreeViewer(tree);
		fTreeViewer.setContentProvider(new TestModelLazyTreeContentProvider((TreeViewer) fTreeViewer));
		return fTreeViewer;
	}

	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		fShell.forceActive();
		// process events because the content provider uses an asyncExec to set the item
		// count of the tree
		processEvents();
	}

	@Override
	protected void setInput() {
		super.setInput();
	}

	@Test
	public void testLeafIsExpandable() {
		TestElement leafElement = fRootElement.getChildAt(2).getChildAt(3).getChildAt(2);
		assertEquals(0, leafElement.getChildCount());
		assertFalse(fTreeViewer.isExpandable(leafElement));
	}

	@Test
	public void testRootIsExpandable() {
		TestElement rootElement = fRootElement.getChildAt(2);
		assertTrue(rootElement.getChildCount() > 0);
		assertTrue(fTreeViewer.isExpandable(rootElement));
	}

	@Test
	public void testNodeIsExpandable() {
		TestElement nodeElement = fRootElement.getChildAt(2).getChildAt(3);
		assertTrue(nodeElement.getChildCount() > 0);
		assertTrue(fTreeViewer.isExpandable(nodeElement));
	}

	@Disabled("Test leads to infinite loop. Duplicate children are a bad idea in virtual trees.")
	@Override
	public void testRefreshWithDuplicateChild() {
	}

	@Disabled("Test leads to infinite loop. Cycles are a bad idea in virtual trees.")
	@Override
	public void testSetExpandedWithCycle() {
	}

	@Disabled("no need to test since virtual trees do not support filtering")
	@Override
	public void testFilterExpanded() {
	}

	@Disabled("no need to test since virtual trees do not support filtering")
	@Override
	public void testFilter() {
	}

	@Disabled("no need to test since virtual trees do not support filtering")
	@Override
	public void testSetFilters() {
	}

	@Disabled("no need to test since virtual trees do not support filtering")
	@Override
	public void testInsertSiblingWithFilterFiltered() {
	}

	@Disabled("no need to test since virtual trees do not support filtering")
	@Override
	public void testInsertSiblingWithFilterNotFiltered() {
	}

	@Disabled("no need to test since virtual trees do not support sorting")
	@Override
	public void testInsertSiblingWithSorter() {
	}

	@Disabled("no need to test since virtual trees do not support filtering")
	@Override
	public void testRenameWithFilter() {
	}

	@Disabled("no need to test since virtual trees do not support sorting")
	@Override
	public void testRenameWithSorter() {
	}

	@Disabled("no need to test since virtual trees do not support sorting")
	@Override
	public void testSorter() {
	}

	@Disabled("test is not relevant for lazy tree viewer")
	@Override
	public void testChildIsNotDuplicatedWhenCompareEquals() {
	}

	@Disabled("test is not relevant for lazy tree viewer")
	@Override
	public void testExpandCollapseToLevel() {
	}

	// Temporary overrides for bug 347491
	@Test
	@Override
	public void testRefreshWithAddedChildren() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testRefreshWithAddedChildren();
	}

	@Test
	@Override
	public void testDeleteSibling() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testDeleteSibling();
	}

	@Test
	@Override
	public void testInsertSibling() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testInsertSibling();
	}

	@Test
	@Override
	public void testInsertSiblings() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testInsertSiblings();
	}

	@Test
	@Override
	public void testSetInput() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testSetInput();
	}

	@Test
	@Override
	public void testSomeChildrenChanged() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testSomeChildrenChanged();
	}

	@Test
	@Override
	public void testWorldChanged() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testWorldChanged();
	}

	@Test
	@Override
	public void testContains() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testContains();
	}

	@Test
	@Override
	public void testAutoExpandOnSingleChildThroughEvent() {
		assumeFalse(disableTestsBug347491, "test disabled because of bug 347491");
		assertTrue(setDataCalls > 0, "data must have been set");
		super.testAutoExpandOnSingleChildThroughEvent();
	}

}
