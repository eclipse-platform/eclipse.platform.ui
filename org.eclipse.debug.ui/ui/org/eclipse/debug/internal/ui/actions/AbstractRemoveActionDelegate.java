/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import org.eclipse.debug.ui.IDebugView;
import org.eclipse.ui.IViewPart;

/**
 * Base implementation of the 'remove' action for a debug view
 * 
 * This class is intended to be extended by clients
 * @see {@link AbstractSelectionActionDelegate}
 * @see {@link org.eclipse.ui.IViewActionDelegate}
 * @see {@link org.eclipse.ui.IActionDelegate2}
 *
 */
public abstract class AbstractRemoveActionDelegate extends AbstractSelectionActionDelegate {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractSelectionActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		IDebugView debugView= (IDebugView)getView().getAdapter(IDebugView.class);
		if (debugView != null) {
			debugView.setAction(IDebugView.REMOVE_ACTION, getAction());
		}
	}
	
}
