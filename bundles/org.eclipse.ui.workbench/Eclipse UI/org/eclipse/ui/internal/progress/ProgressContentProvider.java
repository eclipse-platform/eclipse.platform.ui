/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * Return whether or not we check the preferences or overide.
	 */
    private boolean canShowDebug = false;

    /**
     * Create a new instance of the receiver with all of the
     * default values.  
     */
    public ProgressContentProvider() {
    	ProgressViewUpdater.getSingleton().addCollector(this);
    }

    /**
     * Create a new instance of the receiver with a flag to 
     * indicate if there will be debug info shown or not.
     * @param debug If true debug information will be shown
     * if the debug flag in the ProgressManager is true.
     */
    public ProgressContentProvider(boolean debug) {
    	this();
    	canShowDebug = debug;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {

        return ProgressManager.getInstance().getRootElements(debug());
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
    
    /**
     * Return whether or not we are debugging. Check the
     * system settings unless we are overiding them.
     * @return boolean <code>true</code> if debug
     * (system) jobs are being shown.
     */
    public boolean debug(){
    	if(!canShowDebug) {
			return false;
		}
    	return ProgressViewUpdater.getSingleton().debug;
    	
    }

}
