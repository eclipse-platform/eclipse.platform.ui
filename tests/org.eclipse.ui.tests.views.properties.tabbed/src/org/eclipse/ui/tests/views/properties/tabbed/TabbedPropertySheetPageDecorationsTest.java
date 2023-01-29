/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.views.properties.tabbed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.tests.views.properties.tabbed.decorations.TabbedPropertySheetPageWithDecorations;
import org.eclipse.ui.tests.views.properties.tabbed.decorations.views.DecorationTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsViewContentProvider;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TabbedPropertySheetPageDecorationsTest {

	private DecorationTestsView decorationTestsView;

	private TreeNode[] treeNodes;

	@Before
	public void setUp() throws WorkbenchException {
		/**
		 * Close the existing perspectives.
		 */
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow();
		assertNotNull(workbenchWindow);
		IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		assertNotNull(workbenchPage);
		closeIntroView(workbenchPage);
		processUiEvents();
		workbenchPage.closeAllPerspectives(false, false);
		processUiEvents();

		/**
		 * Open the tests perspective.
		 */
		PlatformUI.getWorkbench().showPerspective(
			TestsPerspective.TESTS_PERSPECTIVE_ID, workbenchWindow);
		processUiEvents();
		/**
		 * Select the Decoration Tests view.
		 */
		IViewPart view = workbenchPage.showView(DecorationTestsView.DECORATION_TESTS_VIEW_ID);
		processUiEvents();

		assertNotNull(view);
		assertTrue(view instanceof DecorationTestsView);
		assertTrue(workbenchPage.isPartVisible(view));
		decorationTestsView = (DecorationTestsView) view;

		/**
		 * get the list of tree nodes from the view.
		 */
		IContentProvider contentProvider = decorationTestsView.getViewer()
			.getContentProvider();
		assertTrue(contentProvider instanceof TestsViewContentProvider);
		TestsViewContentProvider viewContentProvider = (TestsViewContentProvider) contentProvider;
		treeNodes = viewContentProvider.getInvisibleRoot().getChildren();
		assertEquals(8, treeNodes.length);
	}

	private void closeIntroView(IWorkbenchPage workbenchPage) {
		IViewPart intro = workbenchPage.findView("org.eclipse.ui.internal.introview");
		if (intro != null) {
			workbenchPage.hideView(intro);
		}
	}

	@After
	public void tearDown() {

		/**
		 * Bug 175070: Make sure the views have finished painting.
		 */
		processUiEvents();

		/**
		 * Deselect everything in the Tests view.
		 */
		setSelection(new TreeNode[] {} );
	}

	private void processUiEvents() {
		while (Display.getCurrent().readAndDispatch()) {
			//
		}
	}

	/**
	 * Set the selection in the view to cause the properties view to change.
	 *
	 * @param selectedNodes
	 *            nodes to select in the view.
	 */
	private void setSelection(TreeNode[] selectedNodes) {
		StructuredSelection selection = new StructuredSelection(selectedNodes);
		decorationTestsView.getViewer().setSelection(selection, true);
	}

	/**
	 * When Information node is selected, the Information tab is widest if decorations are not used.
	 */
	@Test
	public void test_widestLabelIndex1_WithoutDecorations() {
		((TabbedPropertySheetPageWithDecorations)decorationTestsView.getTabbedPropertySheetPage()).useDecorations(false);
		/**
		 * select Information node
		 */
		setSelection(new TreeNode[] {treeNodes[0]});
		ITabDescriptor[] tabDescriptors = decorationTestsView.getTabbedPropertySheetPage().getActiveTabs();

		/**
		 * First tab is Name
		 */
		assertEquals("Name", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Information
		 */
		assertEquals("Information", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * Third tab is Message
		 */
		assertEquals("Message", tabDescriptors[2].getLabel());//$NON-NLS-1$
		/**
		 * No fourth tab
		 */
		assertEquals(3, tabDescriptors.length);

		/**
		 * Information tab is widest
		 */
		assertEquals(1, ((TabbedPropertyComposite) decorationTestsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
	}

	/**
	 * When Information node is selected, the Name tab is widest if decorations are used.
	 */
	@Test
	public void test_widestLabelIndex1_WithDecorations() {
		((TabbedPropertySheetPageWithDecorations)decorationTestsView.getTabbedPropertySheetPage()).useDecorations(true);
		/**
		 * select Information node
		 */
		setSelection(new TreeNode[] {treeNodes[0]});
		ITabDescriptor[] tabDescriptors = decorationTestsView.getTabbedPropertySheetPage().getActiveTabs();

		/**
		 * First tab is Name
		 */
		assertEquals("Name", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Information
		 */
		assertEquals("Information", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * Third tab is Message
		 */
		assertEquals("Message", tabDescriptors[2].getLabel());//$NON-NLS-1$
		/**
		 * No fourth tab
		 */
		assertEquals(3, tabDescriptors.length);

		/**
		 * Name tab is widest
		 */
		assertEquals(0, ((TabbedPropertyComposite) decorationTestsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
	}

	/**
	 * When Two Information nodes are selected, the Information tab is widest if decorations are not used.
	 */
	@Test
	public void test_widestLabelIndex2_WithoutDecorations() {
		((TabbedPropertySheetPageWithDecorations)decorationTestsView.getTabbedPropertySheetPage()).useDecorations(false);
		/**
		 * select nodes
		 */
		setSelection(new TreeNode[] {treeNodes[0], treeNodes[1]});
		ITabDescriptor[] tabDescriptors = decorationTestsView.getTabbedPropertySheetPage().getActiveTabs();

		/**
		 * First tab is Information
		 */
		assertEquals("Information", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Message
		 */
		assertEquals("Message", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(2, tabDescriptors.length);

		/**
		 * Information tab is widest
		 */
		assertEquals(0, ((TabbedPropertyComposite) decorationTestsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
	}

	/**
	 * When Two Information nodes are selected, the Message tab is widest if decorations are used.
	 */
	@Test
	public void test_widestLabelIndex2_WithDecorations() {
		((TabbedPropertySheetPageWithDecorations)decorationTestsView.getTabbedPropertySheetPage()).useDecorations(true);
		/**
		 * select nodes
		 */
		setSelection(new TreeNode[] {treeNodes[0], treeNodes[1]});
		ITabDescriptor[] tabDescriptors = decorationTestsView.getTabbedPropertySheetPage().getActiveTabs();

		/**
		 * First tab is Information
		 */
		assertEquals("Information", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Message
		 */
		assertEquals("Message", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(2, tabDescriptors.length);

		/**
		 * Message tab is widest
		 */
		assertEquals(1, ((TabbedPropertyComposite) decorationTestsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
	}
}
