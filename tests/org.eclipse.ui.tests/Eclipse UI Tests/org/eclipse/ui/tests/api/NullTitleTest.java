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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests bug 56822 -- NPE thrown when setTitle(null) is called.
 * 
 * @since 3.0
 */
public class NullTitleTest extends UITestCase {
	
	/**
	 * @param testName
	 */
	public NullTitleTest(String testName) {
		super(testName);
	}
	
	WorkbenchWindow window;
	WorkbenchPage page;
	
	protected void doSetUp() throws Exception {
		super.doSetUp();
		window = (WorkbenchWindow)openTestWindow();
		page = (WorkbenchPage)window.getActivePage();
	}
	
	/**
	 * Ensures that we can call ViewPart.setTitle(null) without throwing
	 * any exceptions
	 * 
	 * @throws Throwable
	 */
	public void testNullTitle() throws Throwable {
		page.showView("org.eclipse.ui.tests.NullTitleView");
	}
	
}
