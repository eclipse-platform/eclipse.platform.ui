/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests saving and restoring the expansion state of a tree by tree path, with
 * and without an {@link IElementComparer}.
 */
public class TreeViewerExpansionStateTest {

	private Shell shell;

	private TreeViewer treeViewer;

	private Node root;

	private static class Node {
		final String name;
		final Node parent;
		final List<Node> children = new ArrayList<>();

		Node(String name, Node parent) {
			this.name = name;
			this.parent = parent;
			if (parent != null) {
				parent.children.add(this);
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Keys the nodes by name, so that paths are compared through the comparer
	 * rather than by identity.
	 */
	private static final IElementComparer NAME_COMPARER = new IElementComparer() {

		@Override
		public boolean equals(Object a, Object b) {
			if (a instanceof Node first && b instanceof Node second) {
				return first.name.equals(second.name);
			}
			return Objects.equals(a, b);
		}

		@Override
		public int hashCode(Object element) {
			return element instanceof Node node ? node.name.hashCode() : element.hashCode();
		}
	};

	@AfterEach
	public void tearDown() {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
		treeViewer = null;
		root = null;
	}

	private void createViewer(IElementComparer comparer, int... branching) {
		shell = new Shell();
		shell.setSize(500, 500);
		treeViewer = new TreeViewer(shell);
		if (comparer != null) {
			treeViewer.setComparer(comparer);
		}
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return ((Node) parentElement).children.toArray();
			}

			@Override
			public Object getParent(Object element) {
				return ((Node) element).parent;
			}

			@Override
			public boolean hasChildren(Object element) {
				return !((Node) element).children.isEmpty();
			}
		});
		treeViewer.setLabelProvider(new LabelProvider());

		root = new Node("root", null);
		createChildren(root, 0, branching);
		treeViewer.setInput(root);
		shell.open();
	}

	private static void createChildren(Node parent, int level, int[] branching) {
		if (level >= branching.length) {
			return;
		}
		for (int i = 0; i < branching[level]; i++) {
			createChildren(new Node(parent.name + "." + i, parent), level + 1, branching);
		}
	}

	@Test
	public void testRestoreExpansionWithoutComparer() {
		assertExpansionSurvivesRoundTrip(null);
	}

	/**
	 * The comparer decides how a tree path hashes and compares, so restoring has
	 * to route through it.
	 */
	@Test
	public void testRestoreExpansionWithComparer() {
		assertExpansionSurvivesRoundTrip(NAME_COMPARER);
	}

	private void assertExpansionSurvivesRoundTrip(IElementComparer comparer) {
		createViewer(comparer, 3, 3, 3);
		treeViewer.expandAll();

		TreePath[] expanded = treeViewer.getExpandedTreePaths();
		assertTrue(expanded.length > 0, "Nothing was expanded");

		treeViewer.collapseAll();
		assertEquals(0, treeViewer.getExpandedTreePaths().length, "collapseAll left paths expanded");

		treeViewer.setExpandedTreePaths(expanded);
		assertArrayEquals(expanded, treeViewer.getExpandedTreePaths(), "expansion was not restored");
	}

	/**
	 * Restoring a subset has to expand exactly that subset.
	 */
	@Test
	public void testRestoreSubsetOfExpandedPaths() {
		createViewer(NAME_COMPARER, 3, 3);
		treeViewer.expandAll();

		TreePath[] expanded = treeViewer.getExpandedTreePaths();
		assertEquals(3, expanded.length);

		TreePath[] subset = { expanded[0], expanded[2] };
		treeViewer.setExpandedTreePaths(subset);
		assertArrayEquals(subset, treeViewer.getExpandedTreePaths(), "wrong subset expanded");
	}

	/**
	 * Items the walk reaches after the last path has been found must end up
	 * collapsed, with their children intact for a later restore.
	 */
	@Test
	public void testCollapsesItemsAfterExpandedSetIsExhausted() {
		createViewer(NAME_COMPARER, 4, 3);
		treeViewer.expandAll();

		TreePath[] expanded = treeViewer.getExpandedTreePaths();
		assertEquals(4, expanded.length);

		// The first container stays expanded, so the walk runs out of paths to look
		// for while three expanded containers are still ahead of it.
		TreePath[] first = { expanded[0] };
		treeViewer.setExpandedTreePaths(first);

		assertArrayEquals(first, treeViewer.getExpandedTreePaths(), "only the first container should be expanded");

		TreeItem[] containers = treeViewer.getTree().getItems();
		assertEquals(4, containers.length);
		assertTrue(containers[0].getExpanded(), "the restored container should be expanded");
		for (int i = 1; i < containers.length; i++) {
			assertFalse(containers[i].getExpanded(), "container " + i + " should have been collapsed");
			assertEquals(3, containers[i].getItemCount(), "the children of a collapsed container were lost");
		}

		// Everything can be expanded again from the state left behind.
		treeViewer.setExpandedTreePaths(expanded);
		assertArrayEquals(expanded, treeViewer.getExpandedTreePaths(), "expansion could not be restored again");
	}

	/**
	 * An empty set collapses the tree without disturbing the items.
	 */
	@Test
	public void testRestoreEmptyExpansion() {
		createViewer(NAME_COMPARER, 3, 3);
		treeViewer.expandAll();
		assertEquals(3, treeViewer.getExpandedTreePaths().length);

		treeViewer.setExpandedTreePaths(new TreePath[0]);
		assertEquals(0, treeViewer.getExpandedTreePaths().length, "the tree should be fully collapsed");
		assertEquals(3, treeViewer.getTree().getItemCount());
	}

	/**
	 * A path built from equal but distinct elements has to be recognized, the case
	 * of a refresh that replaced the model objects.
	 */
	@Test
	public void testRestoreWithEqualButDistinctElements() {
		createViewer(NAME_COMPARER, 3, 3);
		treeViewer.expandAll();

		TreePath[] expanded = treeViewer.getExpandedTreePaths();
		assertEquals(3, expanded.length);
		treeViewer.collapseAll();

		TreePath[] rebuilt = new TreePath[expanded.length];
		for (int i = 0; i < expanded.length; i++) {
			Object[] segments = new Object[expanded[i].getSegmentCount()];
			for (int j = 0; j < segments.length; j++) {
				// A fresh object that the comparer considers equal to the original.
				segments[j] = new Node(((Node) expanded[i].getSegment(j)).name, null);
			}
			rebuilt[i] = new TreePath(segments);
		}

		treeViewer.setExpandedTreePaths(rebuilt);
		// The viewer hands back its own elements, so the original paths are the
		// expected ones, which also pins down which containers were expanded.
		assertArrayEquals(expanded, treeViewer.getExpandedTreePaths(),
				"equal but distinct elements were not recognized");
	}

}
