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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateWrapper;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;

public class WorkspaceSynchronizeParticipant extends CVSParticipant {

	public static final String ID = "org.eclipse.team.cvs.ui.cvsworkspace-participant"; //$NON-NLS-1$

	/**
	 * The id of a workspace action group to which additions actions can 
	 * be added.
	 */
	public static final String ACTION_GROUP = "cvs_workspace_actions"; //$NON-NLS-1$
	
	/**
	 * CVS workspace action contribution
	 */
	public class WorkspaceActionContribution extends SynchronizePageActionGroup {
		private ActionDelegateWrapper commitToolbar;
		private ActionDelegateWrapper updateToolbar;
		public void initialize(ISynchronizePageConfiguration configuration) {
			super.initialize(configuration);
			commitToolbar = new ActionDelegateWrapper(new SubscriberCommitAction(), configuration.getSite().getPart());
			WorkspaceUpdateAction action = new WorkspaceUpdateAction();
			action.setPromptBeforeUpdate(true);
			updateToolbar = new ActionDelegateWrapper(action, configuration.getSite().getPart());
			Utils.initAction(commitToolbar, "action.SynchronizeViewCommit.", Policy.getBundle()); //$NON-NLS-1$
			Utils.initAction(updateToolbar, "action.SynchronizeViewUpdate.", Policy.getBundle()); //$NON-NLS-1$
		}
		public void fillActionBars(IActionBars actionBars) {
			IToolBarManager toolbar = actionBars.getToolBarManager();
			appendToGroup(toolbar, ACTION_GROUP, updateToolbar);
			appendToGroup(toolbar, ACTION_GROUP, commitToolbar);
			super.fillActionBars(actionBars);
		}
		public void modelChanged(ISynchronizeModelElement element) {
			if (commitToolbar == null) return;
			commitToolbar.setSelection(element);
			updateToolbar.setSelection(element);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizeParticipant#init(org.eclipse.ui.IMemento)
	 */
	public void init(String secondaryId, IMemento memento) throws PartInitException {
		super.init(secondaryId, memento);
		Subscriber subscriber = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(); 
		setSubscriber(subscriber);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant#initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
	 */
	protected void initializeConfiguration(ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, ACTION_GROUP);
		configuration.addActionContribution(new WorkspaceActionContribution());
		configuration.setSupportedModes(ISynchronizePageConfiguration.ALL_MODES);
		configuration.setMode(ISynchronizePageConfiguration.BOTH_MODE);
	}
}