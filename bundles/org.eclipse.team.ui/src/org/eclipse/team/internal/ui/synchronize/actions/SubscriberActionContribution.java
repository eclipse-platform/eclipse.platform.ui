/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ConfigureRefreshScheduleDialog;
import org.eclipse.team.internal.ui.synchronize.SynchronizeView;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IActionBars;

/**
 * Provides the actions to be associated with a synchronize page
 */
public final class SubscriberActionContribution extends SynchronizePageActionGroup {
	
	// the changes viewer are contributed via the viewer and not the page.
	private Action configureSchedule;
	private SyncViewerShowPreferencesAction showPreferences;
	private Action refreshSelectionAction;
	private RemoveFromViewAction removeFromViewAction;

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	public void initialize(final ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		final SubscriberParticipant participant = (SubscriberParticipant)configuration.getParticipant();
		final ISynchronizePageSite site = configuration.getSite();
		// toolbar
		if(participant.doesSupportSynchronize()) {

			refreshSelectionAction = new Action() {
				public void run() {
					IStructuredSelection selection = (IStructuredSelection)site.getSelectionProvider().getSelection();
					IResource[] resources = Utils.getResources(selection.toArray());
					if (resources.length == 0) {
						// Refresh all participant resources
						resources = participant.getResources();
					}
					participant.refresh(resources, Policy.bind("Participant.synchronizing"), Policy.bind("Participant.synchronizingDetails", participant.getName()), site.getWorkbenchSite()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			};
			Utils.initAction(refreshSelectionAction, "action.refreshWithRemote."); //$NON-NLS-1$
		
			configureSchedule = new Action() {
				public void run() {
					ConfigureRefreshScheduleDialog d = new ConfigureRefreshScheduleDialog(
							site.getShell(), participant.getRefreshSchedule());
					d.setBlockOnOpen(false);
					d.open();
				}
			};
			String participantName = Utils.shortenText(SynchronizeView.MAX_NAME_LENGTH, configuration.getParticipant().getName());
			Utils.initAction(configureSchedule, "action.configureSchedulel.", new String[] {participantName}); //$NON-NLS-1$
		}
		
		showPreferences = new SyncViewerShowPreferencesAction(configuration);
		removeFromViewAction = new RemoveFromViewAction(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager manager) {
		if (findGroup(manager, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP) != null
			&& findGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP) != null) {
			// Place synchronize with navigato to save space
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, refreshSelectionAction);
			appendToGroup(manager, ISynchronizePageConfiguration.NAVIGATE_GROUP, removeFromViewAction);
		} else {
			appendToGroup(manager, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, refreshSelectionAction);
			appendToGroup(manager, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, removeFromViewAction);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		if(actionBars != null) {

			// view menu
			IMenuManager menu = actionBars.getMenuManager();
			if (findGroup(menu, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP) != null
					&& findGroup(menu, ISynchronizePageConfiguration.PREFERENCES_GROUP) != null) {
				appendToGroup(menu, ISynchronizePageConfiguration.PREFERENCES_GROUP, configureSchedule);
			} else {
				appendToGroup(menu, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, configureSchedule);
			}
			appendToGroup(menu, ISynchronizePageConfiguration.PREFERENCES_GROUP, showPreferences);
		}		
	}
}
