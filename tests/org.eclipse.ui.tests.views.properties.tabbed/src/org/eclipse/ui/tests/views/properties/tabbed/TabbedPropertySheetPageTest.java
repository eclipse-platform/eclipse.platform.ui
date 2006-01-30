/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed;

import junit.framework.TestCase;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.views.properties.tabbed.sections.InformationTwoSection;
import org.eclipse.ui.tests.views.properties.tabbed.sections.NameSection;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsView;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsViewContentProvider;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.internal.view.Tab;
import org.eclipse.ui.views.properties.tabbed.internal.view.TabbedPropertyComposite;
import org.eclipse.ui.views.properties.tabbed.internal.view.TabbedPropertyList;

public class TabbedPropertySheetPageTest
    extends TestCase {

    private IViewPart propertiesView;

    private TestsView testsView;

    private TreeNode[] treeNodes;

    protected void setUp()
        throws Exception {
        super.setUp();

        /**
         * Open the properties view.
         */
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();
        assertNotNull(workbenchWindow);
        IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
        assertNotNull(workbenchPage);
        propertiesView = workbenchPage.showView(IPageLayout.ID_PROP_SHEET);
        assertNotNull(propertiesView);

        /**
         * Open the Tests view.
         */
        IViewPart view = workbenchPage.showView(TestsView.TESTS_VIEW_ID);
        assertNotNull(view);
        assertTrue(view instanceof TestsView);
        testsView = (TestsView) view;

        /**
         * get the list of tree nodes from the view.
         */
        IContentProvider contentProvider = testsView.getViewer()
            .getContentProvider();
        assertTrue(contentProvider instanceof TestsViewContentProvider);
        TestsViewContentProvider viewContentProvider = (TestsViewContentProvider) contentProvider;
        treeNodes = viewContentProvider.getInvisibleRoot().getChildren();
        assertEquals(treeNodes.length, 8);
    }

    protected void tearDown()
        throws Exception {
        super.tearDown();

        /**
         * Close the properties view.
         */
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();
        assertNotNull(workbenchWindow);
        IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
        assertNotNull(workbenchPage);
        workbenchPage.hideView(propertiesView);
        propertiesView = null;
        /**
         * Close the Tests view.
         */
        workbenchPage.hideView(testsView);
        testsView = null;

        treeNodes = null;
    }

    /**
     * Set the selection in the view to cause the properties view to change.
     * 
     * @param selectedNodes
     *            nodes to select in the view.
     */
    private void setSelection(TreeNode[] selectedNodes) {
        StructuredSelection selection = new StructuredSelection(selectedNodes);
        testsView.getViewer().setSelection(selection, true);
    }

    /**
     * Get the list of tabs from the tabbed properties view.
     * 
     * @return the tab list.
     */
    private TabbedPropertyList getTabbedPropertyList() {
        Control control = testsView.getTabbedPropertySheetPage().getControl();
        assertTrue(control instanceof TabbedPropertyComposite);
        TabbedPropertyComposite tabbedPropertyComposite = (TabbedPropertyComposite) control;
        return tabbedPropertyComposite.getList();
    }

    /**
     * When One Information Node is selected, three tabs display. Tests
     * typeMapper, labelProvider, propertyCategories, afterTab attributes.
     */
    public void test_tabDisplay() {
        /**
         * select node 0 which is an Information
         */
        setSelection(new TreeNode[] {treeNodes[0]});
        TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
        /**
         * First tab is Name
         */
        assertEquals(tabbedPropertyList.getElementAt(0).toString(), "Name");//$NON-NLS-1$
        /**
         * Second tab is Information
         */
        assertEquals(tabbedPropertyList.getElementAt(1).toString(),
            "Information");//$NON-NLS-1$
        /**
         * Third tab is Message
         */
        assertEquals(tabbedPropertyList.getElementAt(2).toString(), "Message");//$NON-NLS-1$
        /**
         * No fourth tab
         */
        assertNull(tabbedPropertyList.getElementAt(3));
    }

    /**
     * When Two Information Node is selected, only two tabs display. Tests
     * enablesFor attribute.
     */
    public void test_enablesForFilter() {
        /**
         * select nodes
         */
        setSelection(new TreeNode[] {treeNodes[0], treeNodes[1]});
        TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
        /**
         * First tab is Information
         */
        assertEquals(tabbedPropertyList.getElementAt(0).toString(),
            "Information");//$NON-NLS-1$
        /**
         * Second tab is Message
         */
        assertEquals(tabbedPropertyList.getElementAt(1).toString(), "Message");//$NON-NLS-1$
        /**
         * No other tab
         */
        assertNull(tabbedPropertyList.getElementAt(2));
    }

    /**
     * When Two Information Node is selected, two section displayed on Name tab.
     * Tests filter, afterSection attribute.
     */
    public void test_sectionInformationTwoFilter() {
        /**
         * select nodes
         */
        setSelection(new TreeNode[] {treeNodes[1]});
        TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
        /**
         * First tab is Information
         */
        assertEquals(tabbedPropertyList.getElementAt(0).toString(), "Name");//$NON-NLS-1$
        Tab tab = testsView.getTabbedPropertySheetPage().getCurrentTab();
        /**
         * the tab has two sections.
         */
        ISection[] sections = tab.getSections();
        assertEquals(sections.length, 2);
        assertEquals(sections[0].getClass(), NameSection.class);
        assertEquals(sections[1].getClass(), InformationTwoSection.class);
    }

    /**
     * When Information, Error and Warning Nodes are selected, only the Message
     * tab displays. Tests input attribute.
     */
    public void test_selectThreeMessageNodes() {
        /**
         * select nodes
         */
        setSelection(new TreeNode[] {treeNodes[1], treeNodes[2], treeNodes[3],});
        TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
        /**
         * Only tab is Message
         */
        assertEquals(tabbedPropertyList.getElementAt(0).toString(), "Message");//$NON-NLS-1$
        /**
         * No other tab
         */
        assertNull(tabbedPropertyList.getElementAt(1));
    }

    /**
     * When File, Folder and Project Nodes are selected, only the Resource tab
     * displays. Tests input attribute.
     */
    public void test_selectThreeResourceNodes() {
        /**
         * select nodes
         */
        setSelection(new TreeNode[] {treeNodes[5], treeNodes[6], treeNodes[7],});
        TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
        /**
         * Only tab is Resource
         */
        assertEquals(tabbedPropertyList.getElementAt(0).toString(), "Resource");//$NON-NLS-1$
        /**
         * No other tab
         */
        assertNull(tabbedPropertyList.getElementAt(1));
    }

    /**
     * When the view first comes up, there is no properties so the "Properties
     * are not available." banner is displayed. Tests null selection in a
     * viewer.
     */
    public void test_noPropertiesAvailable() {
        Tab tab = testsView.getTabbedPropertySheetPage().getCurrentTab();
        assertNull(tab);
        TabbedPropertyList tabbedPropertyList = getTabbedPropertyList();
        assertNull(tabbedPropertyList.getElementAt(0));
    }

}
