package org.eclipse.team.tests.ccvs.core.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.ui.subscriber.OverrideAndUpdateSubscriberOperation;

public class TestOverrideAndUpdateOperation extends OverrideAndUpdateSubscriberOperation {	

	private boolean prompted = false; 
	
	protected TestOverrideAndUpdateOperation(IDiffElement[] elements) {
		super(null, elements);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberOperation#promptForOverwrite(org.eclipse.team.core.synchronize.SyncInfoSet)
	 */
	protected boolean promptForOverwrite(SyncInfoSet syncSet) {
		TestOverrideAndUpdateOperation.this.prompted = true;
		return true;
	}
	
	public boolean isPrompted() {
		return this.prompted;
	}
}
