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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class VirtualLazyTreeViewerTest extends TreeViewerTest {

	@Rule
	public TestName testName = new TestName();

	protected int setDataCalls = 0;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.VIRTUAL);
		tree.addListener(SWT.SetData, event -> setDataCalls++);
		fTreeViewer = new TreeViewer(tree);
		fTreeViewer.setContentProvider(new TestModelLazyTreeContentProvider((TreeViewer) fTreeViewer));
		return fTreeViewer;
	}

	@Override
	public void setUp() {
		super.setUp();
		// process events because the content provider uses an asyncExec to set the item count of the tree
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

	@Ignore("Test leads to infinite loop. Duplicate children are a bad idea in virtual trees.")
	@Override
	public void testRefreshWithDuplicateChild() {
	}

	@Ignore("Test leads to infinite loop. Cycles are a bad idea in virtual trees.")
	@Override
	public void testSetExpandedWithCycle() {
	}

	@Ignore("no need to test since virtual trees do not support filtering")
	@Override
	public void testFilterExpanded() {
	}

	@Ignore("no need to test since virtual trees do not support filtering")
	@Override
	public void testFilter() {
	}

	@Ignore("no need to test since virtual trees do not support filtering")
	@Override
	public void testSetFilters() {
	}

	@Ignore("no need to test since virtual trees do not support filtering")
	@Override
	public void testInsertSiblingWithFilterFiltered() {
	}

	@Ignore("no need to test since virtual trees do not support filtering")
	@Override
	public void testInsertSiblingWithFilterNotFiltered() {
	}

	@Ignore("no need to test since virtual trees do not support sorting")
	@Override
	public void testInsertSiblingWithSorter() {
		// no need to test since virtual trees do not support sorting
	}

	@Ignore("no need to test since virtual trees do not support filtering")
	@Override
	public void testRenameWithFilter() {
	}

	@Ignore("no need to test since virtual trees do not support sorting")
	@Override
	public void testRenameWithSorter() {
	}

	@Ignore("no need to test since virtual trees do not support sorting")
	@Override
	public void testSorter() {
	}

	@Ignore("test is not relevant for lazy tree viewer")
	@Override
	public void testChildIsNotDuplicatedWhenCompareEquals() {
	}

	// Temporary overrides for bug 347491:
	@Test
	@Override
	public void testRefreshWithAddedChildren() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testRefreshWithAddedChildren();
	}

	@Test
	@Override
	public void testDeleteSibling() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testDeleteSibling();
	}

	@Test
	@Override
	public void testExpandToLevel() {
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testExpandToLevel();
	}

	@Test
	@Override
	public void testInsertSibling() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testInsertSibling();
	}

	@Test
	@Override
	public void testInsertSiblings() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testInsertSiblings();
	}

	@Test
	@Override
	public void testSetInput() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testSetInput();
	}

	@Test
	@Override
	public void testSomeChildrenChanged() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testSomeChildrenChanged();
	}

	@Test
	@Override
	public void testWorldChanged() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + testName.getMethodName());
			return;
		}
		super.testWorldChanged();
	}
}
