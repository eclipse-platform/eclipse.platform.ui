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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;

/**
 * This class acts as the superclass for all actions in the SynchronizeView
 */
public abstract class SyncViewerAction extends Action {

	private IViewPart viewPart;
	private ISelection selection;

	/**
	 * @param text
	 */
	public SyncViewerAction(IViewPart viewPart, String label) {
		super(label);
		this.viewPart = viewPart;
	}

	public Shell getShell() {
		return viewPart.getSite().getShell();
	}
}
