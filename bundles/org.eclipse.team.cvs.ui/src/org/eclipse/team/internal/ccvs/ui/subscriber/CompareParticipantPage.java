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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelProvider;
import org.eclipse.team.internal.ui.synchronize.actions.RemoveSynchronizeParticipantAction;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.subscriber.SynchronizeViewerAdvisor;
import org.eclipse.ui.IActionBars;

public class CompareParticipantPage extends CVSSynchronizeViewPage {

	private RemoveSynchronizeParticipantAction removeAction;
	private Action groupByCommentAction;
	private boolean groupByComment = false;

	private class CompareAdvisor extends CVSSynchronizeViewerAdvisor {
		public CompareAdvisor(ISynchronizeView view, SubscriberParticipant participant) {
			super(view, participant);
		}

		protected SynchronizeModelProvider getModelProvider() {
			if (groupByComment) {
				return new ChangeLogModelProvider(getSyncInfoSet());
			}
			return super.getModelProvider();
		}

		public void refreshModel() {
			setInput(getViewer());
		}
	}
	
	public CompareParticipantPage(SubscriberParticipant participant, ISynchronizeView view) {
		super(participant, view);
		removeAction = new RemoveSynchronizeParticipantAction(getParticipant());
		groupByCommentAction = new Action("Group by comments", Action.AS_CHECK_BOX) { //$NON-NLS-1$
			public void run() {
				groupByComment = ! groupByComment;
				setChecked(groupByComment);
				CompareAdvisor advisor = ((CompareAdvisor)CompareParticipantPage.this.getViewerAdviser());
				try {
					advisor.prepareInput(new NullProgressMonitor());
				} catch (TeamException e) {
					Utils.handle(e);
				}
				advisor.refreshModel();
			}
		};
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
			IMenuManager mgr = actionBars.getMenuManager();
			//mgr.add(new Separator());
			//mgr.add(groupByCommentAction);
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSynchronizeViewPage#createSynchronizeViewerAdvisor()
	 */
	protected SynchronizeViewerAdvisor createSynchronizeViewerAdvisor() {
		return new CompareAdvisor(getSynchronizeView(), getParticipant());
	}
}
