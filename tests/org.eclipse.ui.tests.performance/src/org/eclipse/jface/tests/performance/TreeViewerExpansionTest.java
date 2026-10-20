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

package org.eclipse.jface.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

/**
 * Measures restoring the expansion state of a tree whose elements have a
 * recursive {@link Object#hashCode()}, the shape used by content models such as
 * the LSP document symbols.
 * <p>
 * A viewer restores expansion by looking every visible item up in a hash table
 * keyed by {@link TreePath}, and a tree path hashes all of its segments. An
 * element that hashes its whole subtree therefore makes each lookup cost the
 * size of the subtrees along its path, so the reported hash invocation count
 * grows much faster than the number of elements.
 */
public class TreeViewerExpansionTest extends ViewerTest {

	/** Children per level, from the top level down. */
	private static final int[] BRANCHING = { 12, 12, 12 };

	/**
	 * A few large subtrees, the shape of a generated document. Restoring one
	 * container leaves most items with nothing left to match against.
	 */
	private static final int[] WIDE_BRANCHING = { 3, 600 };

	private int[] branching = BRANCHING;

	private TreeViewer treeViewer;

	private DeepHashElement root;

	private boolean useComparer;

	/**
	 * Element with a recursive hash, modelled after the generated
	 * {@code hashCode}/{@code equals} of an LSP {@code DocumentSymbol}, which fold
	 * in the child list.
	 */
	static final class DeepHashElement {

		/**
		 * Counts every visit, the recursive ones included, so it measures the total
		 * work and not just the number of top level calls.
		 */
		static final AtomicLong hashVisits = new AtomicLong();

		final String name;

		final DeepHashElement parent;

		final List<DeepHashElement> children = new ArrayList<>();

		DeepHashElement(String name, DeepHashElement parent) {
			this.name = name;
			this.parent = parent;
		}

		@Override
		public int hashCode() {
			hashVisits.incrementAndGet();
			return Objects.hash(name, children);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DeepHashElement other)) {
				return false;
			}
			return name.equals(other.name) && children.equals(other.children);
		}

		int size() {
			int size = 1;
			for (DeepHashElement child : children) {
				size += child.size();
			}
			return size;
		}
	}

	/**
	 * Comparer that replaces the recursive hash by a cheap key. The generated name
	 * is unique per element, which is what the viewer requires of elements that
	 * share a parent.
	 */
	private static final IElementComparer CHEAP_COMPARER = new IElementComparer() {

		@Override
		public boolean equals(Object a, Object b) {
			if (a instanceof DeepHashElement first && b instanceof DeepHashElement second) {
				return first.name.equals(second.name);
			}
			return Objects.equals(a, b);
		}

		@Override
		public int hashCode(Object element) {
			if (element instanceof DeepHashElement symbol) {
				return symbol.name.hashCode();
			}
			return element.hashCode();
		}
	};

	@Override
	protected StructuredViewer createViewer(Shell shell) {
		treeViewer = new TreeViewer(shell);
		if (useComparer) {
			treeViewer.setComparer(CHEAP_COMPARER);
		}
		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return ((DeepHashElement) parentElement).children.toArray();
			}

			@Override
			public Object getParent(Object element) {
				return ((DeepHashElement) element).parent;
			}

			@Override
			public boolean hasChildren(Object element) {
				return !((DeepHashElement) element).children.isEmpty();
			}
		});
		treeViewer.setLabelProvider(getLabelProvider());
		return treeViewer;
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DeepHashElement) element).name;
			}
		};
	}

	@Override
	protected Object getInitialInput() {
		root = new DeepHashElement("root", null);
		createChildren(root, 0);
		return root;
	}

	private void createChildren(DeepHashElement parent, int level) {
		if (level >= branching.length) {
			return;
		}
		for (int i = 0; i < branching[level]; i++) {
			DeepHashElement child = new DeepHashElement(parent.name + "." + i, parent);
			parent.children.add(child);
			createChildren(child, level + 1);
		}
	}

	/**
	 * Restores the expansion state of a fully expanded tree, the operation a
	 * content provider performs after every refresh to keep the tree from
	 * collapsing under the user.
	 */
	@Test
	public void testRestoreExpansion() throws CoreException {
		measureRestoreExpansion();
	}

	/**
	 * The same measurement with an {@link IElementComparer} that keys the elements
	 * by a cheap value, which is how a client avoids the recursive hash.
	 */
	@Test
	public void testRestoreExpansionWithComparer() throws CoreException {
		useComparer = true;
		measureRestoreExpansion();
	}

	/**
	 * Restores a small expanded set on a fully materialized tree, where the viewer
	 * walks every item but runs out of paths to look for early on.
	 */
	@Test
	public void testRestoreFewExpandedPaths() throws CoreException {
		branching = WIDE_BRANCHING;
		openBrowser();
		treeViewer.expandAll();
		processEvents();

		TreePath[] all = treeViewer.getExpandedTreePaths();
		assertTrue(all.length > 1, "No container would be left collapsed");
		TreePath[] few = Arrays.copyOf(all, 1);

		// Reaching the target state through the viewer keeps the items of the
		// collapsed containers alive, unlike collapseAll, which prunes them.
		treeViewer.setExpandedTreePaths(few);
		processEvents();
		treeViewer.setExpandedTreePaths(few);
		processEvents();

		AtomicLong visits = new AtomicLong();
		exercise(() -> {
			DeepHashElement.hashVisits.set(0);
			startMeasuring();
			treeViewer.setExpandedTreePaths(few);
			stopMeasuring();
			visits.set(DeepHashElement.hashVisits.get());

			processEvents();
			assertEquals(few.length, treeViewer.getExpandedTreePaths().length,
					"The expansion state was not restored");
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		int elements = root.size() - 1;
		reportTimings("restore " + few.length + " of " + elements + " elements, all materialized");
		System.out.printf(Locale.ROOT, "%-48s %d recursive hash visits for %d elements (%.1f per element)%n",
				getClass().getSimpleName() + " few expanded", visits.get(), elements,
				visits.get() / (double) elements);
	}

	private void measureRestoreExpansion() throws CoreException {
		openBrowser();
		treeViewer.expandAll();
		processEvents();

		TreePath[] expanded = treeViewer.getExpandedTreePaths();
		assertTrue(expanded.length > 0, "The tree was not expanded");

		// The first restore pays for creating the tree items and for JIT warm up.
		treeViewer.collapseAll();
		processEvents();
		treeViewer.setExpandedTreePaths(expanded);
		processEvents();

		AtomicLong visits = new AtomicLong();
		exercise(() -> {
			treeViewer.collapseAll();
			processEvents();

			DeepHashElement.hashVisits.set(0);
			startMeasuring();
			treeViewer.setExpandedTreePaths(expanded);
			stopMeasuring();
			visits.set(DeepHashElement.hashVisits.get());

			processEvents();
			assertEquals(expanded.length, treeViewer.getExpandedTreePaths().length,
					"The expansion state was not restored");
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		int elements = root.size() - 1;
		reportTimings("restore " + expanded.length + " of " + elements + " elements"
				+ (useComparer ? " (comparer)" : ""));
		System.out.printf(Locale.ROOT, "%-48s %d recursive hash visits for %d elements (%.1f per element)%n",
				getClass().getSimpleName() + (useComparer ? " with comparer" : " without comparer"), visits.get(),
				elements, visits.get() / (double) elements);
	}

}
