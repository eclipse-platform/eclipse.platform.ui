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
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.synchronize.actions.ComparePageDropDownAction;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantReference;

public class CompareView extends SynchronizeView {

	private ComparePageDropDownAction fPageDropDown;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeView#getViewName()
	 */
	protected String getViewName() {
		return Policy.bind("CompareView.1"); //$NON-NLS-1$
	}
	
	/**
	 * Create the default actions for the view. These will be shown regardless of the
	 * participant being displayed.
	 */
	protected void createActions() {
		fPageDropDown = new ComparePageDropDownAction(this);
	}

	/**
	 * Add the actions to the toolbar
	 * 
	 * @param mgr toolbar manager
	 */
	protected void configureToolBar(IToolBarManager mgr) {
		mgr.add(fPageDropDown);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SynchronizeView#select(org.eclipse.team.ui.synchronize.ISynchronizeParticipantReference)
	 */
	protected boolean select(ISynchronizeParticipantReference ref) {
		return ! ref.getDescriptor().isStatic();
	}
}