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
package org.eclipse.team.ui.synchronize.subscribers;

import java.util.*;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;

/**
 * This action group provides radio buttons for each possible direction of synchronization information. The
 * modes created by this action group can be configured. The actions directly set the mode of a
 * {@link SubscriberParticipant}.
 *
 * @see SubscriberParticipant
 * @since 3.0 
 */
public class DirectionFilterActionGroup extends ActionGroup implements IPropertyChangeListener {
	
	// The list of created actions	
	private List actions = new ArrayList(3);
	
	// The modes
	private DirectionFilterAction incomingMode;					
	private DirectionFilterAction outgoingMode;
	private DirectionFilterAction bothMode;
	private DirectionFilterAction conflictsMode;
	
	// The participant controlled by these modes
	private SubscriberParticipant page;
	
	// The modes displayed by this action group
	private int supportedModes;
	
	/**
	 * An action filter for a specific mode. 
	 */
	class DirectionFilterAction extends Action {
		private int modeId;
		
		public DirectionFilterAction(String prefix,String commandId, int modeId) {
			super("", AS_RADIO_BUTTON); //$NON-NLS-1$
			this.modeId = modeId;
			Utils.initAction(this, prefix, Policy.getBundle());
			Action a = new Action() {
				public void run() {
					DirectionFilterAction.this.run();
				}
			};
		}
		public void run() {
			if(isChecked()) {
				page.setMode(modeId);
			}
		}
		public int getModeId() {
			return modeId;
		}
	}
	
	/**
	 * Creates a direction filter group with the given supported modes. The
	 * possible values for modes are defined by the {@link SubscriberParticipant}
	 * class.
	 * 
	 * @see SubscriberParticipant#BOTH_MODE
	 * @see SubscriberParticipant#OUTGOING_MODE
	 * @see SubscriberParticipant#INCOMING_MODE
	 * @see SubscriberParticipant#CONFLICTING_MODE
	 * @see SubscriberParticipant#ALL_MODES
	 * 
	 * @param participant the participant showing this group 
	 * @param supportedModes the modes to be shown
	 */
	public DirectionFilterActionGroup(SubscriberParticipant participant, int supportedModes) {		
		this.supportedModes = supportedModes;
		this.page = participant;
		createActions();
		participant.addPropertyChangeListener(this);
		checkMode(participant.getMode());
	}
	
	/**
	 * Sets up the sync modes and the actions for switching between them.
	 */
	private void createActions() {
		// Create the actions
		if((supportedModes & SubscriberParticipant.INCOMING_MODE) != 0) {
			incomingMode = new DirectionFilterAction("action.directionFilterIncoming.", "org.eclipse.team.ui.syncview.incomingFilter",  SubscriberParticipant.INCOMING_MODE); //$NON-NLS-1$ //$NON-NLS-2$
			actions.add(incomingMode);
		}
		
		if((supportedModes & SubscriberParticipant.OUTGOING_MODE) != 0) {
			outgoingMode = new DirectionFilterAction("action.directionFilterOutgoing.", "org.eclipse.team.ui.syncview.outgoingFilter",  SubscriberParticipant.OUTGOING_MODE); //$NON-NLS-1$ //$NON-NLS-2$
			actions.add(outgoingMode);
		}
		
		if((supportedModes & SubscriberParticipant.BOTH_MODE) != 0) {
			bothMode = new DirectionFilterAction("action.directionFilterBoth.", "org.eclipse.team.ui.syncview.bothFilter", SubscriberParticipant.BOTH_MODE); //$NON-NLS-1$ //$NON-NLS-2$
			actions.add(bothMode);
		}
		
		if((supportedModes & SubscriberParticipant.CONFLICTING_MODE) != 0) {
			conflictsMode = new DirectionFilterAction("action.directionFilterConflicts.", "org.eclipse.team.ui.syncview.conflictsFilter", SubscriberParticipant.CONFLICTING_MODE); //$NON-NLS-1$ //$NON-NLS-2$
			actions.add(conflictsMode);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars, String group) {
		super.fillActionBars(actionBars);
		IToolBarManager toolBar = actionBars.getToolBarManager();
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction) it.next();
			if(group != null) {
				toolBar.appendToGroup(group, action);
			} else {
				toolBar.add(action);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillToolBar(IToolBarManager toolBar) {
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction) it.next();
				toolBar.add(action);
		}
	}
	
	private void checkMode(int mode) {
		for (Iterator it = actions.iterator(); it.hasNext();) {
			DirectionFilterAction action = (DirectionFilterAction)it.next();
			if(action.getModeId() == mode) {
				action.setChecked(true);
			} else {
				action.setChecked(false);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals(SubscriberParticipant.P_SYNCVIEWPAGE_MODE)) {
			Integer mode = (Integer)event.getNewValue();
			checkMode(mode.intValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		super.dispose();
	}
}