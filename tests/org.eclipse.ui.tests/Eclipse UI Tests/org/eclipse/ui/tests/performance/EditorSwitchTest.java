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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

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
    public void testEditorSwitching() throws CoreException {

        // Open both files outside the loop so as not to include
        // the initial time to open, just switching.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        IFile mock3File = getProject().getFile(UIPerformanceTestSetup.MOCK3_FILE);
        assertTrue(mock3File.exists());
        IFile multiFile = getProject().getFile(UIPerformanceTestSetup.MULTI_FILE);
        assertTrue(multiFile.exists());
        IDE.openEditor(activePage, mock3File, true);
        IDE.openEditor(activePage, multiFile, true);
        processEvents();

        // Switch between the two editors one hundred times.
        for (int i = 0; i < 20; i++) {
            performanceMeter.start();
            IDE.openEditor(activePage, mock3File, true);
            processEvents();
            IDE.openEditor(activePage, multiFile, true);
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