/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;

/**
 * Test editor switching.
 */
public class EditorSwitchTest extends BasicPerformanceTest {

	private String file1;

	private String file2;

	/**
	 * Constructor.
	 * 
	 * @param testName
	 *            Test's name.
	 */
	public EditorSwitchTest(String[] pair) {
		super("testEditorSwitch:" + pair[0] + "," + pair[1]);
		file1 = "1." + pair[0];
		file2 = "2." + pair[1];
	}
    
    public EditorSwitchTest(String file1, String file2) {
        super("testEditorSwitch:" + file1 + "," + file2);
        this.file1 = file1;
        this.file2 = file2;
    }
    
	/**
	 * Test editor opening performance. This test always fails.
	 */
	protected void runTest() throws CoreException {

		// Open both files outside the loop so as not to include
		// the initial time to open, just switching.
		IWorkbenchWindow window = openTestWindow(UIPerformanceTestSetup.PERSPECTIVE1);
		final IWorkbenchPage activePage = window.getActivePage();
		final IFile file1 = getProject().getFile(this.file1);
		assertTrue(file1.exists());
		final IFile file2 = getProject().getFile(this.file2);
		assertTrue(file2.exists());
		IDE.openEditor(activePage, file1, true);
		IDE.openEditor(activePage, file2, true);
		processEvents();
        EditorTestHelper.calmDown(500, 30000, 500);
        waitForBackgroundJobs();
        
        exercise(new TestRunnable() {

            public void run() throws Exception {            
                startMeasuring();
                IDE.openEditor(activePage, file1, true);
                processEvents();
                IDE.openEditor(activePage, file2, true);
                processEvents();
                stopMeasuring();
                EditorTestHelper.calmDown(500, 30000, 100);
             } 
        });
        
		commitMeasurements();
		assertPerformance();
	}
}
