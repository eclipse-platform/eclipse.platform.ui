/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.Platform;
import org.eclipse.team.tests.ccvs.core.cvsresources.AllTestsCVSResources;

/**
 * Tests that don't require the Team UI plugin to be loaded.
 */
public class AllCoreTests extends EclipseTest {

	public AllCoreTests() {
		super();
	}

	public AllCoreTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			suite.addTest(AllTestsCVSResources.suite());
			return new CVSTestSetup(suite);
		} 
		return new TestSetup(suite);
	}
}
