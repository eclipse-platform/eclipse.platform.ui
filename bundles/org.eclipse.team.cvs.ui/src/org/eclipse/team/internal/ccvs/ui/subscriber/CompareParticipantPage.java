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

import org.eclipse.jface.action.*;
import org.eclipse.team.internal.ui.synchronize.actions.RemoveSynchronizeParticipantAction;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscriber.DirectionFilterActionGroup;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.ui.IActionBars;

public class CompareParticipantPage extends CVSSynchronizeViewPage {

	private RemoveSynchronizeParticipantAction removeAction;
	private DirectionFilterActionGroup modes;
	private Action updateAdapter;

	public CompareParticipantPage(SubscriberParticipant participant, ISynchronizeView view) {
		super(participant, view);
		removeAction = new RemoveSynchronizeParticipantAction(getParticipant());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		super.setActionBars(actionBars);
		if (actionBars != null) {
			IToolBarManager toolbar = actionBars.getToolBarManager();
			toolbar.add(new Separator());
			toolbar.add(removeAction);
		}
	}
}
