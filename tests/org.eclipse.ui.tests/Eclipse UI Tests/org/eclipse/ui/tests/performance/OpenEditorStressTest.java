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
import org.eclipse.test.performance.Performance;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @since 3.1
 */
public class OpenEditorStressTest extends BasicPerformanceTest {

    /**
     * @param testName
     */
    public OpenEditorStressTest(String testName) {
        super(testName);
    }
    
    
    public void measureOpenEditor(IFile file) throws PartInitException {
        assertTrue(file.exists());
        // Open both files outside the loop so as not to include
        // the initial time to open, just switching.
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();

        performanceMeter.start();
        try {
	        // Switch between the two editors one hundred times.
	        for (int i = 0; i < 100; i++) {
	            IEditorPart part = IDE.openEditor(activePage, file, true);
	            processEvents();
	            activePage.closeEditor(part, false);
	            processEvents();	            
	        }
	        performanceMeter.stop();
	        performanceMeter.commit();
	        Performance.getDefault().assertPerformance(performanceMeter);        
        }
        finally {
            performanceMeter.dispose();
        }
    }
    
    public void testMock3EditorOpening() throws PartInitException {
        measureOpenEditor(getProject().getFile(UIPerformanceTestSetup.MOCK3_FILE));
    }
    
    public void testMultiEditorOpening() throws PartInitException {
        measureOpenEditor(getProject().getFile(UIPerformanceTestSetup.MULTI_FILE));
    }
    
    public void testJavaEditorOpening() throws PartInitException {
        measureOpenEditor(getProject().getFile(UIPerformanceTestSetup.JAVA_FILE));
    }
}
