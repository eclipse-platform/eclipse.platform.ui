/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.presentations;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test for Bug 48589. This verifies that the editor tabs escape the "&"
 * character correctly. This creates a test project, and creates a file within
 * its whose name contains an ampersand. It then retrieves the text of the tab
 * item and checks to see that it is appropriate.
 * 
 * @since 3.1
 */
public final class Bug48589Test extends UITestCase {

    /**
     * Constructs a new instance of this test case.
     * 
     * @param testName
     *            The name of the test
     */
    public Bug48589Test(final String testName) {
        super(testName);
    }

    /**
     * Test for Bug 48589. This verifies that the editor tabs escape the "&"
     * character correctly. This creates a test project, and creates a file
     * within its whose name contains an ampersand. It then retrieves the text
     * of the tab item and checks to see that it is appropriate.
     * 
     * @throws CoreException
     *             If the project cannot be created or opened.
     */
    public void testFileNameWithAmpersand() throws CoreException {
        // Open a new test window.
        final IWorkbenchWindow window = openTestWindow();

        // Open a new project, with a text file.
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProject testProject = workspace.getRoot().getProject(
                "Bug 48589 Project");
        testProject.create(null);
        testProject.open(null);
        final String fileName = "A&B.txt";
        final IFile textFile = testProject.getFile(fileName);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                fileName.getBytes());
        textFile.create(inputStream, true, null);
        IDE.openEditor(window.getActivePage(),
                textFile, true);
        
        // Get the current title of the text editor.
        EModelService modelService = window.getService(EModelService.class);
        MArea area = modelService.findElements(((WorkbenchWindow)window).getModel() , null, MArea.class, null).get(0);
        MPartStack partStack = modelService.findElements(area, null, MPartStack.class, null).get(0);
        assertTrue(partStack.getWidget() instanceof CTabFolder);
        
        
        final CTabFolder tabFolder = (CTabFolder) partStack.getWidget();
        final CTabItem item = tabFolder.getItem(0);
        final String actualTitle = item.getText();
        
        // Verify that the title is escaped, as expected.
        final String expectedTitle = "A&&B.txt";
        assertEquals("The title should be equal to the ", expectedTitle,
                actualTitle);
    }
}
