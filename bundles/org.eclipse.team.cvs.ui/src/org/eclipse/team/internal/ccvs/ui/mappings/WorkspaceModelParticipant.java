/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.CVSActionDelegateWrapper;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.operations.MergeActionGroup;
import org.eclipse.team.ui.operations.ModelSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author MValenta
 *
 */
/**
 * @author MValenta
 *
 */
public class WorkspaceModelParticipant extends
		ModelSynchronizeParticipant {

	public static final String VIEWER_ID = "org.eclipse.team.cvs.ui.workspaceSynchronization"; //$NON-NLS-1$
	
	public static final String CONTEXT_MENU_UPDATE_GROUP_1 = "update"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_COMMIT_GROUP_1 = "commit"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_2 = "overrideActions"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_3 = "otherActions1"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_4 = "otherActions2"; //$NON-NLS-1$
	
	/**
	 * CVS workspace action contribution
	 */
	public class WorkspaceMergeActionGroup extends MergeActionGroup {
		private WorkspaceCommitAction commitToolbar;
		
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			
			int modes = configuration.getSupportedModes();
			if ((modes & (ISynchronizePageConfiguration.OUTGOING_MODE | ISynchronizePageConfiguration.BOTH_MODE)) != 0) {	
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_COMMIT_GROUP_1,
						new CommitAction(configuration));
				
				commitToolbar = new WorkspaceCommitAction(configuration);
				Utils.initAction(commitToolbar, "WorkspaceToolbarCommitAction.", Policy.getActionBundle()); //$NON-NLS-1$
				appendToGroup(
						ISynchronizePageConfiguration.P_TOOLBAR_MENU,
						MERGE_ACTION_GROUP,
						commitToolbar);
				// TODO: let's leave off overide and commit for now
//				appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
//					new OverrideAndCommitAction(configuration));
				
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CVSActionDelegateWrapper(new IgnoreAction(), configuration));
			}
			
			if (!configuration.getSite().isModal()) {
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CreatePatchAction(configuration));
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
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
						new CVSActionDelegateWrapper(new SetKeywordSubstitutionAction(), configuration));	
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.team.ui.operations.MergeActionGroup#configureMergeAction(java.lang.String, org.eclipse.jface.action.Action)
		 */
		protected void configureMergeAction(String mergeActionId, Action action) {
			if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
				Utils.initAction(action, "WorkspaceUpdateAction.", Policy.getActionBundle()); //$NON-NLS-1$
			} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
				Utils.initAction(action, "OverrideAndUpdateAction.", Policy.getActionBundle()); //$NON-NLS-1$
			} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
				Utils.initAction(action, "ConfirmMergedAction.", Policy.getActionBundle()); //$NON-NLS-1$
			} else if (mergeActionId == MERGE_ALL_ACTION_ID) {
				Utils.initAction(action, "WorkspaceToolbarUpdateAction.", Policy.getActionBundle()); //$NON-NLS-1$
			} else {
				super.configureMergeAction(mergeActionId, action);
			}
		}
		
		protected void addToContextMenu(String mergeActionId, Action action, IMenuManager manager) {
			IContributionItem group = null;;
			if (mergeActionId == SynchronizationActionProvider.MERGE_ACTION_ID) {
				group = manager.find(CONTEXT_MENU_UPDATE_GROUP_1);
			} else if (mergeActionId == SynchronizationActionProvider.OVERWRITE_ACTION_ID) {
				group = manager.find(CONTEXT_MENU_CONTRIBUTION_GROUP_2);
			} else if (mergeActionId == SynchronizationActionProvider.MARK_AS_MERGE_ACTION_ID) {
				group = manager.find(CONTEXT_MENU_CONTRIBUTION_GROUP_2);
			} else {
				super.addToContextMenu(mergeActionId, action, manager);
				return;
			}
			if (group != null) {
				manager.appendToGroup(group.getId(), action);
			} else {
				manager.add(action);
			}
		}
	}
	
	public WorkspaceModelParticipant() {
	}
	
	public WorkspaceModelParticipant(IResourceMappingScopeManager manager, ISynchronizationContext context, String name) {
		super(manager, context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.cvs.ui.workspace-participant")); //$NON-NLS-1$
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelSynchronizeParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		configuration.setProperty(ISynchronizePageConfiguration.P_VIEWER_ID, VIEWER_ID);
		super.initializeConfiguration(configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingSynchronizeParticipant#createMergeActionGroup()
	 */
	protected MergeActionGroup createMergeActionGroup() {
		return new WorkspaceMergeActionGroup();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelSynchronizeParticipant#restoreContext(org.eclipse.team.core.mapping.IResourceMappingScope, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IMergeContext restoreContext(IResourceMappingScopeManager manager) {
		return WorkspaceSubscriberContext.createContext(manager, ISynchronizationContext.THREE_WAY);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelSynchronizeParticipant#createScopeManager(org.eclipse.core.resources.mapping.ResourceMapping[])
	 */
	protected IResourceMappingScopeManager createScopeManager(ResourceMapping[] mappings) {
		return new SubscriberScopeManager(mappings, CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), true);
	}
	
}
