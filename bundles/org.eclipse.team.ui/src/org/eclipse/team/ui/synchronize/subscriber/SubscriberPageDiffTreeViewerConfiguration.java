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
package org.eclipse.team.ui.synchronize.subscriber;

import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.*;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.synchronize.actions.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.viewers.*;

/**
 * Overrides the SyncInfoDiffViewerConfiguration to configure the diff viewer
 * for the synchroniza view
 */
public class SubscriberPageDiffTreeViewerConfiguration extends TreeViewerAdvisor {

	private ISynchronizeView view;
	private SubscriberParticipant participant;
	private OpenWithActionGroup openWithActions;
	private RefactorActionGroup refactorActions;
	private TeamParticipantRefreshAction refreshSelectionAction;

	public SubscriberPageDiffTreeViewerConfiguration(ISynchronizeView view, SubscriberParticipant participant) {
		super(participant.getId(), participant.getSubscriberSyncInfoCollector().getSyncInfoTree());
		this.view = view;
		this.participant = participant;
	}

	protected SubscriberParticipant getParticipant() {
		return participant;
	}

	protected void initializeActions(StructuredViewer treeViewer) {
		super.initializeActions(treeViewer);
		openWithActions = new OpenWithActionGroup(view, participant);
		refactorActions = new RefactorActionGroup(view);
		refreshSelectionAction = new TeamParticipantRefreshAction(treeViewer, participant, false /* refresh */);
		refreshSelectionAction.setWorkbenchSite(view.getSite());
	}

	protected void fillContextMenu(StructuredViewer viewer, IMenuManager manager) {
		openWithActions.fillContextMenu(manager);
		refactorActions.fillContextMenu(manager);
		manager.add(refreshSelectionAction);
		manager.add(new Separator());
		super.fillContextMenu(viewer, manager);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SyncInfoDiffTreeViewer#handleDoubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	protected void handleDoubleClick(StructuredViewer viewer, DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		DiffNode node = (DiffNode) selection.getFirstElement();
		if (node != null && node instanceof SyncInfoModelElement) {
			SyncInfoModelElement syncNode = (SyncInfoModelElement) node;
			IResource resource = syncNode.getResource();
			if (syncNode != null && resource != null && resource.getType() == IResource.FILE) {
				openWithActions.openInCompareEditor();
				return;
			}
		}
		// Double-clicking should expand/collapse containers
		super.handleDoubleClick(viewer, event);
	}

	protected void initializeListeners(StructuredViewer viewer) {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateStatusLine((IStructuredSelection) event.getSelection());
			}
		});
		viewer.addOpenListener(new IOpenListener() {

			public void open(OpenEvent event) {
				handleOpen();
			}
		});
		super.initializeListeners(viewer);
	}

	protected void handleOpen() {
		openWithActions.openInCompareEditor();
	}

	/**
	 * Updates the message shown in the status line.
	 * @param selection
	 *            the current selection
	 */
	private void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		view.getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}

	/**
	 * Returns the message to show in the status line.
	 * @param selection
	 *            the current selection
	 * @return the status line message
	 * @since 2.0
	 */
	private String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object first = selection.getFirstElement();
			if (first instanceof SyncInfoModelElement) {
				SyncInfoModelElement node = (SyncInfoModelElement) first;
				IResource resource = node.getResource();
				if (resource == null) {
					return node.getName();
				} else {
					return resource.getFullPath().makeRelative().toString();
				}
			}
		}
		if (selection.size() > 1) {
			return selection.size() + Policy.bind("SynchronizeView.13"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
}
