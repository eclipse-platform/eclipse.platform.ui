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
package org.eclipse.ui.internal.progress;


/**
 * The ProgressTreeContentProvider is the content provider for trees that show
 * progress.
 */
public class JobsProgressViewerContentProvider extends ProgressContentProvider
       {

    JobsProgressViewer viewer;

    /**
     * Create an instance of the receiver on the viewer.
     * @param mainViewer
     */
    public JobsProgressViewerContentProvider(JobsProgressViewer mainViewer) {
        super();
        viewer = mainViewer;
    }

    /**
     * Create a new instance of the receiver with the supplied
     * viewer and debug flag.
     * @param mainViewer
     * @param noDebug If true do not show debug information
     */
    public JobsProgressViewerContentProvider(JobsProgressViewer mainViewer,
            boolean noDebug) {
        super(noDebug);
        viewer = mainViewer;
    }

 
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#add(org.eclipse.ui.internal.progress.JobTreeElement[])
     */
    public void add(Object[] elements) {
        viewer.add(viewer.getInput(), elements);

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh()
     */
    public void refresh() {
        viewer.refresh(true);

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh(org.eclipse.ui.internal.progress.JobTreeElement[])
     */
    public void refresh(Object[] elements) {
        for (int i = 0; i < elements.length; i++) {
            viewer.refresh(elements[i], true);
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#remove(org.eclipse.ui.internal.progress.JobTreeElement[])
     */
    public void remove(Object[] elements) {
        viewer.remove(elements);

    }

}