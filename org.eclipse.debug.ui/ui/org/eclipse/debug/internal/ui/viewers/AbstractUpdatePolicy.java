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
package org.eclipse.debug.internal.ui.viewers;


/**
 * An update policy updates a viewer based on deltas reported by a model proxy.
 *  
 * @since 3.2
 *
 */
public abstract class AbstractUpdatePolicy {

	private AsynchronousViewer fViewer = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.IUpdatePolicy#init(org.eclipse.debug.ui.viewers.update.IPresentation)
	 */
	public void init(AsynchronousViewer viewer) {
		fViewer = viewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.IUpdatePolicy#dispose()
	 */
	public synchronized void dispose() {
		fViewer = null;
	}

	/**
	 * Returns the viewer this policy is installed on or <code>null</code>
	 * if disposed.
	 * 
	 * @return presentation to update
	 */
	public AsynchronousViewer getViewer() {
		return fViewer;
	}
	
	protected synchronized boolean isDisposed() {
		return fViewer == null;
	}

}
