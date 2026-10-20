/*******************************************************************************
 * Copyright (c) 2005, 2026 IBM Corporation and others.
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
package org.eclipse.jface.tests.performance;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

/**
 * Performance tests for {@link TreeViewer}: adding elements in batches, adding
 * them repeatedly, and refreshing an unchanged and a shrunk tree.
 */
public class TreeViewerPerformanceTest extends ViewerTest {

	static final int TEST_COUNT = 1000;

	/** Rounds used by the scenarios that add the same elements over and over. */
	private static final int REPEAT_ROUNDS = 10;

	TreeViewer viewer;

	/**
	 * Set before {@link #openBrowser()} to use the content provider that always
	 * answers the same elements, which is what the refresh scenario measures.
	 */
	private boolean useRefreshContentProvider;

	@Override
	protected StructuredViewer createViewer(Shell shell) {
		viewer = new TreeViewer(shell);
		viewer.setLabelProvider(getLabelProvider());
		if (useRefreshContentProvider) {
			viewer.setContentProvider(new RefreshTestTreeContentProvider());
		} else {
			viewer.setContentProvider(new TestTreeContentProvider());
			viewer.setComparator(new ViewerComparator());
		}
		return viewer;
	}

	@Override
	protected Object getInitialInput() {
		return new TestTreeElement(0, null);
	}

	@Test
	public void testAddOneAtATime() {
		openBrowser();

		for (int i = 0; i < ITERATIONS / 10; i++) {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			processEvents();
			startMeasuring();
			for (TestTreeElement child : input.children) {
				viewer.add(input, child);
				processEvents();
			}
			stopMeasuring();
			assertEquals(TEST_COUNT, viewer.getTree().getItemCount());
		}

		reportTimings();
	}

	@Test
	public void testAddTen() throws CoreException {
		doTestAdd(10, TEST_COUNT, false);
	}

	@Test
	public void testAddFifty() throws CoreException {
		doTestAdd(50, TEST_COUNT, false);
	}

	@Test
	public void testAddHundred() throws CoreException {
		doTestAdd(100, TEST_COUNT, false);
	}

	@Test
	public void testAddThousand() throws CoreException {
		doTestAdd(1000, 2000, false);
	}

	@Test
	public void testAddTwoThousand() throws CoreException {
		doTestAdd(2000, 4000, false);
	}

	@Test
	public void testAddHundredPreSort() throws CoreException {
		doTestAdd(100, TEST_COUNT, true);
	}

	@Test
	public void testAddThousandPreSort() throws CoreException {
		doTestAdd(1000, 2000, true);
	}

	@Test
	public void testAddTenTenTimes() throws CoreException {
		doTestAdd(10, TEST_COUNT, false, REPEAT_ROUNDS);
	}

	@Test
	public void testAddFiftyTenTimes() throws CoreException {
		doTestAdd(50, TEST_COUNT, false, REPEAT_ROUNDS);
	}

	@Test
	public void testAddHundredTenTimes() throws CoreException {
		doTestAdd(100, TEST_COUNT, false, REPEAT_ROUNDS);
	}

	private void doTestAdd(int increment, int total, boolean preSort) throws CoreException {
		doTestAdd(increment, total, preSort, 1);
	}

	/**
	 * Adds the given number of elements in batches of the given size and measures
	 * the additions. More than one round empties the tree before each round, so
	 * the measurement then covers removing the elements again as well.
	 */
	private void doTestAdd(int increment, int total, boolean preSort, int rounds) throws CoreException {
		openBrowser();

		exercise(() -> {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(total);
			if (preSort) {
				viewer.getComparator().sort(viewer, input.children);
			}
			Collection<Object> batches = new ArrayList<>();
			int blocks = input.children.length / increment;
			for (int j = 0; j < blocks; j = j + increment) {
				Object[] batch = new Object[increment];
				System.arraycopy(input.children, j * increment, batch, 0, increment);
				batches.add(batch);
			}
			processEvents();
			Object[] batchArray = batches.toArray();
			startMeasuring();

			for (int round = 0; round < rounds; round++) {
				if (rounds > 1) {
					viewer.remove((Object[]) input.children);
				}
				for (Object batch : batchArray) {
					viewer.add(input, (Object[]) batch);
					processEvents();
				}
			}

			stopMeasuring();
			assertEquals(batchArray.length * increment, viewer.getTree().getItemCount());
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		reportTimings();
	}

	/**
	 * Refreshes a tree whose content provider keeps answering the same elements.
	 */
	@Test
	public void testRefreshUnchangedTree() {
		useRefreshContentProvider = true;
		openBrowser();

		for (int i = 0; i < ITERATIONS; i++) {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
			assertEquals(RefreshTestContentProvider.ELEMENT_COUNT, viewer.getTree().getItemCount());
		}

		reportTimings();
	}

	/**
	 * Refreshes an expanded tree that lost most of its elements, so the refresh
	 * has to remove them from the widget.
	 */
	@Test
	public void testRefreshShrunkTree() throws CoreException {
		openBrowser();

		int largeSize = TEST_COUNT;
		int smallSize = 100;
		exercise(() -> {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(largeSize);

			processEvents();
			viewer.refresh();
			viewer.expandAll();
			input.createChildren(smallSize);
			startMeasuring();
			viewer.refresh();

			stopMeasuring();
			assertEquals(smallSize, viewer.getTree().getItemCount());
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		reportTimings();
	}

	private static class TestTreeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			return ((TestTreeElement) parentElement).children;
		}

		@Override
		public Object getParent(Object element) {
			return ((TestTreeElement) element).parent;
		}

		@Override
		public boolean hasChildren(Object element) {
			return ((TestTreeElement) element).children.length > 0;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
			// nothing to dispose
		}

		@Override
		public void inputChanged(Viewer localViewer, Object oldInput, Object newInput) {
			// the input is read on demand
		}

	}

}
