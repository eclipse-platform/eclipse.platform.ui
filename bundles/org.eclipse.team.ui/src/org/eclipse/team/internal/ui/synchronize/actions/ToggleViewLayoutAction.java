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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.TeamSubscriberParticipant;

public class ToggleViewLayoutAction extends Action implements IPropertyChangeListener {	
	private int layout;
	private TeamSubscriberParticipant participant;
	
	public ToggleViewLayoutAction(TeamSubscriberParticipant participant, int layout) {
		super(null, SWT.RADIO);
		this.participant = participant;
		this.layout = layout;
		if(layout == TeamSubscriberParticipant.TABLE_LAYOUT) {
			Utils.initAction(this, "action.toggleViewFlat."); //$NON-NLS-1$	
		} else {
			Utils.initAction(this, "action.toggleViewHierarchical."); //$NON-NLS-1$
		}
		setChecked(participant.getLayout() == layout);
		participant.addPropertyChangeListener(this);
	}
	
	public void run() {
		participant.setLayout(layout);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getProperty().equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_LAYOUT)) {
			Integer newLayout = (Integer)event.getNewValue();
			setChecked(newLayout.intValue() == layout);
		}		
	}
}