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
package org.eclipse.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.ui.tests.util.PlatformUtil;

/**
 * Test all areas of the UI.
 */
public class UiTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UiTestSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public UiTestSuite() {
		addTest(new org.eclipse.ui.tests.api.ApiTestSuite());

		if (!PlatformUtil.onLinux()) {
			addTest(new org.eclipse.ui.tests.dialogs.UIAutomatedSuite());
		}
		addTest(new org.eclipse.ui.tests.propertysheet.PropertySheetTestSuite());		
		addTest(new org.eclipse.ui.tests.internal.InternalTestSuite());
		addTest(new org.eclipse.ui.tests.navigator.NavigatorTestSuite());
		addTest(new org.eclipse.ui.tests.adaptable.AdaptableTestSuite());			
		addTest(new org.eclipse.ui.tests.zoom.ZoomTestSuite());			
		addTest(new org.eclipse.ui.tests.datatransfer.DataTransferTestSuite());
	}
}