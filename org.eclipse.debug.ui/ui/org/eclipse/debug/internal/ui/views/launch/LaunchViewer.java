/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.viewers.AbstractUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.AsynchronousModel;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.swt.widgets.Composite;

public class LaunchViewer extends AsynchronousTreeViewer {
	
	private LaunchView fView;

	public LaunchViewer(Composite parent, LaunchView view) {
		super(parent);
		fView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousViewer#isSuppressEqualSelections()
	 */
	protected boolean isSuppressEqualSelections() {
		// fire activation changes all the time
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#createUpdatePolicy()
	 */
	public AbstractUpdatePolicy createUpdatePolicy() {
		return new LaunchViewUpdatePolicy(fView);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.AsynchronousModelViewer#createModel()
     */
    protected AsynchronousModel createModel() {
        return new LaunchTreeModel(this);
    }	

}
