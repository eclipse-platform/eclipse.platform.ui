/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import junit.framework.TestSuite;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Tests API methods IJobManager.beginRule and IJobManager.endRule
 */
public class BeginEndRuleTest extends AbstractJobManagerTest {
	public static TestSuite suite() {
		return new TestSuite(BeginEndRuleTest.class);
	}
	public BeginEndRuleTest() {
		super();
	}
	public BeginEndRuleTest(String name) {
		super(name);
	}
	public void testFailureCase() {
		ISchedulingRule rule1 = new IdentityRule();
		ISchedulingRule rule2 = new IdentityRule();
		
		//end without begin
		try {
			manager.endRule(rule1);
			fail("1.0");
		} catch (RuntimeException e) {
			//should fail
		}
		//simple mismatched begin/end
		manager.beginRule(rule1);
		try {
			manager.endRule(rule2);
			fail("1.0");
		} catch (RuntimeException e) {
			//should fail
		}
		//should still be able to end the original rule
		manager.endRule(rule1);
		
	}
}