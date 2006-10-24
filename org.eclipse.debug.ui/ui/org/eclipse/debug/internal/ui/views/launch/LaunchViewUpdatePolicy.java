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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.contexts.provisional.DebugContextEvent;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.internal.ui.viewers.TreeUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta;

/**
 * @since 3.2
 *
 */
public class LaunchViewUpdatePolicy extends TreeUpdatePolicy {
	
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
	 * @see org.eclipse.debug.internal.ui.viewers.update.DefaultUpdatePolicy#handleState(org.eclipse.debug.internal.ui.viewers.AsynchronousTreeModelViewer, org.eclipse.debug.internal.ui.viewers.IModelDelta)
	 */
	protected void handleState(AsynchronousTreeViewer viewer, IModelDelta delta) {
		 super.handleState(viewer, delta);
		 if ((delta.getFlags() & (IModelDelta.CONTENT | IModelDelta.SELECT)) == 0) {
			 // a state change without content or selection is a possible context change
			 fView.possibleContextChange(delta.getElement(), DebugContextEvent.CHANGED);
		 }
	}

	protected void handleContent(AsynchronousTreeViewer viewer, IModelDelta delta) {
		super.handleContent(viewer, delta);
		 if ((delta.getFlags() & IModelDelta.SELECT) == 0) {
			 // a content change without select or selection is a possible activation
			 fView.possibleContextChange(delta.getElement(), DebugContextEvent.ACTIVATED);
		 }
	}
	
	

}
