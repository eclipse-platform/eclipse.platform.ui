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
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ConfigureRefreshScheduleDialog;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.subscribers.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkingSet;

/**
 * Provides the actions to be associated with a synchronize page
 */
public final class SubscriberActionContribution extends SynchronizePageActionGroup {
	
	// the changes viewer are contributed via the viewer and not the page.
	private Action configureSchedule;
	private SyncViewerShowPreferencesAction showPreferences;
	private Action refreshAllAction;
	private Action refreshSelectionAction;

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	public void initialize(final ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		final SubscriberParticipant participant = (SubscriberParticipant)configuration.getParticipant();
		final ISynchronizePageSite site = configuration.getSite();
		// toolbar
		if(participant.doesSupportSynchronize()) {
			refreshAllAction = new Action() {
				public void run() {
					// Prime the refresh wizard with an appropriate initial selection
					final SubscriberRefreshWizard wizard = new SubscriberRefreshWizard(participant);
					IWorkingSet set = (IWorkingSet)configuration.getProperty(ISynchronizePageConfiguration.P_WORKING_SET);
					if(set != null) {
						int scopeHint = SubscriberRefreshWizard.SCOPE_WORKING_SET;
						wizard.setScopeHint(scopeHint);
					}					
					WizardDialog dialog = new WizardDialog(site.getShell(), wizard);
					dialog.open();
				}
			};
			Utils.initAction(refreshAllAction, "action.refreshWithRemote."); //$NON-NLS-1$
			refreshSelectionAction = new Action() {
				public void run() {
					IStructuredSelection selection = (IStructuredSelection)site.getSelectionProvider().getSelection();
					IResource[] resources = Utils.getResources(selection.toArray());
					participant.refresh(resources, Policy.bind("Participant.synchronizing"), site.getWorkbenchSite()); //$NON-NLS-1$
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
			Utils.initAction(configureSchedule, "action.configureSchedulel."); //$NON-NLS-1$
		}
		
		showPreferences = new SyncViewerShowPreferencesAction(site.getShell());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager manager) {
		appendToGroup(manager, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, refreshSelectionAction);	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.IActionContribution#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		if(actionBars != null) {
			
			// toolbar
			IToolBarManager manager = actionBars.getToolBarManager();
			appendToGroup(manager, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, refreshAllAction);

			// view menu
			IMenuManager menu = actionBars.getMenuManager();
			appendToGroup(menu, ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, configureSchedule);
			appendToGroup(menu, ISynchronizePageConfiguration.PREFERENCES_GROUP, showPreferences);
		}		
	}
}
