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
import org.eclipse.ui.ide.IDE;

/**
 * @since 3.1
 */
public class OpenMultipleEditorTest extends BasicPerformanceTest {

    private String extension;
    private boolean closeAll;

    /**
     * @param testName
     */
    public OpenMultipleEditorTest(String extension, boolean closeAll) {
        super ("testOpenMultipleEditors:" + extension + (closeAll ? "[closeAll]" : "[closeEach]"));
        this.extension = extension;        
        this.closeAll = closeAll;
    }
    
    protected void runTest() throws Throwable {
        IWorkbenchPage activePage = fWorkbench.getActiveWorkbenchWindow().getActivePage();
        
        performanceMeter.start();
        try {            
	        for (int i = 0; i < EditorPerformanceSuite.ITERATIONS; i++) {
	            IFile file = getProject().getFile(i + "." + extension);
	            IEditorPart part = IDE.openEditor(activePage, file, true);
	            processEvents();
	        }
	        if (closeAll) {
	            activePage.closeAllEditors(false);
	        }
	        else {
	            IEditorPart [] parts = activePage.getEditors();
	            for (int i = 0; i < parts.length; i++) {
                    activePage.closeEditor(parts[i], false);
                }
	        }
	        performanceMeter.stop();
	        performanceMeter.commit();
	        Performance.getDefault().assertPerformance(performanceMeter);        
        }
        finally {
            performanceMeter.dispose();
        }        
    }

}
