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
package org.eclipse.team.internal.ccvs.ui.wizards;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.IgnoreAction;
import org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitOperation;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateWrapper;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Adviser used to add toolbar buttons to the last page of the sharing wizard.
 */
public class SharingWizardPageActionGroup extends SynchronizePageActionGroup {
	
	private ActionDelegateWrapper commitAction;
	private ActionDelegateWrapper ignoreAction;
	public static final String ACTION_GROUP = "cvs_sharing_page_actions"; //$NON-NLS-1$
	
	/**
	 * Custom commit that includes outgoing and conflicting.
	 */
	class SharingCommitAction extends SynchronizeModelAction {
		protected FastSyncInfoFilter getSyncInfoFilter() {
			return new SyncInfoDirectionFilter(new int[] {SyncInfo.CONFLICTING, SyncInfo.OUTGOING});
		}
		protected SynchronizeModelOperation getSubscriberOperation(IWorkbenchPart part, IDiffElement[] elements) {
			return new SubscriberCommitOperation(part, elements, true /* override */);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SynchronizeViewerAdvisor#initializeActions(org.eclipse.jface.viewers.StructuredViewer)
	 */
	public void initialize(ISynchronizePageConfiguration configuration) {
		super.initialize(configuration);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, ACTION_GROUP);
		
		commitAction = new ActionDelegateWrapper(new SharingCommitAction(), null /* view part */);
		Utils.initAction(commitAction, "action.SynchronizeViewCommit.", Policy.getBundle()); //$NON-NLS-1$
		
		ignoreAction = new ActionDelegateWrapper(new IgnoreAction(), null /* view part */);
		Utils.initAction(ignoreAction, "action.SharingWizardIgnore.", Policy.getBundle()); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		IToolBarManager toolbar = actionBars.getToolBarManager();
		if (toolbar != null) {
			appendToGroup(toolbar, ACTION_GROUP, commitAction);
			appendToGroup(toolbar, ACTION_GROUP, ignoreAction);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.presentation.ISynchronizeModelChangeListener#inputChanged(org.eclipse.team.ui.synchronize.presentation.SynchronizeModelProvider)
	 */
	public void modelChanged(final ISynchronizeModelElement root) {
		commitAction.setSelection(root);
	}
}
