/*
 * Created on Jun 16, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.ui.actions.ActionContext;


class CancelSubscription extends Action {
	private final SyncViewerActions actions;
	public CancelSubscription(SyncViewerActions actions) {
		setText("Cancel");
		this.actions = actions;
		setToolTipText("Cancel the active synchronization target");
	}
	public void run() {
		ActionContext context = actions.getContext();
		SubscriberInput input = (SubscriberInput)context.getInput();
		input.getSubscriber().cancel();
	}
	public void updateTitle(SubscriberInput input) {
		TeamSubscriber subscriber = input.getSubscriber();
		if(subscriber.isCancellable()) {
			setText("Cancel [" + subscriber.getName() +"]");
		} else {
			setText("Cancel");
		}
		setToolTipText("Cancel the active synchronization target");
	}
}