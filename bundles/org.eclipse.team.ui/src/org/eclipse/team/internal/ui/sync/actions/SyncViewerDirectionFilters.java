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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.UIConstants;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionContext;

/**
 * This ActionGroup provides filtering of a sync set by change direction.
 * The actions are presented to the user as toolbar buttons where only one
 * button is active at a time
 */
public class SyncViewerDirectionFilters extends SyncViewerActionGroup {

	private static final String MEMENTO_KEY = "SyncViewerDirectionFilters";
	
	// An array of the selection actions for the modes (indexed by mode constant)	
	private List actions = new ArrayList(3);
	private SyncViewerActions refreshGroup;	
	private final static int[] DEFAULT_FILTER = new int[] {SyncInfo.INCOMING, SyncInfo.OUTGOING, SyncInfo.CONFLICTING};
			
	/**
	 * Action for toggling the sync mode.
	 */
	class DirectionFilterAction extends Action {
		// The sync mode that this action enables
		private int syncMode;
		// the title to be used for the view when this mode is active
		private String viewTitle;
		public DirectionFilterAction(String title, ImageDescriptor image, int mode, String viewTitle) {
			super(title, SWT.TOGGLE);
			setImageDescriptor(image);
			this.syncMode = mode;
			this.viewTitle = viewTitle;
		}
		public void run() {
			updateFilter(this);
		}
		public int getFilter() {
			return syncMode;
		}
		public String getViewTitle() {
			return viewTitle;
		}
	}
	
	protected SyncViewerDirectionFilters(SyncViewer viewer, SyncViewerActions refreshGroup) {
		super(viewer);
		this.refreshGroup = refreshGroup;
		createActions();
	}
	
	/**
	 * Sets up the sync modes and the actions for switching between them.
	 */
	private void createActions() {
		// Create the actions
		DirectionFilterAction incomingMode = new DirectionFilterAction(
			Policy.bind("SyncView.incomingModeAction"), //$NON-NLS-1$
			TeamImages.getImageDescriptor(UIConstants.IMG_SYNC_MODE_CATCHUP_ENABLED),
			SyncInfo.INCOMING,
			Policy.bind("SyncView.incomingModeTitle"));
		incomingMode.setToolTipText(Policy.bind("SyncView.incomingModeToolTip")); //$NON-NLS-1$
		incomingMode.setDisabledImageDescriptor(TeamImages.getImageDescriptor(UIConstants.IMG_SYNC_MODE_CATCHUP_DISABLED));
		incomingMode.setHoverImageDescriptor(TeamImages.getImageDescriptor(UIConstants.IMG_SYNC_MODE_CATCHUP));
		actions.add(incomingMode);
					
		DirectionFilterAction outgoingMode = new DirectionFilterAction(
			Policy.bind("SyncView.outgoingModeAction"), //$NON-NLS-1$
			TeamImages.getImageDescriptor(UIConstants.IMG_SYNC_MODE_RELEASE_ENABLED),
			SyncInfo.OUTGOING,
			Policy.bind("SyncView.outgoingModeTitle"));
		outgoingMode.setToolTipText(Policy.bind("SyncView.outgoingModeToolTip")); //$NON-NLS-1$
		outgoingMode.setDisabledImageDescriptor(TeamImages.getImageDescriptor(UIConstants.IMG_SYNC_MODE_RELEASE_DISABLED));
		outgoingMode.setHoverImageDescriptor(TeamImages.getImageDescriptor(UIConstants.IMG_SYNC_MODE_RELEASE));
		actions.add(outgoingMode);
		
		DirectionFilterAction conflictsMode = new DirectionFilterAction(
			Policy.bind("CatchupReleaseViewer.showOnlyConflictsAction"), //$NON-NLS-1$
			TeamImages.getImageDescriptor(UIConstants.IMG_DLG_SYNC_CONFLICTING_ENABLED),
			SyncInfo.CONFLICTING,
			"Synchronize - Conflict Mode");
		conflictsMode.setToolTipText(Policy.bind("CatchupReleaseViewer.showOnlyConflictsAction")); //$NON-NLS-1$
		conflictsMode.setDisabledImageDescriptor(TeamImages.getImageDescriptor(UIConstants.IMG_DLG_SYNC_CONFLICTING_DISABLED));
		conflictsMode.setHoverImageDescriptor(TeamImages.getImageDescriptor(UIConstants.IMG_DLG_SYNC_CONFLICTING));
		actions.add(conflictsMode);
		
		updateCheckedState(DEFAULT_FILTER);
		updateEnablement(null);
	}
	
	public int[] getDirectionFilter() {
		int[] filters = new int[actions.size()];
		int i = 0;
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction)it.next();
			if(action.isChecked()) {
				filters[i++] = action.getFilter();
			}
		}
		int[] enabledFilters = new int[i];
		System.arraycopy(filters, 0, enabledFilters, 0, i);
		return enabledFilters;
	}

	boolean isSet(int[] filters, int afilter) {
		for (int i = 0; i < filters.length; i++) {
			if(filters[i] == afilter) return true;
		}
		return false;
	}

	void updateFilter(DirectionFilterAction action) {
		int[] filters = getDirectionFilter();
		
		// don't allow all filters to be unchecked
		if(filters.length == 0) {
			action.setChecked(true);
		} else  {
			getRefreshGroup().refreshFilters();
		}		
	}
	
	public void updateCheckedState(int[] newFilter) {
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction) it.next();
			action.setChecked(isSet(newFilter, action.getFilter()));
		}		
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
	 * 
	 */
	private void updateEnablement(SubscriberInput input) {
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction) it.next();
			if(input == null) {
				action.setEnabled(false);	
			} else {
				TeamSubscriber s = input.getSubscriber();
				if(action.getFilter() == SyncInfo.OUTGOING && ! s.isReleaseSupported()) {
					action.setChecked(false);
					action.setEnabled(false);					
				} else {
					if(! action.isEnabled()) {
						action.setChecked(true);
						action.setEnabled(true);
					}
				}
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