/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;

/**
 * @since 3.1
 */
public class OpenCloseEditorTest extends BasicPerformanceTest {

    private String extension;

    /**
     * @param tagging
     * @param testName
     */
    public OpenCloseEditorTest(String extension, int tagging) {
        super ("testOpenAndCloseEditors:" + extension, tagging);
        this.extension = extension;    
    }
    
    
    protected void runTest() throws Throwable {
        IFile file = getProject().getFile("1." + extension);
        assertTrue(file.exists());
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
                   
        for (int i = 0; i < EditorPerformanceSuite.ITERATIONS; i++) {
            startMeasuring();
            IEditorPart part = IDE.openEditor(activePage, file, true);
            processEvents();
            activePage.closeEditor(part, false);
            processEvents();
            stopMeasuring();
        }
        tagIfNecessary("Open/Close Editor", Dimension.CPU_TIME);
        commitMeasurements();
        assertPerformance();        
    }
}
