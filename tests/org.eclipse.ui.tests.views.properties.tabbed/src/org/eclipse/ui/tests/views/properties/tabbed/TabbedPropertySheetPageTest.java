/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.tests.views.properties.tabbed.sections.InformationTwoSection;
import org.eclipse.ui.tests.views.properties.tabbed.sections.NameSection;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsView;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsViewContentProvider;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabContents;

public class TabbedPropertySheetPageTest
    extends TestCase {

    private TestsView testsView;

    private TreeNode[] treeNodes;

    protected void setUp()
        throws Exception {
        super.setUp();

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
         * Select the Tests view.
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
		 * Bug 175070: Make sure the views have finished painting.
         */
        while (Display.getCurrent().readAndDispatch()) {
            //
        }

        /**
         * Deselect everything in the Tests view.
         */
        setSelection(new TreeNode[] {} );
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
     * When One Information Node is selected, three tabs display. Tests
     * typeMapper, labelProvider, propertyCategories, afterTab attributes.
     */
    public void test_tabDisplay() {
        /**
         * select node 0 which is an Information
         */
        setSelection(new TreeNode[] {treeNodes[0]});
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();

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
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();
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
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();
        /**
         * First tab is Information
         */
        assertEquals("Name", tabDescriptors[0].getLabel());//$NON-NLS-1$
        TabContents tabContents = testsView.getTabbedPropertySheetPage().getCurrentTab();
        /**
         * the tab has two sections.
         */
        ISection[] sections = tabContents.getSections();
        assertEquals(2, sections.length);
        assertEquals(NameSection.class, sections[0].getClass());
        assertEquals(InformationTwoSection.class, sections[1].getClass());
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
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();
        /**
         * Only tab is Message
         */
        assertEquals("Message", tabDescriptors[0].getLabel());//$NON-NLS-1$
        /**
         * No other tab
         */
        assertEquals(1, tabDescriptors.length);
    }

    /**
     * When Information node is selected, the Information tab is widest.
     */
    public void test_widestLabelIndex1() {
        /**
         * select Information node
         */
        setSelection(new TreeNode[] {treeNodes[0]});
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();

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
        assertEquals(1, ((TabbedPropertyComposite) testsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
    }

    /**
     * When Error node is selected, the Message tab is widest.
     */
    public void test_widestLabelIndex2() {
        /**
         * select Error node
         */
        setSelection(new TreeNode[] {treeNodes[2]});
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();

        /**
         * First tab is Name
         */
        assertEquals("Name", tabDescriptors[0].getLabel());//$NON-NLS-1$
        /**
         * Second tab is Error
         */
        assertEquals("Error", tabDescriptors[1].getLabel());//$NON-NLS-1$
        /**
         * Third tab is Message
         */
        assertEquals("Message", tabDescriptors[2].getLabel());//$NON-NLS-1$
        /**
         * No fourth tab
         */
        assertEquals(3, tabDescriptors.length);

        /**
         * Message tab is widest
         */
        assertEquals(2, ((TabbedPropertyComposite) testsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
    }

    /**
     * When Warning node is selected, the Warning tab is widest.
     */
    public void test_widestLabelIndex3() {
        /**
         * select Warning node
         */
        setSelection(new TreeNode[] {treeNodes[3]});
        ITabDescriptor[] tabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();

        /**
         * First tab is Name
         */
        assertEquals("Name", tabDescriptors[0].getLabel());//$NON-NLS-1$
        /**
         * Second tab is Warning
         */
        assertEquals("Warning", tabDescriptors[1].getLabel());//$NON-NLS-1$
        /**
         * Third tab is Message
         */
        assertEquals("Message", tabDescriptors[2].getLabel());//$NON-NLS-1$
        /**
         * No fourth tab
         */
        assertEquals(3, tabDescriptors.length);

        /**
         * Warning tab is widest
         */
        assertEquals(1, ((TabbedPropertyComposite) testsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
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
        ITabDescriptor[] TabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();
        /**
         * Only tab is Resource
         */
        assertEquals("Resource", TabDescriptors[0].getLabel());//$NON-NLS-1$
        /**
         * No other tab
         */
        assertEquals(1, TabDescriptors.length);
    }

    /**
     * When the view first comes up, there is no properties so the "Properties
     * are not available." banner is displayed. Tests null selection in a
     * viewer.
     */
    public void test_noPropertiesAvailable() {
    	TabContents tabContents = testsView.getTabbedPropertySheetPage().getCurrentTab();
        assertNull(tabContents);
        ITabDescriptor[] TabDescriptors = testsView.getTabbedPropertySheetPage().getActiveTabs();
        assertEquals(0, TabDescriptors.length);
        /**
         * widestLabelIndex should be -1
         */
        assertEquals(-1, ((TabbedPropertyComposite) testsView.getTabbedPropertySheetPage().getControl()).getList().getWidestLabelIndex());
    }

}
