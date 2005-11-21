/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.selection;


import org.eclipse.debug.ui.IDebugView;
import org.eclipse.ui.IViewPart;

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
