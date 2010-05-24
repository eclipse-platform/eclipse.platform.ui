/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.*;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

public class ShowResourceInHistoryAction extends WorkspaceAction {
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				IResource[] resources = getSelectedResources();
				if (resources.length != 1) return;
				TeamUI.showHistoryFor(TeamUIPlugin.getActivePage(), resources[0], null);
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return CVSUIMessages.ShowHistoryAction_showHistory; 
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return (!cvsResource.isFolder() && super.isEnabledForCVSResource(cvsResource));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getId()
	 */
	public String getId() {
		return ICVSUIConstants.CMD_HISTORY;
	}	
	
	protected boolean isEnabledForUnmanagedResources() {
		return true;
	}
	
	protected boolean isEnabledForIgnoredResources() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	public void setEnabled(Object evaluationContext) {
		IWorkbenchWindow activeWorkbenchWindow = (IWorkbenchWindow) HandlerUtil
				.getVariable(evaluationContext,
						ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
		if (activeWorkbenchWindow != null) {
			ISelection selection = (ISelection) HandlerUtil.getVariable(
					evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
			if (selection == null) {
				selection = StructuredSelection.EMPTY;
			}
			IWorkbenchPart part = (IWorkbenchPart) HandlerUtil.getVariable(
					evaluationContext, ISources.ACTIVE_PART_NAME);
			updateSelection(activeWorkbenchWindow, part, selection);
		}
	}
	
	private void updateSelection(IWorkbenchWindow activeWorkbenchWindow,
			IWorkbenchPart part, ISelection selection) {
		// If the action is run from within an editor, try and find the
		// file for the given editor.
		if (part != null && part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				selectionChanged((IAction) null, new StructuredSelection(file));
			}
		} else {
			// Fallback is to prime the action with the selection
			selectionChanged((IAction) null, selection);
		}
	}
}
