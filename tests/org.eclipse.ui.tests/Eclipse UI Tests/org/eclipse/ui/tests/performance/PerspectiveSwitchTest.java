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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Performance;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Test perspective switching.
 */
public class PerspectiveSwitchTest extends BasicPerformanceTest {

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
     * Test perspective switching performance. 
     */
    public void testPerspectiveSwitching() throws CoreException, IOException,
            WorkbenchException {
        // Get the two perspectives to switch between.
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry
                .findPerspectiveWithId("org.eclipse.ui.tests.dnd.dragdrop");
        final IPerspectiveDescriptor perspective2 = registry
                .findPerspectiveWithId("org.eclipse.ui.tests.fastview_perspective");

        // Open the file.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        IFile mock3File = getProject().getFile("1." + EditorPerformanceSuite.EDITOR_FILE_EXTENSIONS[0]);
        assertTrue(mock3File.exists());

        IDE.openEditor(activePage, mock3File, true);

        // Open both perspective outside the loop so as not to include
        // the initial time to open, just switching.        
        activePage.setPerspective(perspective1);
        activePage.setPerspective(perspective2);

        for (int i = 0; i < 20; i++) {
            performanceMeter.start();
            activePage.setPerspective(perspective1);
            processEvents();
            activePage.setPerspective(perspective2);
            processEvents();
            performanceMeter.stop();
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