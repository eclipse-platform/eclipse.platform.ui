/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The ProgressContentProvider is the content provider used for classes that
 * listen to the progress changes.
 */
public abstract class ProgressContentProvider implements
        IProgressUpdateCollector, IStructuredContentProvider {

	/**
	 * Return whether or not we are filtering debug items.
	 */
    protected boolean filterDebug;

    /**
     * Create a new instance of the receiver with all of the
     * default values.  
     */
    public ProgressContentProvider() {
        this(!ProgressViewUpdater.getSingleton().debug);
    }

    /**
     * Create a new instance of the receiver with a flag to 
     * indicate if there will be debug info shown or not.
     * @param noDebug If true debug information will be shown
     * if the debug flag in the ProgressManager is true.
     */
    public ProgressContentProvider(boolean noDebug) {
        ProgressViewUpdater.getSingleton().addCollector(this);
        filterDebug = noDebug;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {

        return ProgressManager.getInstance().getRootElements(!filterDebug);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        ProgressViewUpdater.getSingleton().removeCollector(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        //No change when input changes
    }

}