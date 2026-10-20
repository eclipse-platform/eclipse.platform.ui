/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.search.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.search.internal.ui.SelectAllAction;

/**
 * Tests for {@link SelectAllAction}.
 */
public class SelectAllActionTest {

	private Shell fShell;

	@BeforeEach
	public void setUp() {
		fShell= new Shell(Display.getDefault());
	}

	@AfterEach
	public void tearDown() {
		if (fShell != null && !fShell.isDisposed()) {
			fShell.dispose();
		}
	}

	@Test
	public void testSelectAllWithTableViewer() {
		final SelectionChangedEvent[] event= new SelectionChangedEvent[1];
		TableViewer tableViewer= new TableViewer(fShell, SWT.MULTI);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.addSelectionChangedListener((e) -> {
			event[0]= e;
		});
		Object[] elements= { "item1", "item2", "item3" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		tableViewer.setInput(elements);

		SelectAllAction action= new SelectAllAction();
		action.setViewer(tableViewer);
		action.run();

		IStructuredSelection selection= tableViewer.getStructuredSelection();
		assertEquals(3, selection.size(), "All items should be selected"); //$NON-NLS-1$
		assertNotNull(event[0], "SelectionChangedEvent should have been fired"); //$NON-NLS-1$
		assertEquals(3, ((IStructuredSelection) event[0].getSelection()).size(), "Selection in event should include all items"); //$NON-NLS-1$
	}

	@Test
	public void testSelectAllWithTreeViewerFlatList() {
		final SelectionChangedEvent[] event= new SelectionChangedEvent[1];
		TreeViewer treeViewer= new TreeViewer(fShell, SWT.MULTI);
		treeViewer.setContentProvider(new TreeNodeContentProvider());
		treeViewer.addSelectionChangedListener((e) -> {
			event[0]= e;
		});

		final TreeNode[] nodes= new TreeNode[3];
		nodes[0]= new TreeNode("node1"); //$NON-NLS-1$
		nodes[1]= new TreeNode("node2"); //$NON-NLS-1$
		nodes[2]= new TreeNode("node3"); //$NON-NLS-1$
		treeViewer.setInput(nodes);

		SelectAllAction action= new SelectAllAction();
		action.setViewer(treeViewer);
		action.run();

		IStructuredSelection selection= treeViewer.getStructuredSelection();
		assertEquals(3, selection.size(), "All items should be selected"); //$NON-NLS-1$
		assertNotNull(event[0], "SelectionChangedEvent should have been fired"); //$NON-NLS-1$
		assertEquals(3, ((IStructuredSelection) event[0].getSelection()).size(), "Selection in event should include all items"); //$NON-NLS-1$
	}

	@Test
	public void testSelectAllWithNoViewer() {
		// Should not throw any exception when no viewer is set
		SelectAllAction action= new SelectAllAction();
		assertDoesNotThrow(() -> action.run(), "Running SelectAllAction without a viewer should not throw an exception"); //$NON-NLS-1$
	}

	@Test
	public void testSelectAllWithEmptyTableViewer() {
		final SelectionChangedEvent[] event= new SelectionChangedEvent[1];
		TableViewer tableViewer= new TableViewer(fShell, SWT.MULTI);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(new Object[0]);
		tableViewer.addSelectionChangedListener((e) -> {
			event[0]= e;
		});

		SelectAllAction action= new SelectAllAction();
		action.setViewer(tableViewer);
		action.run();

		IStructuredSelection selection= tableViewer.getStructuredSelection();
		assertTrue(selection.isEmpty(), "Selection should be empty for an empty table"); //$NON-NLS-1$
		assertNotNull(event[0], "SelectionChangedEvent should have been fired"); //$NON-NLS-1$
		assertTrue(event[0].getSelection().isEmpty(), "Selection in event should be empty for an empty table"); //$NON-NLS-1$
	}

	@Test
	public void testSelectAllWithEmptyTreeViewer() {
		final SelectionChangedEvent[] event= new SelectionChangedEvent[1];
		TreeViewer treeViewer= new TreeViewer(fShell, SWT.MULTI);
		treeViewer.setContentProvider(new TreeNodeContentProvider());
		treeViewer.setInput(new TreeNode[0]);
		treeViewer.addSelectionChangedListener((e) -> {
			event[0]= e;
		});

		SelectAllAction action= new SelectAllAction();
		action.setViewer(treeViewer);
		action.run();

		IStructuredSelection selection= treeViewer.getStructuredSelection();
		assertTrue(selection.isEmpty(), "Selection should be empty for an empty tree"); //$NON-NLS-1$
		assertNotNull(event[0], "SelectionChangedEvent should have been fired"); //$NON-NLS-1$
		assertTrue(event[0].getSelection().isEmpty(), "Selection in event should be empty for an empty tree"); //$NON-NLS-1$
	}

	@Test
	public void testSelectAllWithTreeViewerOnlySelectsExpandedItems() {
		final SelectionChangedEvent[] event= new SelectionChangedEvent[1];
		TreeViewer treeViewer= new TreeViewer(fShell, SWT.MULTI);
		treeViewer.setContentProvider(new TreeNodeContentProvider());
		treeViewer.addSelectionChangedListener((e) -> {
			event[0]= e;
		});

		final TreeNode[] nodes= new TreeNode[2];
		TreeNode parent= new TreeNode("parent"); //$NON-NLS-1$
		parent.setChildren(new TreeNode[] { new TreeNode("child1"), new TreeNode("child2") }); //$NON-NLS-1$ //$NON-NLS-2$
		nodes[0]= parent;
		nodes[1]= new TreeNode("sibling"); //$NON-NLS-1$
		treeViewer.setInput(nodes);

		// collapse all (default state) - children not expanded
		SelectAllAction action= new SelectAllAction();
		action.setViewer(treeViewer);
		action.run();

		IStructuredSelection collapsedSelection= treeViewer.getStructuredSelection();
		assertEquals(2, collapsedSelection.size(),
				"Only top-level items should be selected when tree is collapsed"); //$NON-NLS-1$
		assertNotNull(event[0], "SelectionChangedEvent should have been fired"); //$NON-NLS-1$
		assertEquals(2, ((IStructuredSelection) event[0].getSelection()).size(), "Selection in event should include only top-level items when tree is collapsed"); //$NON-NLS-1$

		// expand "parent" and re-run select all
		treeViewer.expandToLevel(parent, 1);
		action.run();

		IStructuredSelection expandedSelection= treeViewer.getStructuredSelection();
		assertEquals(4, expandedSelection.size(),
				"Top-level items plus expanded children should be selected"); //$NON-NLS-1$
		assertEquals(4, ((IStructuredSelection) event[0].getSelection()).size(), "Selection in event should include top-level items plus expanded children when tree is expanded"); //$NON-NLS-1$

	}

}
