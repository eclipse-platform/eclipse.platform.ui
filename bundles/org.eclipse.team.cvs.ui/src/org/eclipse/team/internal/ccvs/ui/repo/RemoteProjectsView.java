/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.model.CVSTagElement;
import org.eclipse.team.internal.ccvs.ui.model.RemoteProjectsElement;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This view shows a list of projects stored in a repository that share the same
 * tag
 */
public class RemoteProjectsView extends RemoteViewPart {
	
	public static final String VIEW_ID = "org.eclipse.team.ccvs.ui.RemoteProjectsView"; //$NON-NLS-1$
	
	private RemoteProjectsElement root;

	/**
	 * Constructor for RemoteProjectsView.
	 * @param partName
	 */
	public RemoteProjectsView() {
		super(VIEW_ID);
	}

	protected void initializeListeners() {
		// listen for selection changes in the repo view
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(RepositoriesView.VIEW_ID, this);
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(RemoteTagsView.VIEW_ID, this);
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(RepositoriesView.VIEW_ID, this);
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(RemoteTagsView.VIEW_ID, this);
		super.dispose();
	}
	
	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Object[] selected = TeamAction.getSelectedAdaptables(selection, ICVSRepositoryLocation.class);
		if (selected.length != 0) {
			root.setRoot((ICVSRepositoryLocation)selected[0]);
		} else {
			selected = TeamAction.getSelectedAdaptables(selection, CVSTagElement.class);
			if (selected.length != 0) {
				CVSTagElement element = (CVSTagElement)selected[0];
				root.setRoot(element.getRoot());
				root.setTag(element.getTag());
			}
		};
		refreshViewer();
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.repo.RemoteViewPart#getTreeInput()
	 */
	protected Object getTreeInput() {
		root = new RemoteProjectsElement();
		return root;
	}

}
