/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
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
    public PerspectiveSwitchTest(String [] ids, int tagging) {
        super("testPerspectiveSwitch:" + ids[0] + "," + ids[1] + ",editor " + ids[2], tagging);
        this.id1 = ids[0];
        this.id2 = ids[1];
        this.activeEditor = ids[2];
    }

    /**
     * Test perspective switching performance.
     */
    @Override
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

        // Open the two perspectives and the file, in a new window.
        // Do this outside the loop so as not to include
        // the initial time to open, just switching.
        IWorkbenchWindow window = openTestWindow(id1);
        final IWorkbenchPage page = window.getActivePage();
        assertNotNull(page);
        page.setPerspective(perspective2);

        //IFile aFile = getProject().getFile("1." + EditorPerformanceSuite.EDITOR_FILE_EXTENSIONS[0]);
        IFile aFile = getProject().getFile(activeEditor);
        assertTrue(aFile.exists());

        IDE.openEditor(page, aFile, true);

       	tagIfNecessary("UI - Perspective Switch", Dimension.ELAPSED_PROCESS);

        exercise(new TestRunnable() {
            @Override
			public void run() throws Exception {
                processEvents();

                startMeasuring();
                page.setPerspective(perspective1);
                processEvents();
                page.setPerspective(perspective2);
                processEvents();
                stopMeasuring();
            }
        });

        commitMeasurements();
        assertPerformance();
    }
}
