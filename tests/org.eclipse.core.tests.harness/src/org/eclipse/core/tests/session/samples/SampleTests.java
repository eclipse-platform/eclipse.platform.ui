/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.session.samples;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.core.tests.session.SessionTestSuite;

public class SampleTests extends TestSuite {
	public SampleTests() {
		addTest(SampleSessionTest.suite());
		addTest(UISampleSessionTest.suite());
		TestSuite another = new SessionTestSuite(EclipseWorkspaceTest.PI_HARNESS);
		another.addTestSuite(SampleSessionTest.class);
		addTest(another);
	}

	public static Test suite() {
		return new SampleTests();
	}
}