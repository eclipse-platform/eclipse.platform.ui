/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * The ProgressTreeContentProvider is the content provider for trees that show
 * progress.
 */
public class ProgressTreeContentProvider extends ProgressContentProvider
        implements ITreeContentProvider {

    NewProgressViewer viewer;

    /**
     * Create an instance of the receiver on the viewer.
     * @param mainViewer
     */
    public ProgressTreeContentProvider(NewProgressViewer mainViewer) {
        super();
        viewer = mainViewer;
    }

    
    /**
     * Create a new instance of the receiver with the supplied
     * viewer and debug flag.
     * @param mainViewer
     * @param debug If true do show debug information
     * @see ProgressContentProvider
     */
    public ProgressTreeContentProvider(NewProgressViewer mainViewer,
            boolean debug) {
        super(debug);
        viewer = mainViewer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */

    public Object[] getChildren(Object parentElement) {
        return ((JobTreeElement) parentElement).getChildren();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element == this)
            return null;
          return ((JobTreeElement) element).getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element == this)
            return ProgressManager.getInstance().hasJobInfos();
       return ((JobTreeElement) element).hasChildren();
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
