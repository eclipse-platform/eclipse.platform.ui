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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.core.mapping.provider.ScopeGenerator;
import org.eclipse.team.core.subscribers.SubscriberResourceMappingContext;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.CVSActionDelegateWrapper;
import org.eclipse.team.internal.core.mapping.CompoundResourceTraversal;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.operations.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PartInitException;

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
	
	public WorkspaceModelParticipant(ISynchronizationContext context, String name) {
		super(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.cvs.ui.workspace-participant")); //$NON-NLS-1$
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
	}
	
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
	
	protected void initializeContext(ResourceTraversal[] traversals) throws PartInitException {
		CompoundResourceTraversal traversal = new CompoundResourceTraversal();
		traversal.addTraversals(traversals);
		try {
			// When restoring, we can't do anything long running so we'll need to use the local content for everything
			NullProgressMonitor monitor = new NullProgressMonitor();
			ResourceMapping[] mappings = ScopeGenerator.getMappingsFromProviders(traversal.getRoots(), ResourceMappingContext.LOCAL_CONTEXT, monitor);
			IResourceMappingScope scope = createScope(mappings, monitor);
			IMergeContext context = WorkspaceSubscriberContext.createContext(scope, false /* refresh */, ISynchronizationContext.THREE_WAY, monitor);
			initializeContext(context);
		} catch (CoreException e) {
			CVSUIPlugin.log(e);
			throw new PartInitException(e.getStatus());
		}
	}

	private IResourceMappingScope createScope(ResourceMapping[] mappings, IProgressMonitor monitor) throws PartInitException {
		ModelOperation op = new ModelOperation(null, mappings) {
			protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				// Do nothing, we just want to build the scope
			}
			protected void promptIfInputChange(IProgressMonitor monitor) {
				// Don't prompt
			}
			protected boolean isUseLocalContext() {
				return true;
			}
			protected ResourceMappingContext getResourceMappingContext() {
				return new SubscriberResourceMappingContext(CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), true);
			}
		};
		// Run the operatin in order to build the scope
		try {
			op.run(monitor);
		} catch (InvocationTargetException e) {
			CVSUIPlugin.log(IStatus.ERROR, "An error occurred", e.getTargetException());
			throw new PartInitException("Unexpected error during participant restore");
		} catch (InterruptedException e) {
			throw new PartInitException("Unexpected interrupt during participant restore");
		}
		return op.getScope();
	}
}
