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
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPropertyListener;

class ToggleViewAction extends Action implements IPropertyListener {
	private SyncViewer viewer;
	
	public ToggleViewAction(SyncViewer viewer, int initialState) {
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
				
		setChecked(initialState == SyncViewer.TREE_VIEW);
		viewer.addPropertyListener(this);
	}
	
	public void run() {
		int viewerType;
		if(isChecked()) {
			viewerType = SyncViewer.TREE_VIEW;	
		} else {
			viewerType = SyncViewer.TABLE_VIEW;
		}
		viewer.switchViewerType(viewerType);
	}
	
	public void propertyChanged(Object source, int propId) {
		if(propId == SyncViewer.PROP_VIEWTYPE) {
			setChecked(viewer.getCurrentViewType() == SyncViewer.TREE_VIEW);
		}			
	}
}