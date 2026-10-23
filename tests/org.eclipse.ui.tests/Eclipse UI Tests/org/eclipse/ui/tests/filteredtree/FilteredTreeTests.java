/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 *     Sebastian Lohmeier <sebastian@monochromata.de> - Bug 484310
 ******************************************************************************/

package org.eclipse.ui.tests.filteredtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.After;
import org.junit.Test;

public class FilteredTreeTests {
	private FilteredTree fTreeViewer;
	private TestElement fRootElement;
	// create an 8000-item Tree
	private static int DEPTH = 3;
	private static int NUM_ITEMS = 20;

	private class MyFilteredTree extends FilteredTree{
		public MyFilteredTree(Composite comp, int style) {
			super(comp);
			doSomeStuffBeforeWidgetCreation();
			init(style, new PatternFilter());
		}

		private void doSomeStuffBeforeWidgetCreation(){
			// do nothing
		}
	}

	private abstract class FilteredTreeDialog extends Dialog {
		private final int style;

		public FilteredTreeDialog(Shell shell, int treeStyle){
			super(shell);
			style = treeStyle;
		}
		@Override
		protected Control createContents(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setLayout(new GridLayout());

			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			fTreeViewer = doCreateFilteredTree(c, style);
			fTreeViewer.getViewer().setUseHashlookup(true);
			setTreeInput();
			return parent;
		}

		protected void setTreeInput() {
			setInput();
		}

		protected abstract FilteredTree doCreateFilteredTree(Composite comp, int style);

	}

