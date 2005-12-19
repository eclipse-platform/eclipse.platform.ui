/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.operations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.mapping.*;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * Synchronize participant that obtains it's synchronization state from
 * a {@link ISynchronizationContext}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 **/
public class ModelSynchronizeParticipant extends
		AbstractSynchronizeParticipant {

	public static final String TOOLBAR_CONTRIBUTION_GROUP = "toolbar_group_1"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_1 = "context_menu_group_1"; //$NON-NLS-1$
	public static final String CONTEXT_MENU_CONTRIBUTION_GROUP_2 = "context_menu_group_2"; //$NON-NLS-1$
	
	private ISynchronizationContext context;

	/**
	 * Actions for a model participant
	 */
	private class ModelActionContribution extends SynchronizePageActionGroup {
		private MergeIncomingChangesAction updateToolbarAction;
		
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			
			ISynchronizationContext context = ((ModelSynchronizeParticipant)configuration.getParticipant()).getContext();
			if (context instanceof IMergeContext) {
				updateToolbarAction = new MergeIncomingChangesAction(configuration);
				appendToGroup(
						ISynchronizePageConfiguration.P_TOOLBAR_MENU,
						TOOLBAR_CONTRIBUTION_GROUP,
						updateToolbarAction);
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_1,
						new MergeAction(configuration, false));
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_1,
						new MergeAction(configuration, true));
				appendToGroup(
						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
						CONTEXT_MENU_CONTRIBUTION_GROUP_2,
						new MarkAsMergedAction(configuration));
			}
			
//			appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_1,
//					new WorkspaceCommitAction(configuration));
//			appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
//					new OverrideAndUpdateAction(configuration));
//			appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
//					new OverrideAndCommitAction(configuration));
//			appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_2,
//					new ConfirmMergedAction(configuration));		
//			appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_3,
//					new CVSActionDelegateWrapper(new IgnoreAction(), configuration));
//			if (!configuration.getSite().isModal()) {
//				appendToGroup(
//						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
//						new CreatePatchAction(configuration));
//				appendToGroup(
//						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
//						new CVSActionDelegateWrapper(new BranchAction(), configuration));
//				appendToGroup(
//						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
//						new CVSActionDelegateWrapper(new ShowAnnotationAction(), configuration));
//				appendToGroup(
//						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
//						new CVSActionDelegateWrapper(new ShowResourceInHistoryAction(), configuration));
//				appendToGroup(
//						ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//						CONTEXT_MENU_CONTRIBUTION_GROUP_3,
//						new CVSActionDelegateWrapper(new SetKeywordSubstitutionAction(), configuration));	
//			}
//			appendToGroup(
//					ISynchronizePageConfiguration.P_CONTEXT_MENU, 
//					CONTEXT_MENU_CONTRIBUTION_GROUP_4,
//					new RefreshDirtyStateAction(configuration));
		}
	}
	
	/**
	 * Create a participant for the given context
	 * @param context the synchronization context
	 */
	public ModelSynchronizeParticipant(ISynchronizationContext context, String name) {
		initializeContext(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.ui.synchronization_context_synchronize_participant")); //$NON-NLS-1$
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
		setName(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(
			ISynchronizePageConfiguration configuration) {
		configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] {ISynchronizePageConfiguration.NAVIGATE_GROUP, ISynchronizePageConfiguration.MODE_GROUP, TOOLBAR_CONTRIBUTION_GROUP});
		configuration.addActionContribution(new ModelActionContribution());
		configuration.setProperty(ISynchronizePageConfiguration.P_CONTEXT_MENU, new String[] { ISynchronizePageConfiguration.NAVIGATE_GROUP, CONTEXT_MENU_CONTRIBUTION_GROUP_1, CONTEXT_MENU_CONTRIBUTION_GROUP_2});
		configuration.setSupportedModes(ISynchronizePageConfiguration.INCOMING_MODE | ISynchronizePageConfiguration.CONFLICTING_MODE);
		configuration.setMode(ISynchronizePageConfiguration.INCOMING_MODE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#createPage(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	public IPageBookViewPage createPage(
			ISynchronizePageConfiguration configuration) {
		return new ModelSynchronizePage(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#run(org.eclipse.ui.IWorkbenchPart)
	 */
	public void run(IWorkbenchPart part) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#dispose()
	 */
	public void dispose() {
		context.dispose();
	}
	
	/**
	 * Set the context of this participant. This method must be invoked
	 * before a page is obtained from the participant.
	 * @param context the context for this participant
	 */
	protected void initializeContext(ISynchronizationContext context) {
		this.context = context;
	}

	/**
	 * Return the synchronization context for this participant.
	 * @return the synchronization context for this participant
	 */
	public ISynchronizationContext getContext() {
		return context;
	}
}
