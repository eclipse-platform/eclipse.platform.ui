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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoFilter;
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.synchronize.actions.RemoveSynchronizeParticipantAction;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant;
import org.eclipse.ui.IActionBars;

public class CompareParticipant extends SubscriberParticipant {
	
	private SyncInfoFilter contentComparison = new SyncInfoFilter() {
		private SyncInfoFilter contentCompare = new SyncInfoFilter.ContentComparisonSyncInfoFilter();
		public boolean select(SyncInfo info, IProgressMonitor monitor) {
			// Want to select infos whose contents do not match
			return !contentCompare.select(info, monitor);
		}
	};
	
	private class CompareParticipantAdvisor extends CVSSynchronizeViewerAdvisor {
		private RemoveSynchronizeParticipantAction removeAction;
		
		public CompareParticipantAdvisor(ISynchronizeView view, SubscriberParticipant participant) {
			super(view, participant);
		}
		
		protected void initializeActions(StructuredViewer treeViewer) {
			super.initializeActions(treeViewer);
			removeAction = new RemoveSynchronizeParticipantAction(getParticipant());
		}
		
		public void setActionBars(IActionBars actionBars) {
			super.setActionBars(actionBars);
			if(actionBars != null) {
				IToolBarManager toolbar = actionBars.getToolBarManager();
				if(toolbar != null) {
					toolbar.add(new Separator());
					toolbar.add(removeAction);
				}
			}		
		}
	}
	
	public CompareParticipant(CVSCompareSubscriber subscriber) {
		super();
		setMode(BOTH_MODE);
		setSubscriber(subscriber);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#setSubscriber(org.eclipse.team.core.subscribers.Subscriber)
	 */
	protected void setSubscriber(Subscriber subscriber) {
		super.setSubscriber(subscriber);
		try {
			ISynchronizeParticipantDescriptor descriptor = TeamUI.getSynchronizeManager().getParticipantDescriptor(CVSCompareSubscriber.ID);
			setInitializationData(descriptor);
		} catch (CoreException e) {
			CVSUIPlugin.log(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#getName()
	 */
	public String getName() {
		return getSubscriber().getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#updateMode(int)
	 */
	protected void updateMode(int mode) {
		// Don't allow modes to be used with this participant
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#preCollectingChanges()
	 */
	protected void preCollectingChanges() {
		super.preCollectingChanges();
		getSubscriberSyncInfoCollector().setFilter(contentComparison);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#isPersistent()
	 */
	public boolean isPersistent() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant#createSynchronizeViewerAdvisor(org.eclipse.team.ui.synchronize.ISynchronizeView)
	 */
	protected StructuredViewerAdvisor createSynchronizeViewerAdvisor(ISynchronizeView view) {
		return new CompareParticipantAdvisor(view, this);
	}
	
	/**
	 * Refresh this participant and show the results in a model dialog.
	 * @param resources
	 */
	public void refresh(IResource[] resources) {
		refresh(resources, getRefreshListenerFactory().createModalDialogListener(getId(), this, getSubscriberSyncInfoCollector().getSyncInfoTree()), getName(), null);
	}
}
