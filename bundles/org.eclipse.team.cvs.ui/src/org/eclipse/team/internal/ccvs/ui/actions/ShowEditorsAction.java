/*******************************************************************************
 * Copyright (c) 2003 CSC SoftwareConsult GmbH & Co. OHG, Germany and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * 	CSC - Intial implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.EditorsInfo;
import org.eclipse.team.internal.ccvs.ui.EditorsView;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
/**
 * 
 * 
 * Action for Show Editors in popup menus
 * 
 * @author <a href="mailto:kohlwes@gmx.net">Gregor Kohlwes</a>
 * 
 */
public class ShowEditorsAction extends WorkspaceAction {
	
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final EditorsAction editorsAction = new EditorsAction();
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
				executeProviderAction(editorsAction, monitor);
			}
		}, true /* cancelable */ , PROGRESS_DIALOG);
		EditorsInfo[] infos = editorsAction.getEditorsInfo();
		EditorsView view = (EditorsView)showView(EditorsView.VIEW_ID);
		if (view != null) {
			view.setInput(infos);
		}

	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		// We support one selected Resource,
		// because the editors command will
		// show the editors of all children too.
		return false;
	}

}
