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

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceUpdateAction;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.ui.sync.SyncInfoSet;

class TestUpdateAction extends WorkspaceUpdateAction {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SafeUpdateAction#warnAboutFailedResources(org.eclipse.team.ui.sync.SyncInfoSet)
	 */
	protected void warnAboutFailedResources(SyncInfoSet syncSet) {
		return;
	}

	protected boolean promptForOverwrite(SyncInfoSet syncSet) {
		EclipseTest.fail("Should never prompt on update, simply update nodes that are valid.");
		return false;
	}
	
	public IRunnableWithProgress getRunnable(SyncInfoSet syncSet) {
		return super.getRunnable(syncSet);
	}
	
	protected boolean canRunAsJob() {
		return false;
	}
}
