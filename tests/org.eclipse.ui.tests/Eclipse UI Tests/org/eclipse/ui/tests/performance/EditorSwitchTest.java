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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.test.performance.Performance;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.TestPlugin;

/**
 * Test editor switching.
 */
public class EditorSwitchTest extends BasicPerformanceTest {

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
    public void testEditorSwitching() throws CoreException, IOException,
            WorkbenchException {

        /*
         * Open a workbench window on the first perspective. Make it the Java
         * perspective to force plug-in loading.
         */
//        final Display display = fWorkbench.getDisplay();
//        final IWorkbenchWindow window = fWorkbench.openWorkbenchWindow(
//                "org.eclipse.jdt.ui.JavaPerspective", null);

        IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject(UIPerformanceTestSetup.PROJECT_NAME);

        // Create a test java file.
        TestPlugin plugin = TestPlugin.getDefault();
        URL fullPathString = plugin.getDescriptor().find(
                new Path("data/PerspectiveSwitchSourceCode.txt"));
        IPath path = new Path(fullPathString.getPath());
        File file = path.toFile();
        InputStream inputStream = new FileInputStream(file);
        IFile javaFile = testProject.getFile("Util2.java");
        javaFile.create(inputStream, true, null);

        // Create a test text file.
        inputStream = new FileInputStream(file);
        IFile textFile = testProject.getFile("A.txt");
        textFile.create(inputStream, true, null);
        inputStream.close();

        // Open the file.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        IDE.openEditor(activePage, javaFile, true);

        // Switch between the two editors one hundred times.
        for (int i = 0; i < 20; i++) {
            performanceMeter.start();
            IDE.openEditor(activePage, textFile, true);
            processEvents();
            IDE.openEditor(activePage, javaFile, true);
            performanceMeter.stop();
            processEvents();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        performanceMeter.commit();
        Performance.getDefault().assertPerformance(performanceMeter);
   }
}