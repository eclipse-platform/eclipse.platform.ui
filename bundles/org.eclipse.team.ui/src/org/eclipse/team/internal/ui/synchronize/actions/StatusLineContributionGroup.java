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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.sets.*;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.synchronize.TeamSubscriberParticipant;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionGroup;

public class StatusLineContributionGroup extends ActionGroup implements ISyncSetChangedListener {

	private static final String INCOMING_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.incoming"; //$NON-NLS-1$
	private static final String OUTGOING_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.outgoing"; //$NON-NLS-1$
	private static final String CONFLICTING_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.conflicting"; //$NON-NLS-1$
	private static final String WORKINGSET_ID = TeamUIPlugin.ID + "org.eclipse.team.iu.statusline.workingset"; //$NON-NLS-1$
	private final static int WORKING_SET_FIELD_SIZE = 25;

	private StatusLineCLabelContribution incoming;
	private StatusLineCLabelContribution outgoing;
	private StatusLineCLabelContribution conflicting;
	private StatusLineCLabelContribution workingSet;

	private SubscriberInput input;
	private TeamSubscriberParticipant participant;

	public StatusLineContributionGroup(final Shell shell, final WorkingSetFilterActionGroup setGroup, TeamSubscriberParticipant participant) {
		super();
		this.participant = participant;
		this.input = participant.getInput();
		this.incoming = createStatusLineContribution(INCOMING_ID, TeamSubscriberParticipant.INCOMING_MODE, "0", TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DLG_SYNC_INCOMING).createImage()); //$NON-NLS-1$
		this.outgoing = createStatusLineContribution(OUTGOING_ID, TeamSubscriberParticipant.OUTGOING_MODE, "0", TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DLG_SYNC_OUTGOING).createImage()); //$NON-NLS-1$
		this.conflicting = createStatusLineContribution(CONFLICTING_ID, TeamSubscriberParticipant.CONFLICTING_MODE, "0", TeamUIPlugin.getImageDescriptor(ISharedImages.IMG_DLG_SYNC_CONFLICTING).createImage()); //$NON-NLS-1$		
		this.workingSet = new StatusLineCLabelContribution(WORKINGSET_ID, 25);
		this.workingSet.setTooltip(Policy.bind("StatisticsPanel.workingSetTooltip"));
		updateWorkingSetText();

		this.workingSet.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				new SelectWorkingSetAction(setGroup, shell).run();
			}
		});

		input.registerListeners(this);
		participant.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_WORKINGSET)) {
					updateWorkingSetText();
				}
			}
		});
	}

	private void updateWorkingSetText() {
		IWorkingSet set = participant.getWorkingSet();
		if (set == null) {
			workingSet.setText(Policy.bind("StatisticsPanel.noWorkingSet"));
		} else {
			String name = set.getName();
			if (name.length() > WORKING_SET_FIELD_SIZE) {
				name = name.substring(0, WORKING_SET_FIELD_SIZE - 3) + "...";
			}
			workingSet.setText(name);
		}
	}

	private StatusLineCLabelContribution createStatusLineContribution(String id, final int mode, String label, Image image) {
		StatusLineCLabelContribution item = new StatusLineCLabelContribution(id, 15);
		item.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				participant.setMode(mode);
			}
		});
		item.setText(label); //$NON-NLS-1$
		if (image != null) {
			item.setImage(image);
			TeamUIPlugin.disposeOnShutdown(image);
		}
		return item;
	}

	public void dispose() {
		input.deregisterListeners(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.internal.ui.sync.sets.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.internal.ui.sync.sets.SyncSetChangedEvent)
	 */
	public void syncSetChanged(SyncSetChangedEvent event) {
		if (input != null) {
			SyncInfoStatistics workspaceSetStats = input.getSubscriberSyncSet().getStatistics();
			SyncInfoStatistics workingSetSetStats = input.getWorkingSetSyncSet().getStatistics();

			final int workspaceConflicting = (int) workspaceSetStats.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK);
			final int workspaceOutgoing = (int) workspaceSetStats.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK);
			final int workspaceIncoming = (int) workspaceSetStats.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK);
			final int workingSetConflicting = (int) workingSetSetStats.countFor(SyncInfo.CONFLICTING, SyncInfo.DIRECTION_MASK);
			final int workingSetOutgoing = (int) workingSetSetStats.countFor(SyncInfo.OUTGOING, SyncInfo.DIRECTION_MASK);
			final int workingSetIncoming = (int) workingSetSetStats.countFor(SyncInfo.INCOMING, SyncInfo.DIRECTION_MASK);

			TeamUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkingSet set = input.getWorkingSet();
					if (set != null) {
						conflicting.setText(Policy.bind("StatisticsPanel.changeNumbers", new Integer(workingSetConflicting).toString(), new Integer(workspaceConflicting).toString())); //$NON-NLS-1$
						incoming.setText(Policy.bind("StatisticsPanel.changeNumbers", new Integer(workingSetIncoming).toString(), new Integer(workspaceIncoming).toString())); //$NON-NLS-1$
						outgoing.setText(Policy.bind("StatisticsPanel.changeNumbers", new Integer(workingSetOutgoing).toString(), new Integer(workspaceOutgoing).toString())); //$NON-NLS-1$

						conflicting.setTooltip(Policy.bind("StatisticsPanel.numbersWorkingSetTooltip", Policy.bind("StatisticsPanel.conflicting"), set.getName()));
						outgoing.setTooltip(Policy.bind("StatisticsPanel.numbersWorkingSetTooltip", Policy.bind("StatisticsPanel.outgoing"), set.getName()));
						incoming.setTooltip(Policy.bind("StatisticsPanel.numbersWorkingSetTooltip", Policy.bind("StatisticsPanel.incoming"), set.getName()));

					} else {
						conflicting.setText(new Integer(workspaceConflicting).toString()); //$NON-NLS-1$
						incoming.setText(new Integer(workspaceIncoming).toString()); //$NON-NLS-1$
						outgoing.setText(new Integer(workspaceOutgoing).toString()); //$NON-NLS-1$

						conflicting.setTooltip(Policy.bind("StatisticsPanel.numbersTooltip", Policy.bind("StatisticsPanel.conflicting")));
						outgoing.setTooltip(Policy.bind("StatisticsPanel.numbersTooltip", Policy.bind("StatisticsPanel.outgoing")));
						incoming.setTooltip(Policy.bind("StatisticsPanel.numbersTooltip", Policy.bind("StatisticsPanel.incoming")));
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
		mgr.add(workingSet);
		mgr.add(incoming);
		mgr.add(outgoing);
		mgr.add(conflicting);
	}
}