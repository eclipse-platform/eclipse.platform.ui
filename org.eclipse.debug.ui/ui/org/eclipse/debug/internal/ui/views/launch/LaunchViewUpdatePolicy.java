/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DefaultUpdatePolicy;

/**
 * @since 3.2
 *
 */
public class LaunchViewUpdatePolicy extends DefaultUpdatePolicy {
	
	private LaunchView fView = null;
	
	public LaunchViewUpdatePolicy(LaunchView view) {
		fView = view;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.AbstractUpdatePolicy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		fView = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DefaultUpdatePolicy#handleState(org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer, org.eclipse.debug.internal.ui.viewers.IModelDelta)
	 */
	protected void handleState(AsynchronousTreeViewer viewer, IModelDelta delta) {
		 super.handleState(viewer, delta);
		 // only context change if not already selected
		 if ((delta.getFlags() & IModelDelta.SELECT) == 0) {
			 fView.possibleContextChange(delta.getElement());
		 }
	}

}
