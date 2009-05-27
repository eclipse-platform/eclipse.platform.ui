/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.e4.ui.tests.workbench.ContextTest;
import org.eclipse.e4.ui.tests.workbench.HandlerTest;
import org.eclipse.e4.ui.tests.workbench.MSashTest;
import org.eclipse.e4.ui.tests.workbench.MWindowTest;

/**
 *
 */
public class UIAllTests extends TestSuite {
	public static Test suite() {
		return new UIAllTests();
	}

	public UIAllTests() {
		addTestSuite(MWindowTest.class);
		addTestSuite(MSashTest.class);
		addTestSuite(HandlerTest.class);
		addTestSuite(ContextTest.class);
	}
}
