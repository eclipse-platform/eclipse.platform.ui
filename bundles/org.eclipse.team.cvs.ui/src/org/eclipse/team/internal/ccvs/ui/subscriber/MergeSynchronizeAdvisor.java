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
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateManager;
import org.eclipse.team.internal.ui.synchronize.ActionDelegateManager.WrappedActionDelegate;
import org.eclipse.team.internal.ui.synchronize.actions.RemoveSynchronizeParticipantAction;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscribers.DirectionFilterActionGroup;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant;
import org.eclipse.ui.IActionBars;


public class MergeSynchronizeAdvisor extends CVSSynchronizeViewerAdvisor {

	private RemoveSynchronizeParticipantAction removeAction;
	private DirectionFilterActionGroup modes;
	private WrappedActionDelegate updateAdapter;
	
	public MergeSynchronizeAdvisor(ISynchronizeView view, SubscriberParticipant participant) {
		super(view, participant);		
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SynchronizeViewerAdvisor#initializeActions(org.eclipse.jface.viewers.StructuredViewer)
	 */
	protected void initializeActions(StructuredViewer treeViewer) {
		super.initializeActions(treeViewer);
		
		removeAction = new RemoveSynchronizeParticipantAction(getParticipant());
		modes = new DirectionFilterActionGroup(getParticipant(), SubscriberParticipant.INCOMING_MODE | SubscriberParticipant.CONFLICTING_MODE);
		MergeUpdateAction action = new MergeUpdateAction();
		action.setPromptBeforeUpdate(true);
		updateAdapter = new ActionDelegateManager.WrappedActionDelegate(action, getSynchronizeView(), treeViewer);
		getDelegateManager().addDelegate(updateAdapter);
		
		Utils.initAction(updateAdapter, "action.SynchronizeViewUpdate.", Policy.getBundle()); //$NON-NLS-1$
		getParticipant().setMode(SubscriberParticipant.INCOMING_MODE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {		
		super.setActionBars(actionBars);
		if(actionBars != null) {
			IToolBarManager toolbar = actionBars.getToolBarManager();
			if(toolbar != null) {
				toolbar.add(new Separator());
				modes.fillToolBar(toolbar);
				toolbar.add(new Separator());
				toolbar.add(updateAdapter);
				toolbar.add(removeAction);
			}
		}		
	}	
}