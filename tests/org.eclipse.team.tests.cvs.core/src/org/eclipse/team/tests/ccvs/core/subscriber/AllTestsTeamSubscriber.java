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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.ccvs.core.CVSTestSetup;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

public class AllTestsTeamSubscriber extends EclipseTest {

	public AllTestsTeamSubscriber() {
		super();
	}

	public AllTestsTeamSubscriber(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTest(CVSMergeSubscriberTest.suite());
		suite.addTest(CVSWorkspaceSubscriberTest.suite());
		return new CVSTestSetup(suite);
	}
}
