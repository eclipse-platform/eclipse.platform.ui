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

import java.util.HashSet;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredContentProvider;

/**
 * The ProgressViewerContentProvider is the content provider progress
 * viewers.
 */
public class ProgressViewerContentProvider extends ProgressContentProvider
         {
    ProgressViewer progressViewer;

    /**
     * Create a new instance of the receiver.
     * @param structured The Viewer we are providing content for
     */
    public ProgressViewerContentProvider(ProgressViewer structured) {
        super();
        progressViewer = structured;
    }

    /**
     * Create a new instance of the receiver.
     * @param structured The Viewer we are providing content for
     * @param noDebug A flag to indicate if the debug flag is false.
     */
    public ProgressViewerContentProvider(ProgressViewer structured,
            boolean noDebug) {
        super(noDebug);
        progressViewer = structured;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#add(org.eclipse.ui.internal.progress.JobTreeElement[])
     */
    public void add(Object[] elements) {
        progressViewer.setInput(this);
    }

    /**
     * Return only the elements that we want to display.
     * 
     * @param elements
     *            the array of elements.
     * @return the elements that we want to display.
     */
    public Object[] getDisplayedValues(Object[] elements) {
        HashSet showing = new HashSet();

        for (int i = 0; i < elements.length; i++) {
            JobTreeElement element = (JobTreeElement) elements[i];
            if (element.isActive()) {
                if (element.isJobInfo()
                        && ((JobInfo) element).getJob().getState() != Job.RUNNING)
                    continue;
                showing.add(element);
            }
        }

        return showing.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh()
     */
    public void refresh() {
        progressViewer.refresh(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh(org.eclipse.ui.internal.progress.JobTreeElement[])
     */
    public void refresh(Object[] elements) {
        Object[] refreshes = getRoots(elements, true);
        for (int i = 0; i < refreshes.length; i++) {
            progressViewer.refresh(refreshes[i], true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#remove(org.eclipse.ui.internal.progress.JobTreeElement[])
     */
    public void remove(Object[] elements) {
        progressViewer.setInput(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        JobTreeElement[] elements = ProgressManager.getInstance()
                .getRootElements(!filterDebug);
        return getDisplayedValues(elements);
    }

    /**
     * Get the root elements of the passed elements as we only show roots.
     * Replace the element with its parent if subWithParent is true
     * 
     * @param elements
     *            the array of elements.
     * @param subWithParent
     *            sub with parent flag.
     * @return
     */
    private Object[] getRoots(Object[] elements, boolean subWithParent) {
        if (elements.length == 0)
            return elements;
        HashSet roots = new HashSet();
        for (int i = 0; i < elements.length; i++) {
            JobTreeElement element = (JobTreeElement) elements[i];
            if (element.isJobInfo()) {
                GroupInfo group = ((JobInfo) element).getGroupInfo();
                if (group == null)
                    roots.add(element);
                else {
                    if (subWithParent)
                        roots.add(group);
                }
            } else
                roots.add(element);
        }
        return roots.toArray();
    }

}