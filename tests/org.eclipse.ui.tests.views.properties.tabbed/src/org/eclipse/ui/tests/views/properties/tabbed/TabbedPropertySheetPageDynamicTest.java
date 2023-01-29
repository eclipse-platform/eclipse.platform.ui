/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
package org.eclipse.ui.tests.views.properties.tabbed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsColor;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsElement;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsShape;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsTreeNode;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsView;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsViewContentProvider;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the dynamic tab and section support.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class TabbedPropertySheetPageDynamicTest {

	private DynamicTestsView dynamicTestsView;

	private DynamicTestsTreeNode[] treeNodes;

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
		workbenchPage.closeAllPerspectives(false, false);

		/**
		 * Open the tests perspective.
		 */
		PlatformUI.getWorkbench().showPerspective(
				TestsPerspective.TESTS_PERSPECTIVE_ID, workbenchWindow);

		/**
		 * Open the dynamic tests view.
		 */
		IViewPart view = workbenchPage
				.showView(DynamicTestsView.DYNAMIC_TESTS_VIEW_ID);
		assertNotNull(view);
		assertTrue(view instanceof DynamicTestsView);
		dynamicTestsView = (DynamicTestsView) view;

		/**
		 * get the list of tree nodes from the view.
		 */
		IContentProvider contentProvider = dynamicTestsView.getViewer()
				.getContentProvider();
		assertTrue(contentProvider instanceof DynamicTestsViewContentProvider);
		DynamicTestsViewContentProvider viewContentProvider = (DynamicTestsViewContentProvider) contentProvider;
		treeNodes = (DynamicTestsTreeNode[]) viewContentProvider
				.getInvisibleRoot().getChildren();
		assertEquals(11, treeNodes.length);
	}

	@After
	public void tearDown() {

		/**
		 * Bug 175070: Make sure the views have finished painting.
		 */
		while (Display.getCurrent().readAndDispatch()) {
			//
		}

		/**
		 * Deselect everything in the Tests view.
		 */
		setSelection(new DynamicTestsTreeNode[] {});
	}

	/**
	 * Set the selection in the view to cause the properties view to change.
	 *
	 * @param selectedNodes
	 *            nodes to select in the view.
	 */
	private void setSelection(DynamicTestsTreeNode[] selectedNodes) {
		StructuredSelection selection = new StructuredSelection(selectedNodes);
		dynamicTestsView.getViewer().setSelection(selection, true);
	}

	/**
	 * When the three blue nodes are selected, two tabs display.
	 */
	@Test
	public void test_BlueStaticContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_STATIC);
		select_all_blue();
	}

	/**
	 * When the three blue nodes are selected, two tabs display.
	 */
	@Test
	public void test_BlueDynamicTabContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_TABS);
		select_all_blue();
	}

	/**
	 * When the three blue nodes are selected, two tabs display.
	 */
	@Test
	public void test_BlueDynamicSectionContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS);
		select_all_blue();
	}

	/**
	 * When the three blue nodes are selected, two tabs display.
	 */
	public void select_all_blue() {
		List<DynamicTestsTreeNode> blueList = new ArrayList<>();
		for (DynamicTestsTreeNode treeNode : treeNodes) {
			if (DynamicTestsColor.BLUE
					.equals(treeNode.getDynamicTestsElement().getPropertyValue(DynamicTestsElement.ID_COLOR))) {
				blueList.add(treeNode);
			}
		}
		DynamicTestsTreeNode[] selectNodes = blueList.toArray(new DynamicTestsTreeNode[blueList.size()]);
		assertEquals(3, blueList.size());

		setSelection(selectNodes);

		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * First tab is Element
		 */
		assertEquals("Element", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Color
		 */
		assertEquals("Color", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(2, tabDescriptors.length);
	}

	/**
	 * When the three triangle nodes are selected, two tabs display.
	 */
	@Test
	public void test_TriangleStaticContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_STATIC);
		select_all_triangle();
	}

	/**
	 * When the three triangle nodes are selected, two tabs display.
	 */
	@Test
	public void test_TriangleDynamicTabContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_TABS);
		select_all_triangle();
	}

	/**
	 * When the three triangle nodes are selected, two tabs display.
	 */
	@Test
	public void test_TriangleDynamicSectionContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS);
		select_all_triangle();
	}

	/**
	 * When the three triangle nodes are selected, two tabs display.
	 */
	public void select_all_triangle() {
		List<DynamicTestsTreeNode> triangleList = new ArrayList<>();
		for (DynamicTestsTreeNode treeNode : treeNodes) {
			if (DynamicTestsShape.TRIANGLE
					.equals(treeNode.getDynamicTestsElement().getPropertyValue(DynamicTestsElement.ID_SHAPE))) {
				triangleList.add(treeNode);
			}
		}
		DynamicTestsTreeNode[] selectNodes = triangleList.toArray(new DynamicTestsTreeNode[triangleList.size()]);
		assertEquals(4, triangleList.size());

		setSelection(selectNodes);

		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * First tab is Element
		 */
		assertEquals("Element", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Shape
		 */
		assertEquals("Shape", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(2, tabDescriptors.length);
	}

	/**
	 * When the black triangle is selected, three tabs display.
	 */
	@Test
	public void test_BlackTriangleStaticContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_STATIC);
		select_blackTriangle();
		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * Only three tabs displayed for static contribution.
		 */
		assertEquals(3, tabDescriptors.length);
	}

	/**
	 * When the black triangle is selected, four tabs display.
	 */
	@Test
	public void test_BlackTriangleDynamicTabContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_TABS);
		select_blackTriangle();
		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * Fourth tab is Black
		 */
		assertEquals("Black", tabDescriptors[3].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(4, tabDescriptors.length);
	}

	/**
	 * When the black triangle is selected, three tabs display.
	 */
	@Test
	public void test_BlackTriangleDynamicSectionContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS);
		select_blackTriangle();
		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * Only three tabs displayed for dynamic section contribution.
		 */
		assertEquals(3, tabDescriptors.length);
	}

	/**
	 * When the black triangle is selected, three tabs display.
	 */
	public void select_blackTriangle() {
		DynamicTestsTreeNode blackTriangleNode = null;
		for (DynamicTestsTreeNode treeNode : treeNodes) {
			if (DynamicTestsColor.BLACK
					.equals(treeNode.getDynamicTestsElement().getPropertyValue(DynamicTestsElement.ID_COLOR))) {
				blackTriangleNode = treeNode;
				break;
			}
		}
		assertNotNull(blackTriangleNode);

		setSelection(new DynamicTestsTreeNode[] { blackTriangleNode });

		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * First tab is Element
		 */
		assertEquals("Element", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Shape
		 */
		assertEquals("Shape", tabDescriptors[1].getLabel());//$NON-NLS-1$
		/**
		 * Third tab is Advanced
		 */
		assertEquals("Advanced", tabDescriptors[2].getLabel());//$NON-NLS-1$
	}

	/**
	 * When the red star is selected, three tabs display.
	 */
	@Test
	public void test_RedStarStaticContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_STATIC);
		select_RedStar();
		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * Third tab is Advanced
		 */
		assertEquals("Advanced", tabDescriptors[2].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(3, tabDescriptors.length);
	}

	/**
	 * When the red star is selected, four tabs display.
	 */
	@Test
	public void test_RedStarDynamicTabContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_TABS);
		select_RedStar();
		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * Third tab is Advanced
		 */
		assertEquals("Advanced", tabDescriptors[2].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(3, tabDescriptors.length);
	}

	/**
	 * When the red star is selected, three tabs display.
	 */
	@Test
	public void test_RedStarDynamicSectionContribution() {
		dynamicTestsView
				.setContributorId(DynamicTestsView.DYNAMIC_TESTS_VIEW_DYNAMIC_SECTIONS);
		select_RedStar();
		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * Third tab is Star
		 */
		assertEquals("Star", tabDescriptors[2].getLabel());//$NON-NLS-1$
		/**
		 * Fourth tab is Advanced
		 */
		assertEquals("Advanced", tabDescriptors[3].getLabel());//$NON-NLS-1$
		/**
		 * No other tab
		 */
		assertEquals(4, tabDescriptors.length);
	}

	/**
	 * When the red star is selected, four tabs display.
	 */
	public void select_RedStar() {
		DynamicTestsTreeNode redStarNode = null;
		for (DynamicTestsTreeNode treeNode : treeNodes) {
			if (DynamicTestsShape.STAR
					.equals(treeNode.getDynamicTestsElement().getPropertyValue(DynamicTestsElement.ID_SHAPE))) {
				redStarNode = treeNode;
				break;
			}
		}
		assertNotNull(redStarNode);

		setSelection(new DynamicTestsTreeNode[] { redStarNode });

		ITabDescriptor[] tabDescriptors = dynamicTestsView.getTabbedPropertySheetPage().getActiveTabs();
		/**
		 * First tab is Element
		 */
		assertEquals("Element", tabDescriptors[0].getLabel());//$NON-NLS-1$
		/**
		 * Second tab is Color
		 */
		assertEquals("Color", tabDescriptors[1].getLabel());//$NON-NLS-1$
	}

}
