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
package org.eclipse.team.internal.ui.sync.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.sets.SubscriberInput;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionContext;

/**
 * This ActionGroup provides filtering of a sync set by change direction.
 * The actions are presented to the user as toolbar buttons where only one
 * button is active at a time
 */
public class SyncViewerDirectionFilters extends SyncViewerActionGroup {

	private static final String MEMENTO_KEY = "SyncViewerDirectionFilters"; //$NON-NLS-1$
	
	// An array of the selection actions for the modes (indexed by mode constant)	
	private List actions = new ArrayList(3);
	private SyncViewerActions refreshGroup;
	
	private DirectionFilterAction incomingMode;					
	private DirectionFilterAction outgoingMode;
	private DirectionFilterAction bothMode;
	private DirectionFilterAction conflictsMode;
		
	private final static int[] DEFAULT_FILTER = new int[] {SyncInfo.INCOMING, SyncInfo.OUTGOING, SyncInfo.CONFLICTING};
	
	public final static int INCOMING_MODE = 1;
	public final static int OUTGOING_MODE = 2;
	public final static int BOTH_MODE = 3;
	public final static int CONFLICTING_MODE = 4;
			
	/**
	 * Action for toggling the sync mode.
	 */
	class DirectionFilterAction extends Action {
		// The sync mode that this action enables
		private int[] syncMode;
		private boolean toggled;
		private int modeId;
		public DirectionFilterAction(String prefix,String commandId, int[] mode, int modeId) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			this.syncMode = mode;
			this.modeId = modeId;
			Utils.initAction(this, prefix);
			Action a = new Action() {
				public void run() {
					DirectionFilterAction.this.setChecked(! DirectionFilterAction.this.isChecked());
					DirectionFilterAction.this.run();
				}
			};
			IKeyBindingService kbs = refreshGroup.getSyncView().getSite().getKeyBindingService();
			Utils.registerAction(kbs, a, commandId);	//$NON-NLS-1$
		}
		public void run() {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCVIEW_SELECTED_MODE, modeId);
			updateFilter(this);
		}
		public int[] getFilter() {
			return syncMode;
		}
	}
	
	protected SyncViewerDirectionFilters(SynchronizeView viewer, SyncViewerActions refreshGroup) {
		super(viewer);
		this.refreshGroup = refreshGroup;
		createActions();
	}
	
	/**
	 * Sets up the sync modes and the actions for switching between them.
	 */
	private void createActions() {
		// Create the actions
		incomingMode = new DirectionFilterAction("action.directionFilterIncoming.", "org.eclipse.team.ui.syncview.incomingFilter", new int[] {SyncInfo.INCOMING, SyncInfo.CONFLICTING}, INCOMING_MODE); //$NON-NLS-1$ //$NON-NLS-2$
		actions.add(incomingMode);
					
		outgoingMode = new DirectionFilterAction("action.directionFilterOutgoing.", "org.eclipse.team.ui.syncview.outgoingFilter", new int[] {SyncInfo.OUTGOING, SyncInfo.CONFLICTING}, OUTGOING_MODE); //$NON-NLS-1$ //$NON-NLS-2$
		actions.add(outgoingMode);

		bothMode = new DirectionFilterAction("action.directionFilterBoth.", "org.eclipse.team.ui.syncview.bothFilter", new int[] {SyncInfo.OUTGOING, SyncInfo.INCOMING, SyncInfo.CONFLICTING}, BOTH_MODE); //$NON-NLS-1$ //$NON-NLS-2$
		actions.add(bothMode);

		conflictsMode = new DirectionFilterAction("action.directionFilterConflicts.", "org.eclipse.team.ui.syncview.conflictsFilter", new int[] {SyncInfo.CONFLICTING}, CONFLICTING_MODE); //$NON-NLS-1$ //$NON-NLS-2$
		actions.add(conflictsMode);
		
		updateEnablement(null);
	}
	
	public int[] getDirectionFilter() {
		int[] filters = new int[actions.size()];
		int i = 0;
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction)it.next();
			if(action.isChecked()) {
				return action.getFilter();
			}
		}
		// should never happen because buttons are radio
		Assert.isTrue(true);
		return new int[0];
	}

	boolean isSet(int[] filters, int[] afilter) {
		for (int i = 0; i < filters.length; i++) {
			for (int j = 0; i < afilter.length; i++) {
				if(filters[i] == afilter[j]) return true;
			}
		}
		return false;
	}

	void updateFilter(DirectionFilterAction action) {
		getRefreshGroup().refreshFilters();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		IToolBarManager toolBar = actionBars.getToolBarManager();
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction) it.next();
			toolBar.add(action);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.actions.SyncViewerActionGroup#restore(org.eclipse.ui.IMemento)
	 */
	public void restore(IMemento memento) {
		super.restore(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.actions.SyncViewerActionGroup#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		super.save(memento);
	}

	public SyncViewerActions getRefreshGroup() {
		return refreshGroup;
	}

	/*
	 * Only enable actions if a context is available. In addition disable the outgoing filter if
	 * the subscriber doesn't support releasing changes to it.
	 */
	private void updateEnablement(SubscriberInput input) {
		if(input == null) {
			incomingMode.setEnabled(false);
			outgoingMode.setEnabled(false);
			conflictsMode.setEnabled(false);
			bothMode.setEnabled(false);
		} else {
			TeamSubscriber s = input.getSubscriber();
			incomingMode.setEnabled(true);
			conflictsMode.setEnabled(true);
			bothMode.setEnabled(true);
			if( ! s.isReleaseSupported()) {
				outgoingMode.setEnabled(false);					
			} else {
				outgoingMode.setEnabled(true);	
			}
			int defaultMode = TeamUIPlugin.getPlugin().getPreferenceStore().getInt(IPreferenceIds.SYNCVIEW_SELECTED_MODE);
			bothMode.setChecked(false);
			incomingMode.setChecked(false);
			outgoingMode.setChecked(false);
			conflictsMode.setChecked(false);
			switch(defaultMode) {
				case INCOMING_MODE: incomingMode.setChecked(true); break; 
				case CONFLICTING_MODE: conflictsMode.setChecked(true); break;
				case BOTH_MODE: bothMode.setChecked(true); break;
				case OUTGOING_MODE: 
					// handle the case where the outgoing mode is disabled.
					if(outgoingMode.isEnabled()) {
						outgoingMode.setChecked(true);
					} else {
						incomingMode.setChecked(true);
					}
					break;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.actions.SyncViewerActionGroup#initializeActions()
	 */
	protected void initializeActions() {
		SubscriberInput input = getSubscriberContext();
		if(input != null) {
			updateEnablement(input);
		} else {
			updateEnablement(null);
		}
		super.initializeActions();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.actions.SyncViewerActionGroup#removeContext(org.eclipse.ui.actions.ActionContext)
	 */
	public void removeContext(ActionContext context) {
		SubscriberInput input = getSubscriberContext();
		if(input != null) {
			updateEnablement(null);
		}
	}
}