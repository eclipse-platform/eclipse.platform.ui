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
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPropertyListener;

class ToggleViewAction extends Action implements IPropertyListener {
	private SynchronizeView viewer;
	
	public ToggleViewAction(SynchronizeView viewer, int initialState) {
		this.viewer = viewer;
		Utils.initAction(this, "action.toggleView."); //$NON-NLS-1$
		Action a = new Action() {
			public void run() {
				ToggleViewAction.this.setChecked(! ToggleViewAction.this.isChecked());
				ToggleViewAction.this.run();
			}
		};
		IKeyBindingService kbs = viewer.getSite().getKeyBindingService();
		Utils.registerAction(kbs, a, "org.eclipse.team.ui.syncview.toggleView");	//$NON-NLS-1$
				
		setChecked(initialState == SynchronizeView.TREE_VIEW);
		viewer.addPropertyListener(this);
	}
	
	public void run() {
		int viewerType;
		if(isChecked()) {
			viewerType = SynchronizeView.TREE_VIEW;	
		} else {
			viewerType = SynchronizeView.TABLE_VIEW;
		}
		viewer.switchViewerType(viewerType);
	}
	
	public void propertyChanged(Object source, int propId) {
		if(propId == SynchronizeView.PROP_VIEWTYPE) {
			setChecked(viewer.getCurrentViewType() == SynchronizeView.TREE_VIEW);
		}			
	}
}