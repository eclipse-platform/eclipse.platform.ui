/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.subscriber;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceCommitOperation;

class TestCommitOperation extends WorkspaceCommitOperation {
	
	private boolean prompted;

	public TestCommitOperation(IDiffElement[] elements, boolean override) {
		super(null, elements, override);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamOperation#canRunAsJob()
	 */
	protected boolean canRunAsJob() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitOperation#promptForComment(org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.resources.IResource[])
	 */
	protected String promptForComment(RepositoryManager manager, IResource[] resourcesToCommit) {
		return "dummy comment";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitOperation#promptForConflicts(org.eclipse.team.core.synchronize.SyncInfoSet)
	 */
	protected int promptForConflicts(SyncInfoSet syncSet) {
		this.prompted = true;
		return 0; // ok to commit all conflicts
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitOperation#promptForResourcesToBeAdded(org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.resources.IResource[])
	 */
	protected IResource[] promptForResourcesToBeAdded(RepositoryManager manager, IResource[] unadded) {
		return unadded;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitOperation#promptForConflictHandling(org.eclipse.team.core.synchronize.SyncInfoSet)
	 */
	protected boolean promptForConflictHandling(SyncInfoSet syncSet) {
		return true;
	}
	
	public boolean isPrompted() {
		return this.prompted;
	}
}
