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

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Creates a rule hierarchy for testing
 * The hierarchy is of the form:
 * 			R1
 * 		   /  \
 * 		  R2  R3
 * 		 /      \
 * 		R4		R5 
 * and so forth. R1 is always the root of the hierarchy so the reset
 * method has to be called before using the hierarchy.
 * To get rules, just create new instances of the class.
 * The first instance of the class after a reset call will be R1, the second R2 etc.
 */
public class RuleHierarchy implements ISchedulingRule {
	private static int ruleNumber = 1;
	public final int rule = ruleNumber++;
		
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean contains(ISchedulingRule rule) {
		if((rule instanceof RuleHierarchy) && (((RuleHierarchy)rule).rule >= this.rule) 
				&& ((((RuleHierarchy)rule).rule%2 == this.rule%2) || (this.rule < 2))) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule rule) {
		if(contains(rule))
			return true;
		if(rule.contains(this))
			return true;
		
		return false;
	}
	
	/**
	 * Reset the hierachy. (to simplify reuse, since the first rule is the root of the hierarchy)
	 */
	public static void reset() {
		ruleNumber = 1;
	}
	
	public String toString() {
		return "HierarchyRule (" + rule + ")";
	}
}
