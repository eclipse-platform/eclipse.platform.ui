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

    private String id1;
    private String id2;
    private String activeEditor;

    /**
     * Constructor.
     * 
     * @param id
     */
    public PerspectiveSwitchTest(String [] ids) {
        super("testPerspectiveSwitch:" + ids[0] + "," + ids[1] + ",editor " + ids[2]);
        this.id1 = ids[0];
        this.id2 = ids[1];
        this.activeEditor = ids[2];
    }
	
    /**
     * Test perspective switching performance. 
     */
    protected void runTest() throws CoreException, WorkbenchException {
        // Get the two perspectives to switch between.
        final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
                .getPerspectiveRegistry();
        final IPerspectiveDescriptor perspective1 = registry
                .findPerspectiveWithId(id1);
        final IPerspectiveDescriptor perspective2 = registry
                .findPerspectiveWithId(id2);

        // Don't fail if we reference an unknown perspective ID. This can be
        // a normal occurrance since the test suites reference JDT perspectives, which
        // might not exist. Just skip the test.
        if (perspective1 == null) {
            System.out.println("Unknown perspective ID: " + id1);
            return;
        }

        if (perspective2 == null) {
            System.out.println("Unknown perspective ID: " + id2);
            return;
        }
        
        // Open a file.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        //IFile aFile = getProject().getFile("1." + EditorPerformanceSuite.EDITOR_FILE_EXTENSIONS[0]);
        IFile aFile = getProject().getFile(activeEditor);
        assertTrue(aFile.exists());

        IDE.openEditor(activePage, aFile, true);

        // Open both perspective outside the loop so as not to include
        // the initial time to open, just switching.        
        activePage.setPerspective(perspective1);
        activePage.resetPerspective();
        activePage.setPerspective(perspective2);
        activePage.resetPerspective();

        for (int i = 0; i < 20; i++) {
            processEvents();
            
            performanceMeter.start();
            activePage.setPerspective(perspective1);
            processEvents();
            activePage.setPerspective(perspective2);
            processEvents();
            performanceMeter.stop();
        }
        performanceMeter.commit();
        Performance.getDefault().assertPerformance(performanceMeter);
    }
}