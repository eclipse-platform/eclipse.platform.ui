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
import junit.framework.TestSuite;

import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.subscriber.CVSMergeSubscriberTest;
import org.eclipse.team.tests.ccvs.core.subscriber.CVSSyncSubscriberTest;
import org.eclipse.team.tests.ccvs.core.subscriber.CVSWorkspaceSubscriberTest;
import org.eclipse.team.tests.ccvs.ui.CVSUITestCase;

public class AllTestsSynchronizeView extends EclipseTest {

	private SynchronizeView syncView;

	public AllTestsSynchronizeView() {
		super();
	}

	public AllTestsSynchronizeView(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		// sync info tests re-using the subscribers tests but checking state based on the 
		// sync set data structures in the synchronize view.
		suite.addTest(CVSMergeSubscriberTest.suite());
		suite.addTest(CVSWorkspaceSubscriberTest.suite());
		CVSSyncSubscriberTest.setSyncSource(new SyncInfoFromSyncSet());
		
		// place other ui tests here
				
		return new CVSUITestCase(suite);
	}
}
