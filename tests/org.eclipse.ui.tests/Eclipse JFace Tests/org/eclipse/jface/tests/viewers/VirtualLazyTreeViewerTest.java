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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

public class VirtualLazyTreeViewerTest extends TreeViewerTest {

	protected int setDataCalls = 0;

	public VirtualLazyTreeViewerTest(String name) {
		super(name);
	}

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

	@Override
	public void tearDown() {
		super.tearDown();
//    	System.out.println("calls: " + setDataCalls);
	}

	public void testLeafIsExpandable() {
		TestElement leafElement = fRootElement.getChildAt(2).getChildAt(3).getChildAt(2);
		assertEquals(0, leafElement.getChildCount());
		assertFalse(fTreeViewer.isExpandable(leafElement));
	}

	public void testRootIsExpandable() {
		TestElement rootElement = fRootElement.getChildAt(2);
		assertTrue(rootElement.getChildCount() > 0);
		assertTrue(fTreeViewer.isExpandable(rootElement));
	}

	public void testNodeIsExpandable() {
		TestElement nodeElement = fRootElement.getChildAt(2).getChildAt(3);
		assertTrue(nodeElement.getChildCount() > 0);
		assertTrue(fTreeViewer.isExpandable(nodeElement));
	}


	@Override
	public void testRefreshWithDuplicateChild() {
		// Test leads to infinite loop. Duplicate children are a bad idea in virtual trees.
	}

	@Override
	public void testSetExpandedWithCycle() {
		// Test leads to infinite loop. Cycles are a bad idea in virtual trees.
	}

	@Override
	public void testFilterExpanded() {
		// no need to test since virtual trees do not support filtering
	}

	@Override
	public void testFilter() {
		// no need to test since virtual trees do not support filtering
	}

	@Override
	public void testSetFilters() {
		// no need to test since virtual trees do not support filtering
	}

	@Override
	public void testInsertSiblingWithFilterFiltered() {
		// no need to test since virtual trees do not support filtering
	}

	@Override
	public void testInsertSiblingWithFilterNotFiltered() {
		// no need to test since virtual trees do not support filtering
	}

	@Override
	public void testInsertSiblingWithSorter() {
		// no need to test since virtual trees do not support sorting
	}

	@Override
	public void testRenameWithFilter() {
		// no need to test since virtual trees do not support filtering
	}

	@Override
	public void testRenameWithSorter() {
		// no need to test since virtual trees do not support sorting
	}

	@Override
	public void testSorter() {
		// no need to test since virtual trees do not support sorting
	}

	// Temporary overrides for bug 347491:
	@Override
	public void testRefreshWithAddedChildren() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testRefreshWithAddedChildren();
	}

	@Override
	public void testDeleteSibling() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testDeleteSibling();
	}

	@Override
	public void testExpandToLevel() {
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testExpandToLevel();
	}

	@Override
	public void testInsertSibling() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testInsertSibling();
	}

	@Override
	public void testInsertSiblings() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testInsertSiblings();
	}

	@Override
	public void testSetInput() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testSetInput();
	}

	@Override
	public void testSomeChildrenChanged() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testSomeChildrenChanged();
	}

	@Override
	public void testWorldChanged() {
		if (disableTestsBug347491) {
			return;
		}
		if (setDataCalls == 0) {
			System.err.println("SWT.SetData is not received. Cancelled test " + getName());
			return;
		}
		super.testWorldChanged();
	}
}
