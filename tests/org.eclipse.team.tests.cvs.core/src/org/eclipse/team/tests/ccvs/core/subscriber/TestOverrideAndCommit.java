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
package org.eclipse.team.tests.ccvs.core.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.subscriber.OverrideAndCommitAction;
import org.eclipse.team.ui.synchronize.actions.SyncInfoSet;

public class TestOverrideAndCommit extends OverrideAndCommitAction {
	
	private boolean prompted = false;

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitAction#promptForComment(org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.resources.IResource[])
	 */
	protected String promptForComment(RepositoryManager manager, IResource[] resourcesToCommit) {
		return "test comments";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitAction#promptForConflicts(org.eclipse.team.ui.sync.SyncInfoSet)
	 */
	protected int promptForConflicts(SyncInfoSet syncSet) {
		this.prompted = true;
		return 0; // ok to commit all conflicts
	}
	
	public boolean isPrompted() {
		return this.prompted;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return false;
	}
}
