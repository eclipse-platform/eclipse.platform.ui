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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.*;
import org.eclipse.team.internal.ui.synchronize.ScopableSubscriberParticipant;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

public class WorkspaceSynchronizeParticipant extends ScopableSubscriberParticipant {

	public static final String ID = "org.eclipse.team.cvs.ui.cvsworkspace-participant"; //$NON-NLS-1$

	/**
	 * The id of a workspace action group to which additions actions can 
	 * be added.
	 */
	public static final String TOOLBAR_CONTRIBUTION_GROUP = "toolbar_group_1"; //$NON-NLS-1$
	
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_1 = "context_group_1"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_2 = "context_group_2"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_3 = "context_group_3"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_4 = "context_group_4"; //$NON-NLS-1$

	/**
	 * CVS workspace action contribution
	 */
	public class WorkspaceActionContribution extends SynchronizePageActionGroup {
		private WorkspaceCommitAction commitToolbar;
		private WorkspaceUpdateAction updateToolbar;
		
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			
			updateToolbar = new WorkspaceUpdateAction(
					configuration, 
					getVisibleRootsSelectionProvider(), 
					"WorkspaceToolbarUpdateAction."); //$NON-NLS-1$
			updateToolbar.setPromptBeforeUpdate(true);
			appendToGroup(
					ISynchronizePageConfiguration.P_TOOLBAR_MENU,
					TOOLBAR_CONTRIBUTION_GROUP,
					updateToolbar);
			
			commitToolbar = new WorkspaceCommitAction(
					configuration, 
					getVisibleRootsSelectionProvider(), 
					"WorkspaceToolbarCommitAction."); //$NON-NLS-1$
			appendToGroup(
					ISynchronizePageConfiguration.P_TOOLBAR_MENU,
					TOOLBAR_CONTRIBUTION_GROUP,
					commitToolbar);
			
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					new WorkspaceUpdateAction(configuration));
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
					new WorkspaceCommitAction(configuration));
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
					new OverrideAndUpdateAction(configuration));
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
					new OverrideAndCommitAction(configuration));
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
					new ConfirmMergedAction(configuration));
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_3,
					new CVSActionDelegateWrapper(new IgnoreAction(), configuration));
			if (!configuration.getSite().isModal()) {
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CVSActionDelegateWrapper(new GenerateDiffFileAction(), configuration));
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CVSActionDelegateWrapper(new BranchAction(), configuration));
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CVSActionDelegateWrapper(new ShowAnnotationAction(), configuration));
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CVSActionDelegateWrapper(new ShowResourceInHistoryAction(), configuration));
			}
			appendToGroup(
					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
					CONTEXT_MENU_CONTRIBUTION_GROUP_4,
					new RefreshDirtyStateAction(configuration));
		}
	}
	
	/**
	 * No arg contructor used for
	 * creation of persisted participant after startup
	 */
	public WorkspaceSynchronizeParticipant() {
	}
	
	public WorkspaceSynchronizeParticipant(ISynchronizeScope scope) {
		super(scope);
		setSubscriber(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(String secondaryId, IMemento memento) throws PartInitException {
		super.init(secondaryId, memento);
		setSubscriber(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		ILabelDecorator labelDecorator = new CVSParticipantLabelDecorator(configuration);
		configuration.addLabelDecorator(labelDecorator);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, TOOLBAR_CONTRIBUTION_GROUP);
		configuration.addActionContribution(new WorkspaceActionContribution());
		configuration.setSupportedModes(ISynchronizePageConfiguration.ALL_MODES);
		configuration.setMode(ISynchronizePageConfiguration.BOTH_MODE);
		
		// The manager adds itself to the configuration in it's constructor
		new ChangeLogModelManager(configuration);
		
		// Add context menu groups here to give the client displaying the
		// page a chance to remove the context menu
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP_1);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP_2);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP_3);
		configuration.addMenuGroup(
				ISynchronizePageConfiguration.P_CONTEXT_MENU, 
				CONTEXT_MENU_CONTRIBUTION_GROUP_4);
	}
	
	protected ISynchronizeParticipantDescriptor getDescriptor() {
		return TeamUI.getSynchronizeManager().getParticipantDescriptor(ID);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SubscriberParticipant#updateLabels(org.eclipse.team.ui.synchronize.ISynchronizeModelElement, org.eclipse.compare.CompareConfiguration, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void prepareCompareInput(ISynchronizeModelElement element, CompareConfiguration config, IProgressMonitor monitor) throws TeamException {
        monitor.beginTask(null, 100);
        CVSParticipant.deriveBaseContentsFromLocal(element, Policy.subMonitorFor(monitor, 10));
        super.prepareCompareInput(element, config, Policy.subMonitorFor(monitor, 80));
        CVSParticipant.updateLabelsForCVS(element, config, Policy.subMonitorFor(monitor, 10));
        monitor.done();
    }
}
