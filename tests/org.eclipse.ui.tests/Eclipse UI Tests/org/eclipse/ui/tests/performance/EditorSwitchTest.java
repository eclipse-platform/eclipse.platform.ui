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
import java.io.InputStream;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Test perspective switching.
 */
public class EditorSwitchTest extends UITestCase {

    /**
     * Constructor.
     * 
     * @param testName
     *            Test's name.
     */
    public EditorSwitchTest(String testName) {
        super(testName);
    }

    /**
     * Test perspective switching performance. This test always fails.
     */
    public void testPerspectiveSwitching() throws CoreException, IOException,
            WorkbenchException {

        /*
         * Open a workbench window on the first perspective. Make it the Java
         * perspective to force plug-in loading.
         */
        final Display display = fWorkbench.getDisplay();
        final IWorkbenchWindow window = fWorkbench.openWorkbenchWindow(
                "org.eclipse.jdt.ui.JavaPerspective", null);

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
        InputStream inputStream = new FileInputStream(file);
        IFile javaFile = testProject.getFile("Util.java");
        javaFile.create(inputStream, true, null);

        // Create a test text file.
        inputStream.reset();
        IFile textFile = testProject.getFile("A.txt");
        textFile.create(inputStream, true, null);
        inputStream.close();

        // Open the file.
        IDE.openEditor(window.getActivePage(), javaFile, true);

        // Switch between the two editors one hundred times.
        long elapsedTime = -System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            IDE.openEditor(window.getActivePage(), textFile, true);
            while (display.readAndDispatch())
                ; // allow repaints
            IDE.openEditor(window.getActivePage(), javaFile, true);
            while (display.readAndDispatch())
                ; // allow repaints
        }
        elapsedTime += System.currentTimeMillis();
        fail("Elapsed Time: " + elapsedTime);
    }
}
