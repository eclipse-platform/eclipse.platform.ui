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
import org.eclipse.swt.SWT;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.ui.IPropertyListener;

class ToggleViewAction extends Action implements IPropertyListener {
	private SynchronizeView viewer;
	private int layout;
	
	public ToggleViewAction(SynchronizeView viewer, int layout) {
		super(null, SWT.RADIO);
		this.viewer = viewer;
		this.layout = layout;
		if(layout == SynchronizeView.TABLE_VIEW) {
			Utils.initAction(this, "action.toggleViewFlat."); //$NON-NLS-1$	
		} else {
			Utils.initAction(this, "action.toggleViewHierarchical."); //$NON-NLS-1$
		}
		viewer.addPropertyListener(this);		
	}
	
	public void run() {
		viewer.switchViewerType(layout);
	}
	
	public void propertyChanged(Object source, int propId) {
		if(propId == SynchronizeView.PROP_VIEWTYPE) {
			setChecked(viewer.getCurrentViewType() == layout);
		}			
	}
}