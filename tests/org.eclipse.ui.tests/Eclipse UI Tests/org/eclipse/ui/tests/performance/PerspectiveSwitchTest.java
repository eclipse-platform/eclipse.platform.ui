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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Test perspective switching.
 */
public class PerspectiveSwitchTest extends UITestCase {

    private PerformanceMeter performanceMeter;
    /**
     * Constructor.
     * 
     * @param testName
     *            Test's name.
     */
    public PerspectiveSwitchTest(String testName) {
        super(testName);
    }
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
	    super.doSetUp();
		Performance performance = Performance.getDefault();
		performanceMeter = performance.createPerformanceMeter(performance.getDefaultScenarioId(this));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
	    super.doTearDown();
		performanceMeter.dispose();
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
        IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject(UIPerformanceTestSetup.PROJECT_NAME);

        // Create a test java file.
        TestPlugin plugin = TestPlugin.getDefault();
        URL fullPathString = plugin.getDescriptor().find(
                new Path("data/PerspectiveSwitchSourceCode.txt"));
        IPath path = new Path(fullPathString.getPath());
        File file = path.toFile();
        FileInputStream inputStream = new FileInputStream(file);
        IFile javaFile = testProject.getFile("Util1.java");
        javaFile.create(inputStream, true, null);	    

        // Open the file.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        IDE.openEditor(activePage, javaFile, true);

        for (int i = 0; i < 20; i++) {
            performanceMeter.start();
            activePage.setPerspective(resourcePerspective);
            processEvents();
            activePage.setPerspective(javaPerspective);
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