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
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.sets.ISyncSetChangedListener;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.internal.ui.synchronize.sets.SyncInfoStatistics;
import org.eclipse.team.internal.ui.synchronize.sets.SyncSetChangedEvent;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;

public class StatusLineContributionGroup extends ActionGroup implements ISyncSetChangedListener {
	
	private static final String INCOMING_ID = "org.eclipse.team.iu.statusline.incoming"; //$NON-NLS-1$
	private static final String OUTGOING_ID = "org.eclipse.team.iu.statusline.outgoing"; //$NON-NLS-1$
	private static final String CONFLICTING_ID = "org.eclipse.team.iu.statusline.conflicting"; //$NON-NLS-1$
	
	private StatusLineCLabelContribution incoming;
	private StatusLineCLabelContribution outgoing;
	private StatusLineCLabelContribution conflicting;
	private SubscriberInput input;
	
	public StatusLineContributionGroup(SubscriberInput input) {
		super();
		this.incoming = createStatusLineContribution(INCOMING_ID, "0", TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DLG_SYNC_INCOMING).createImage()); //$NON-NLS-1$
		this.outgoing = createStatusLineContribution(OUTGOING_ID, "0", TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DLG_SYNC_OUTGOING).createImage()); //$NON-NLS-1$
		this.conflicting = createStatusLineContribution(CONFLICTING_ID, "0", TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DLG_SYNC_CONFLICTING).createImage()); //$NON-NLS-1$
		this.input = input;
		input.registerListeners(this);
	}
	
	private StatusLineCLabelContribution createStatusLineContribution(String id, String label, Image image) {
		StatusLineCLabelContribution item = new StatusLineCLabelContribution(id, 15);
		item.setText(Policy.bind("StatisticsPanel.outgoing")); //$NON-NLS-1$
		if(image != null) {
			item.setImage(image);
			TeamUIPlugin.disposeOnShutdown(image);
		}
		return item;
	}
	
	public void dispose() {
		input.deregisterListeners(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.sets.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.internal.ui.sync.sets.SyncSetChangedEvent)
	 */
	public void syncSetChanged(SyncSetChangedEvent event) {
		if(input != null) {
			SyncInfoStatistics workspaceSetStats = input.getSubscriberSyncSet().getStatistics();
			SyncInfoStatistics workingSetSetStats = input.getWorkingSetSyncSet().getStatistics();
			
			final int workspaceConflicting = (int)workspaceSetStats.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK);
			final int workspaceOutgoing = (int)workspaceSetStats.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK);
			final int workspaceIncoming = (int)workspaceSetStats.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK);
			final int workingSetConflicting = (int)workingSetSetStats.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK);
			final int workingSetOutgoing = (int)workingSetSetStats.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK);
			final int workingSetIncoming = (int)workingSetSetStats.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK);
			
			TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					if(input.getWorkingSet() != null) {
						conflicting.setText(Policy.bind("StatisticsPanel.changeNumbers", new Integer(workingSetConflicting).toString(), new Integer(workspaceConflicting).toString())); //$NON-NLS-1$
						incoming.setText(Policy.bind("StatisticsPanel.changeNumbers", new Integer(workingSetIncoming).toString(), new Integer(workspaceIncoming).toString())); //$NON-NLS-1$
						outgoing.setText(Policy.bind("StatisticsPanel.changeNumbers", new Integer(workingSetOutgoing).toString(), new Integer(workspaceOutgoing).toString())); //$NON-NLS-1$
					} else {
						conflicting.setText(new Integer(workspaceConflicting).toString()); //$NON-NLS-1$
						incoming.setText(new Integer(workspaceIncoming).toString()); //$NON-NLS-1$
						outgoing.setText(new Integer(workspaceOutgoing).toString()); //$NON-NLS-1$
					}
				}
			});
			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IStatusLineManager mgr = actionBars.getStatusLineManager();
		mgr.add(incoming);
		mgr.add(outgoing);
		mgr.add(conflicting);		
	}
}