/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.team.ui.sync.ISynchronizeView;
import org.eclipse.ui.IPropertyListener;

class SelectAllAction extends Action implements IPropertyListener {
	private final SynchronizeView viewer;
	
	public SelectAllAction(SynchronizeView viewer) {
		this.viewer = viewer;
		viewer.addPropertyListener(this);
	}
	
	public void run() {
		viewer.selectAll();
	}
	
	public void propertyChanged(Object source, int propId) {
		if(propId == SynchronizeView.PROP_VIEWTYPE) {
			setEnabled(viewer.getCurrentViewType() == ISynchronizeView.TABLE_VIEW);
			viewer.getViewSite().getActionBars().updateActionBars();	
		}			
	}
}