/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.util.UITestCase;
import org.eclipse.ui.views.navigator.ResourceNavigator;

/**
 * Tests that object contributions are enabled and shown correctly in pop-up
 * menus depending on the state of the workbench. This test relies on the
 * <code>plugin.xml</code> file containing certain values. Please see the
 * appropriate section in that file for more information about the initial
 * set-up.
 * 
 * @since 3.0
 */
public final class ObjectContributionTest extends UITestCase {

    /**
     * Constructs a new instance of <code>ObjectContributionTest</code> with
     * the name of the test.
     * 
     * @param name
     *            The name of the test; may be <code>null</code>.
     */
    public ObjectContributionTest(final String name) {
        super(name);
    }

    /**
     * Tests whether the content-type object contribution works. This is testing
     * a use care familiar to Ant UI. The content-type scans an XML file to see
     * if its root element is <code>&lt;project&gt;</code>.
     * 
     * @throws CoreException
     *             If a problem occurs when creating the project or file, or if
     *             the project can't be opened.
     */
    public final void testObjectStateContentType() throws CoreException {
        // Create an XML file with <project> as its root element.
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject testProject = workspace.getRoot().getProject(
                "ObjectContributionTestProject");
        testProject.create(null);
        testProject.open(null);
        final IFile xmlFile = testProject.getFile("ObjectContributionTest.xml");
        final String contents = "<testObjectStateContentTypeElement></testObjectStateContentTypeElement>";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                contents.getBytes());
        xmlFile.create(inputStream, true, null);

        /*
         * Open a workbench window in the resource perspective, and select the
         * XML file in the resource navigator. Keep track of the resource
         * navigator and its selection provider.
         */
        final WorkbenchWindow window = (WorkbenchWindow) fWorkbench
                .openWorkbenchWindow("org.eclipse.ui.resourcePerspective",
                        ResourcesPlugin.getWorkspace());
        final IWorkbenchPage page = window.getActivePage();
        final ISelection selection = new StructuredSelection(xmlFile);
        ResourceNavigator navigator = null;
        ISelectionProvider navigatorSelectionProvider = null;
        if (page != null) {
            IViewReference[] views = page.getViewReferences();
            for (int i = 0; i < views.length; i++) {
                IViewReference reference = views[i];
                IViewPart viewPart = reference.getView(false);
                if (viewPart instanceof ResourceNavigator) {
                    navigator = (ResourceNavigator) viewPart;
                    navigator.selectReveal(selection);
                    navigatorSelectionProvider = navigator.getSite()
                            .getSelectionProvider();
                    break;
                }
            }
        }

        // Create a fake PopupMenuExtender so we can get some data back.
        final MenuManager fakeMenuManager = new MenuManager();
        fakeMenuManager.add(new GroupMarker(
                org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS));
        final PopupMenuExtender extender = new PopupMenuExtender(null,
                fakeMenuManager, navigatorSelectionProvider, navigator);

        /*
         * Pretend to show the pop-up menu -- looking to motivate the extender
         * to fill the menu based on the selection provider.
         * 
         * TODO This causes a big delay (in the order of a minute or more) while
         * trying to fill this menu. It seems to be loading a bunch of plug-ins,
         * and doing class loading.
         */
        extender.menuAboutToShow(fakeMenuManager);

        // Check to see if the appropriate object contribution is present.
        final IContributionItem[] items = fakeMenuManager.getItems();
        boolean found = false;
        for (int i = 0; i < items.length; i++) {
            if ("org.eclipse.ui.tests.ObjectContributionTest.testObjectStateContentType"
                    .equals(items[i].getId())) {
                found = true;
                break;
            }
        }
        assertTrue(
                "The pop-up menu for an XML file did not add object contributions based on its content.",
                found);
    }
}