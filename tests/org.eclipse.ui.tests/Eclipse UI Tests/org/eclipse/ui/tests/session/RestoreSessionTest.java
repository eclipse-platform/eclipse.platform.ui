/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.session;

import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;

import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @since 3.5
 */
public class RestoreSessionTest extends TestCase {
	public static TestSuite suite() {
		TestSuite ts = new TestSuite("org.eclipse.ui.tests.session.RestoreSessionTest");
		ts.addTest(new RestoreSessionTest("testDisplayAccess"));
		return ts;
	}
	
	public RestoreSessionTest(String name) {
		super(name);
	}

	/**
	 * Checks various combinations of Thread + DisplayAccess + [a]sync execs. 
	 * Anything without a call to DisplayAccess should be deferred until after startup.
	 * @see RCPTestWorkbenchAdvisor#preStartup()
	 */
	public void testDisplayAccess() {
		// check that there was a display during startup
		assertNotNull(RCPTestWorkbenchAdvisor.syncWithoutDisplayAccess);
		assertNotNull(RCPTestWorkbenchAdvisor.syncWithDisplayAccess);
		
		assertNotNull(RCPTestWorkbenchAdvisor.asyncWithDisplayAccess);
		assertNotNull(RCPTestWorkbenchAdvisor.asyncWithoutDisplayAccess);
		
		assertNotNull(RCPTestWorkbenchAdvisor.asyncDuringStartup);
		
		// test startup threading - async run during startup
		assertEquals(Boolean.FALSE, RCPTestWorkbenchAdvisor.asyncDuringStartup);
		
		// sync & async from qualified thread should have run during the startup
		assertEquals(Boolean.TRUE, RCPTestWorkbenchAdvisor.syncWithDisplayAccess);
		assertEquals(Boolean.TRUE, RCPTestWorkbenchAdvisor.asyncWithDisplayAccess);
		
		// sync & async from a non-qualified thread should not have run during the startup
		assertEquals(Boolean.FALSE, RCPTestWorkbenchAdvisor.syncWithoutDisplayAccess);
		assertEquals(Boolean.FALSE, RCPTestWorkbenchAdvisor.asyncWithoutDisplayAccess);
		
		// display access during startup in UI thread should not cause an exception
		assertFalse(RCPTestWorkbenchAdvisor.displayAccessInUIThreadAllowed);
	}
}
