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
package org.eclipse.team.tests.ccvs.ui.sync;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.subscriber.AllTestsTeamSubscriber;

public class SyncViewTests extends EclipseTest {

	private SyncViewer syncView;

	public SyncViewTests() {
		super();
	}

	public SyncViewTests(String name) {
		super(name);
	}

	public static Test suite() {
		return AllTestsTeamSubscriber.suite(new SyncInfoFromSyncSet());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.CVSWorkspaceSubscriberTest#testDeletionConflicts()
	 */
	public void testDeletionConflicts() throws TeamException, CoreException {
		// Added for Bug 40035 to obscure inherited test
	}
}
