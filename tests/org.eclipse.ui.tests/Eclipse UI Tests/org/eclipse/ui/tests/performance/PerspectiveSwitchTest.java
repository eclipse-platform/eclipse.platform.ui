/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Test perspective switching.
 */
public class PerspectiveSwitchTest extends UITestCase {

    /**
     * Constructor.
     * 
     * @param testName
     *            Test's name.
     */
    public PerspectiveSwitchTest(String testName) {
        super(testName);
    }

    /**
     * Test perspective switching performance. This test always fails.
     */
    public void testPerspectiveSwitching() throws CoreException, IOException,
            WorkbenchException {
        // Get the two perspectives to switch between.
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        final IPerspectiveDescriptor javaPerspective = registry
                .findPerspectiveWithId("org.eclipse.jdt.ui.JavaPerspective");
        final IPerspectiveDescriptor resourcePerspective = registry
                .findPerspectiveWithId("org.eclipse.ui.resourcePerspective");

        /*
         * Open a workbench window on the first perspective. Make it the Java
         * perspective to force plug-in loading.
         */
        final Display display = fWorkbench.getDisplay();
        final IWorkbenchWindow window = fWorkbench.openWorkbenchWindow(
                javaPerspective.getId(), null);

        // Create a java project.
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject testProject = workspace.getRoot().getProject(
                "PerspectiveSwitchTest Project");
        testProject.create(null);
        testProject.open(null);
        IProjectDescription projectDescription = testProject.getDescription();
        String[] natureIds = { "org.eclipse.jdt.core.javanature" };
        projectDescription.setNatureIds(natureIds);
        ICommand buildCommand = new BuildCommand();
        buildCommand.setBuilderName("org.eclipse.jdt.core.javabuilder");
        projectDescription.setBuildSpec(new ICommand[] { buildCommand });
        testProject.setDescription(projectDescription, null);

        // Create a test java file.
        TestPlugin plugin = TestPlugin.getDefault();
        URL fullPathString = plugin.getDescriptor().find(
                new Path("data/PerspectiveSwitchSourceCode.txt"));
        IPath path = new Path(fullPathString.getPath());
        File file = path.toFile();
        FileInputStream inputStream = new FileInputStream(file);
        IFile javaFile = testProject.getFile("Util.java");
        javaFile.create(inputStream, true, null);

        // Open the file.
        IDE.openEditor(window.getActivePage(), javaFile, true);

        // Switch between the two perspectives one hundred times.
        long elapsedTime = -System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            window.getActivePage().setPerspective(resourcePerspective);
            while (display.readAndDispatch())
                ; // allow repaints
            window.getActivePage().setPerspective(javaPerspective);
            while (display.readAndDispatch())
                ; // allow repaints
        }
        elapsedTime += System.currentTimeMillis();
        fail("Elapsed Time: " + elapsedTime);
    }
}