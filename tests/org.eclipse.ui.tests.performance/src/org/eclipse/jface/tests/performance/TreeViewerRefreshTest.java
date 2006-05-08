/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;


/**
 * The TreeViewerRefreshTest is the refresh test for 
 * tree viewers.
 *
 */
public class TreeViewerRefreshTest extends ViewerTest {
    
    TreeViewer viewer;
    private RefreshTestTreeContentProvider contentProvider;

    public TreeViewerRefreshTest(String testName, int tagging) {
        super(testName, tagging);
    }

    public TreeViewerRefreshTest(String testName) {
        super(testName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.tests.performance.ViewerTest#createViewer(org.eclipse.swt.widgets.Shell)
     */
    protected StructuredViewer createViewer(Shell shell) {
        viewer = new TreeViewer(shell);
        contentProvider = new RefreshTestTreeContentProvider();
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(getLabelProvider());
        return viewer;
    }

    /**
     * Test the time for doing a refresh.
     * @throws Throwable
     */
    public void testRefresh() throws Throwable {
        openBrowser();

        for (int i = 0; i < ITERATIONS; i++) {
            startMeasuring();
            viewer.refresh();
            processEvents();
            stopMeasuring();
        }
        
        commitMeasurements();
        assertPerformance();
    }
    

}