	@Test
	public void testCreateFilteredTree(){
		runFilteredTreeTest(SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
	}

	@Test
	public void testCreateCheckboxFilteredTree(){
		runFilteredTreeTest(SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.CHECK);
	}
	/*
	 * Tests creation of a subclass of filtered tree, using alternate constructor.
	 */
	@Test
	public void testCreateMyFilteredTree(){
		fRootElement = TestElement.createModel(DEPTH, NUM_ITEMS);
		final int treeStyle = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL |SWT.FLAT;

		Dialog dialog = new FilteredTreeDialog((Shell)null, treeStyle){
			@Override
			protected FilteredTree doCreateFilteredTree(Composite comp, int style) {
				return createMyFilteredTree(comp, treeStyle);
			}
		};

		dialog.create();

		Assert.isNotNull(fTreeViewer, "Filtered tree is null");
		assertNumberOfTopLevelItems(NUM_ITEMS);

		dialog.close();
	}

	@Test
	public void testAddAndRemovePattern() {
		Dialog dialog = createFilteredTreeDialog();

		Assert.isNotNull(fTreeViewer, "Filtered tree is null");
		assertNumberOfTopLevelItems(NUM_ITEMS);

		applyPattern("0-0-0-0 name-*");
		assertNumberOfTopLevelItems(1);

		applyPattern(" 0-0-0-0 name-*");
		assertNumberOfTopLevelItems(1);

		applyPattern("0-0-0-0 name-* ");
		assertNumberOfTopLevelItems(1);

		applyPattern("0-0-0-0 name unknownWord");
		assertNumberOfTopLevelItems(0);

		applyPattern("");
		assertNumberOfTopLevelItems(NUM_ITEMS);

		dialog.close();
	}

	@Test
	public void testClearingFilterRevealsSelection() {
		Dialog dialog = createFilteredTreeDialog();
		TreeViewer viewer = fTreeViewer.getViewer();
		TestElement leaf = fRootElement.getChildAt(5).getChildAt(7).getChildAt(9);

		filterAndClear(viewer, leaf.toString(), new StructuredSelection(leaf));

		assertEquals(leaf, viewer.getStructuredSelection().getFirstElement());
		assertTrue("Parent of the selection is collapsed", viewer.getExpandedState(leaf.getContainer()));
		assertTrue("Grandparent of the selection is collapsed",
				viewer.getExpandedState(leaf.getContainer().getContainer()));

		dialog.close();
	}

	@Test
	public void testClearingFilterRevealsSelectionInQuickSelectionMode() {
		Dialog dialog = createFilteredTreeDialog();
		fTreeViewer.setQuickSelectionMode(true);
		TreeViewer viewer = fTreeViewer.getViewer();
		TestElement leaf = fRootElement.getChildAt(5).getChildAt(7).getChildAt(9);

		filterAndClear(viewer, leaf.toString(), new StructuredSelection(leaf));

		assertEquals(leaf, viewer.getStructuredSelection().getFirstElement());
		assertTrue("Parent of the selection is collapsed", viewer.getExpandedState(leaf.getContainer()));

		dialog.close();
	}

	@Test
	public void testClearingFilterRevealsSelectedPathOfRepeatedElement() {
		Dialog dialog = createStringTreeDialog(SWT.SINGLE);
		TreeViewer viewer = fTreeViewer.getViewer();
		TreePath selected = new TreePath(new Object[] { "b", "b1", "leaf" });

		filterAndClear(viewer, "leaf", new TreeSelection(selected));

		assertEquals(new TreeSelection(selected), viewer.getStructuredSelection());
		assertTrue("Selected path is collapsed", viewer.getExpandedState(selected.getParentPath()));
		assertFalse("Other occurrence was expanded", viewer.getExpandedState(new TreePath(new Object[] { "a" })));

		dialog.close();
	}

	@Test
	public void testClearingFilterDoesNotSelectOtherOccurrenceOfRemovedElement() {
		StringTreeContentProvider contentProvider = new StringTreeContentProvider();
		Dialog dialog = createStringTreeDialog(SWT.MULTI, contentProvider);
		TreeViewer viewer = fTreeViewer.getViewer();
		Text filterControl = fTreeViewer.getFilterControl();
		TreePath kept = new TreePath(new Object[] { "a", "a1", "leafA" });
		int unfilteredCount = viewer.getTree().getItemCount();
		filterControl.setText("leaf");
		assertTrue("Filter was not applied", DisplayHelper.waitForCondition(viewer.getControl().getDisplay(), 5000,
				() -> viewer.getTree().getItemCount() < unfilteredCount));
		viewer.setSelection(new TreeSelection(new TreePath[] { kept, new TreePath(new Object[] { "b", "b1", "leaf" }) }),
				true);
		contentProvider.children.put("b1", List.of("leafB"));

		filterControl.setText("");
		assertTrue("Filter was not cleared", DisplayHelper.waitForCondition(viewer.getControl().getDisplay(), 5000,
				() -> viewer.getTree().getItemCount() == unfilteredCount));

		assertEquals(new TreeSelection(kept), viewer.getStructuredSelection());

		dialog.close();
	}

	@Test
	public void testWideningFilterRevealsSelection() {
		Dialog dialog = createStringTreeDialog(SWT.SINGLE);
		TreeViewer viewer = fTreeViewer.getViewer();
		TreePath selected = new TreePath(new Object[] { "b", "b1", "leafB" });

		filterAndWiden(fTreeViewer.getFilterControl(), viewer, "leafB", new TreeSelection(selected), "leaf");

		assertEquals(new TreeSelection(selected), viewer.getStructuredSelection());
		assertTrue("Selected path is collapsed", viewer.getExpandedState(selected.getParentPath()));

		dialog.close();
	}

	@Test
	public void testWideningFilterKeepsMatchingSelectionInQuickSelectionMode() {
		Dialog dialog = createStringTreeDialog(SWT.SINGLE);
		fTreeViewer.setQuickSelectionMode(true);
		TreeViewer viewer = fTreeViewer.getViewer();
		TreePath selected = new TreePath(new Object[] { "b", "b1", "leafB" });

		filterAndWiden(fTreeViewer.getFilterControl(), viewer, "leafB", new TreeSelection(selected), "leaf");

		assertEquals(new TreeSelection(selected), viewer.getStructuredSelection());

		dialog.close();
	}

	@Test
	public void testClearingFilterRevealsSelectionInE4Tree() {
		Shell shell = new Shell();
		shell.setLayout(new GridLayout());
		shell.setSize(400, 500);
		try {
			var tree = new org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree(shell, SWT.SINGLE | SWT.BORDER,
					new org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter());
			TreeViewer viewer = tree.getViewer();
			viewer.setContentProvider(new StringTreeContentProvider());
			viewer.setLabelProvider(new LabelProvider());
			viewer.setInput(StringTreeContentProvider.CHILDREN);
			TreePath selected = new TreePath(new Object[] { "b", "b1", "leafB" });

			filterAndWiden(tree.getFilterControl(), viewer, "leafB", new TreeSelection(selected), "");

			assertEquals(new TreeSelection(selected), viewer.getStructuredSelection());
			assertTrue("Selected path is collapsed", viewer.getExpandedState(selected.getParentPath()));
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testClearingFilterRevealsAllSelectedPaths() {
		Dialog dialog = createStringTreeDialog(SWT.MULTI);
		TreeViewer viewer = fTreeViewer.getViewer();
		TreePath first = new TreePath(new Object[] { "a", "a1", "leafA" });
		TreePath second = new TreePath(new Object[] { "b", "b1", "leafB" });

		filterAndClear(viewer, "leaf", new TreeSelection(new TreePath[] { first, second }));

		assertEquals(2, viewer.getStructuredSelection().size());
		assertTrue("First selected path is collapsed", viewer.getExpandedState(first.getParentPath()));
		assertTrue("Second selected path is collapsed", viewer.getExpandedState(second.getParentPath()));

		dialog.close();
	}

	private void filterAndClear(TreeViewer viewer, String filter, IStructuredSelection selection) {
		filterAndWiden(fTreeViewer.getFilterControl(), viewer, filter, selection, "");
	}

	private static void filterAndWiden(Text filterControl, TreeViewer viewer, String filter,
			IStructuredSelection selection, String widerFilter) {
		int unfilteredCount = viewer.getTree().getItemCount();
		filterControl.setText(filter);
		assertTrue("Filter was not applied", DisplayHelper.waitForCondition(viewer.getControl().getDisplay(), 5000,
				() -> viewer.getTree().getItemCount() < unfilteredCount));
		viewer.setSelection(selection, true);
		int filteredCount = viewer.getTree().getItemCount();

		filterControl.setText(widerFilter);
		assertTrue("Filter was not widened", DisplayHelper.waitForCondition(viewer.getControl().getDisplay(), 5000,
				() -> viewer.getTree().getItemCount() > filteredCount));
	}

	private Dialog createStringTreeDialog(int selectionStyle) {
		return createStringTreeDialog(selectionStyle, new StringTreeContentProvider());
	}

	private Dialog createStringTreeDialog(int selectionStyle, StringTreeContentProvider contentProvider) {
		Dialog dialog = new FilteredTreeDialog((Shell) null, selectionStyle | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER) {
			@Override
			protected FilteredTree doCreateFilteredTree(Composite comp, int style) {
				FilteredTree tree = createFilteredTree(comp, style);
				tree.getViewer().setContentProvider(contentProvider);
				return tree;
			}

			@Override
			protected void setTreeInput() {
				fTreeViewer.getViewer().setInput(StringTreeContentProvider.CHILDREN);
			}
		};
		dialog.create();
		return dialog;
	}

	/**
	 * Tree of strings in which "leaf" appears under both "a/a1" and "b/b1".
	 */
	private static class StringTreeContentProvider implements ITreeContentProvider {
		static final Map<String, List<String>> CHILDREN = Map.of( //
				"", List.of("a", "b", "c", "d", "e", "f"), //
				"a", List.of("a1"), //
				"b", List.of("b1"), //
				"a1", List.of("leaf", "leafA"), //
				"b1", List.of("leaf", "leafB"));

		final Map<String, List<String>> children = new HashMap<>(CHILDREN);

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren("");
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return children.getOrDefault(parentElement, List.of()).toArray();
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return children.containsKey(element);
		}
	}

	private void runFilteredTreeTest(final int treeStyle){
		Dialog dialog = createFilteredTreeDialog(treeStyle);

		Assert.isNotNull(fTreeViewer, "Filtered tree is null");
		assertNumberOfTopLevelItems(NUM_ITEMS);

		dialog.close();
	}

	private Dialog createFilteredTreeDialog() {
		return createFilteredTreeDialog(SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	}

	private Dialog createFilteredTreeDialog(final int treeStyle) {
		fRootElement = TestElement.createModel(DEPTH, NUM_ITEMS);

		Dialog dialog = new FilteredTreeDialog((Shell)null, treeStyle){
			@Override
			protected FilteredTree doCreateFilteredTree(Composite comp, int style) {
				return createFilteredTree(comp, treeStyle);
			}
		};

		dialog.create();
		return dialog;
	}

	private FilteredTree createFilteredTree(Composite parent, int style){
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		FilteredTree fTree = new FilteredTree(c, style, new PatternFilter());

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 400;
		gd.heightHint = 500;
		fTree.setLayoutData(gd);
		fTree.getViewer().setContentProvider(new TestModelContentProvider());
		fTree.getViewer().setLabelProvider(new LabelProvider());

		return fTree;
	}

	private FilteredTree createMyFilteredTree(Composite parent, int style){
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		FilteredTree fTree = new MyFilteredTree(c, style);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 400;
		gd.heightHint = 500;
		fTree.setLayoutData(gd);
		fTree.getViewer().setContentProvider(new TestModelContentProvider());
		fTree.getViewer().setLabelProvider(new LabelProvider());
		return fTree;
	}

	private void assertNumberOfTopLevelItems(int expectedCount) {
		int actualCount = fTreeViewer.getViewer().getTree().getItemCount();
		Assert.isTrue(actualCount == expectedCount,
				"tree item count " + actualCount + " does not match expected: " + expectedCount);
	}

	private void applyPattern(String pattern) {
		fTreeViewer.getPatternFilter().setPattern(pattern);
		fTreeViewer.getViewer().refresh();
	}

	private void setInput() {
		fTreeViewer.getViewer().setInput(fRootElement);
	}

	@After
	public void doTearDown() throws Exception {
		fTreeViewer = null;
		fRootElement = null;
	}


}
