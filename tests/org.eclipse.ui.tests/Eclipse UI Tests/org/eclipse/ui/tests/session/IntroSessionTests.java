/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.session;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.ui.PlatformUI;

/**
 * Tests intro-related session properties.
 * 
 * @since 3.1
 */
public class IntroSessionTests extends TestCase {
	
	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.IntroSessionTests");
		ts.addTest(new IntroSessionTests("testIntro"));
		return ts;
	}
	
	/**
	 * @param name
	 */
	public IntroSessionTests(String name) {
		super(name);
	}
	
	public void testIntro() {
		//assert that the intro was not shown
		assertNull(PlatformUI.getWorkbench().getIntroManager().getIntro());
	}
}
