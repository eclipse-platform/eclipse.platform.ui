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
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.IgnoreAction;
import org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitOperation;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateWrapper;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;

/**
 * Adviser used to add toolbar buttons to the last page of the sharing wizard.
 */
public class SharingWizardTreeAdviser extends TreeViewerAdvisor implements ISynchronizeModelChangeListener {
	
	private ActionDelegateWrapper commitToolbar;
	private ActionDelegateWrapper ignoreAction;
	
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
	
	public SharingWizardTreeAdviser(String menuId, IWorkbenchPartSite site, SyncInfoTree set) {
		super(menuId, site, set);
		
		// Sync changes are used to update the action state for the update/commit buttons.
		addInputChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SynchronizeViewerAdvisor#initializeActions(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void initializeActions(StructuredViewer viewer) {
		super.initializeActions(viewer);
		
		commitToolbar = new ActionDelegateWrapper(new SharingCommitAction(), null /* view part */);
		Utils.initAction(commitToolbar, "action.SynchronizeViewCommit.", Policy.getBundle()); //$NON-NLS-1$
		
		ignoreAction = new ActionDelegateWrapper(new IgnoreAction(), null /* view part */);
		Utils.initAction(ignoreAction, "action.SharingWizardIgnore.", Policy.getBundle()); //$NON-NLS-1$
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ignoreAction.setSelection(event.getSelection());
			}
		});
		ignoreAction.setSelection(viewer.getSelection());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.viewers.StructuredViewerAdvisor#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		IToolBarManager toolbar = actionBars.getToolBarManager();
		if (toolbar != null) {
			toolbar.add(new Separator());
			toolbar.add(commitToolbar);
			toolbar.add(ignoreAction);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.AbstractSynchronizeParticipant#dispose()
	 */
	public void dispose() {
		super.dispose();
		removeInputChangedListener(this);
		CVSUIPlugin.removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.presentation.ISynchronizeModelChangeListener#inputChanged(org.eclipse.team.ui.synchronize.presentation.SynchronizeModelProvider)
	 */
	public void modelChanged(ISynchronizeModelElement root) {
		commitToolbar.setSelection(root);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ignoreAction.setSelection(getViewer().getSelection());
			}
		});
	}
}
