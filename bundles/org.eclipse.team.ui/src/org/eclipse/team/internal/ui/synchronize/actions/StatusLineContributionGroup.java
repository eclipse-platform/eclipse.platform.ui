/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;

public class StatusLineContributionGroup extends ActionGroup implements ISyncInfoSetChangeListener {

	private static final String INCOMING_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.incoming"; //$NON-NLS-1$
	private static final String OUTGOING_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.outgoing"; //$NON-NLS-1$
	private static final String CONFLICTING_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.conflicting"; //$NON-NLS-1$
	private static final String TOTALS_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.totals"; //$NON-NLS-1$
	private final static int TEXT_FIELD_MAX_SIZE = 25;

	private StatusLineCLabelContribution incoming;
	private StatusLineCLabelContribution outgoing;
	private StatusLineCLabelContribution conflicting;
	private StatusLineCLabelContribution totalChanges;
	
	private Image incomingImage = TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DLG_SYNC_INCOMING).createImage();
	private Image outgoingImage = TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DLG_SYNC_OUTGOING).createImage();
	private Image conflictingImage = TeamUIPlugin.getImageDescriptor(ITeamUIImages.IMG_DLG_SYNC_CONFLICTING).createImage();
	
	private ISynchronizePageConfiguration configuration;

	public StatusLineContributionGroup(final Shell shell, ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
		if (isThreeWay()) {
			this.incoming = createStatusLineContribution(INCOMING_ID, ISynchronizePageConfiguration.INCOMING_MODE, "0", incomingImage); //$NON-NLS-1$
			this.outgoing = createStatusLineContribution(OUTGOING_ID, ISynchronizePageConfiguration.OUTGOING_MODE, "0", outgoingImage); //$NON-NLS-1$
			this.conflicting = createStatusLineContribution(CONFLICTING_ID, ISynchronizePageConfiguration.CONFLICTING_MODE, "0", conflictingImage); //$NON-NLS-1$
		} else {
			this.totalChanges = new StatusLineCLabelContribution(TOTALS_ID, TEXT_FIELD_MAX_SIZE);
		}
		
		// Listen to changes to update the counts
		SyncInfoSet set = getSyncInfoSet();
		set.addSyncSetChangedListener(this);
		updateCounts();
	}

	private boolean isThreeWay() {
		return configuration.getComparisonType() == ISynchronizePageConfiguration.THREE_WAY;
	}
	
	private SubscriberParticipant getParticipant() {
		return (SubscriberParticipant)configuration.getParticipant();
	}

	private StatusLineCLabelContribution createStatusLineContribution(String id, final int mode, String label, Image image) {
		StatusLineCLabelContribution item = new StatusLineCLabelContribution(id, 15);
		item.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				configuration.setMode(mode);
			}
		});
		item.setText(label); 
		item.setImage(image);
		return item;
	}

	public void dispose() {
		getSyncInfoSet().removeSyncSetChangedListener(this);
		if (isThreeWay()) {
			incomingImage.dispose();
			outgoingImage.dispose();
			conflictingImage.dispose();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.sync.sets.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.internal.ui.sync.sets.SyncSetChangedEvent)
	 */
	public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
		updateCounts();
	}

	private void updateCounts() {
		if (getParticipant().getSubscriber() != null) {
			SyncInfoSet workspaceSetStats = getSyncInfoSet();

			final int total = workspaceSetStats.size();
			final int workspaceConflicting = (int) workspaceSetStats.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK);
			final int workspaceOutgoing = (int) workspaceSetStats.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK);
			final int workspaceIncoming = (int) workspaceSetStats.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK);

			TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					if (isThreeWay()) {
						conflicting.setText(new Integer(workspaceConflicting).toString()); 
						incoming.setText(new Integer(workspaceIncoming).toString()); 
						outgoing.setText(new Integer(workspaceOutgoing).toString()); 
	
						conflicting.setTooltip(NLS.bind(TeamUIMessages.StatisticsPanel_numbersTooltip, new String[] { TeamUIMessages.StatisticsPanel_conflicting })); // 
						outgoing.setTooltip(NLS.bind(TeamUIMessages.StatisticsPanel_numbersTooltip, new String[] { TeamUIMessages.StatisticsPanel_outgoing })); // 
						incoming.setTooltip(NLS.bind(TeamUIMessages.StatisticsPanel_numbersTooltip, new String[] { TeamUIMessages.StatisticsPanel_incoming })); // 
					} else {
						if (total == 1) {
							totalChanges.setText(NLS.bind(TeamUIMessages.StatisticsPanel_numberTotalSingular, new String[] { Integer.toString(total) })); 
						} else {
							totalChanges.setText(NLS.bind(TeamUIMessages.StatisticsPanel_numberTotalPlural, new String[] { Integer.toString(total) })); 
						}
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IStatusLineManager mgr = actionBars.getStatusLineManager();
		if (isThreeWay()) {
			mgr.add(incoming);
			mgr.add(outgoing);
			mgr.add(conflicting);
		} else {
			mgr.add(totalChanges);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.subscribers.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
		updateCounts();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.ISyncInfoSetChangeListener#syncInfoSetError(org.eclipse.team.core.subscribers.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
		// Nothing to do for errors
	}
	
	private SyncInfoSet getSyncInfoSet() {
		return (SyncInfoSet)configuration.getProperty(SynchronizePageConfiguration.P_WORKING_SET_SYNC_INFO_SET);
	}
}
