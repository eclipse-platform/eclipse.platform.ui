/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.navigator.ResourceNavigator;

public class ResourceNavigatorTest extends UITestCase {
    private IWorkbenchPage activePage;

    private IWorkbenchWindow workbenchWindow;

    private IViewPart view;

    private IProject p1;

    private IProject p2;

    private IFile f1;

    private IFile f2;

    /**
     * Constructor for ResourceNavigatorTest.
     * @param testName
     */
    public ResourceNavigatorTest(String testName) {
        super(testName);
    }

    protected void doSetUp() throws Exception {
        super.doSetUp();
        workbenchWindow = openTestWindow();
        activePage = workbenchWindow.getActivePage();
    }

    private void setupView() throws Throwable {
        view = activePage.showView("org.eclipse.ui.views.ResourceNavigator");
    }

    private void setupResources() throws Throwable {
        if (p1 == null) {
            p1 = FileUtil.createProject("TP1");
            f1 = null;
        }
        if (p2 == null) {
            p2 = FileUtil.createProject("TP2");
            f2 = null;
        }
        if (f1 == null)
            f1 = FileUtil.createFile("f1.txt", p1);
        if (f2 == null)
            f2 = FileUtil.createFile("f2.txt", p2);
    }

    public void fixTestGlobalBookmarkAction() throws Throwable {
        setupView();
        setupResources();

        // Select a file
        IStructuredSelection sel = new StructuredSelection(f1);
        ((ResourceNavigator) view).selectReveal(sel);

        // Remember the previous marker count on the file
        int oldCount = (f1.findMarkers(IMarker.BOOKMARK, true,
                IResource.DEPTH_INFINITE)).length;

        // Now try the bookmark action
        ActionUtil.runActionUsingPath(this, workbenchWindow,
                IWorkbenchActionConstants.M_EDIT + '/'
                        + IWorkbenchActionConstants.BOOKMARK);

        // Make sure the resource was bookmarked
        int newCount = (f1.findMarkers(IMarker.BOOKMARK, true,
                IResource.DEPTH_INFINITE)).length;
        assertTrue(
                "Selected file was not bookmarked via Edit->Bookmark action.",
                oldCount + 1 == newCount);
    }

    /*	
     * This test should be moved to an interactive test suite as this
     * test causes a dialog to popup when the resource is deleted by
     * the delete action
     * 
     public void testGlobalDeleteAction() throws Throwable {
     setupView();
     setupResources();

     // Select a file
     IStructuredSelection sel = new StructuredSelection(f1);
     ((ResourceNavigator) view).selectReveal(sel);
     
     // Now try the delete action
     ActionUtil.runActionUsingPath(this, workbenchWindow, IWorkbenchActionConstants.M_EDIT + '/' + IWorkbenchActionConstants.DELETE);
     
     // Make sure the resource was deleted
     assertTrue("Selected file was not deleted via Edit->Delete action.", p1.findMember(f1.getName()) == null);
     f1 = null;
     }
     */

    public void testSelectReveal() throws Throwable {
        setupView();
        setupResources();

        ISetSelectionTarget part = (ISetSelectionTarget) view;
        TreeViewer tree = ((ResourceNavigator) view).getViewer();

        // Set the selection in the navigator
        IStructuredSelection sel1 = new StructuredSelection(f1);
        part.selectReveal(sel1);
        // Get the selection the tree has
        IStructuredSelection treeSel1 = (IStructuredSelection) tree
                .getSelection();
        assertTrue("First selection wrong size, should be only one.", treeSel1
                .size() == 1);
        IResource resource1 = (IResource) treeSel1.getFirstElement();
        assertTrue("First selection contains wrong file resource.", resource1
                .equals(f1));

        // Set the selection in the navigator
        IStructuredSelection sel2 = new StructuredSelection(p2);
        part.selectReveal(sel2);
        // Get the selection the tree has
        IStructuredSelection treeSel2 = (IStructuredSelection) tree
                .getSelection();
        assertTrue("Second selection wrong size, should be only one.", treeSel2
                .size() == 1);
        IResource resource2 = (IResource) treeSel2.getFirstElement();
        assertTrue("Second selection contains wrong project resource.",
                resource2.equals(p2));
    }

    public void testWorkingSet() throws Throwable {
        setupView();
        setupResources();

        ResourceNavigator navigator = ((ResourceNavigator) view);
        IWorkingSetManager workingSetManager = fWorkbench
                .getWorkingSetManager();
        IWorkingSet workingSet = workingSetManager.createWorkingSet("ws1",
                new IAdaptable[] { f1 });

        assertNull(navigator.getWorkingSet());

        navigator.setWorkingSet(workingSet);
        assertEquals(workingSet, navigator.getWorkingSet());

        navigator.setWorkingSet(null);
        assertNull(navigator.getWorkingSet());

        FileUtil.createFile("f11.txt", p1);
        navigator.setWorkingSet(workingSet);
        TreeViewer viewer = navigator.getTreeViewer();
        viewer.expandAll();
        TreeItem[] items = viewer.getTree().getItems();
        assertEquals(p1, items[0].getData());
        items = items[0].getItems();
        assertEquals(f1, items[0].getData());
    }
}

