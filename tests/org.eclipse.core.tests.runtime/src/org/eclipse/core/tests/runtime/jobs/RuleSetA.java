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
 * The root of a scheduling rule hierarchy
 * Can be set to conflict with all rules in the hierarchy
 * 
 */
public class RuleSetA implements ISchedulingRule {
	private static int nextRule = 0;
	private final int ruleNumber = incRule();
	static boolean conflict = false; //flag to indicate whether rules in the hierarchy should conflict with each other
		
	public boolean contains(ISchedulingRule rule) {
		return (rule instanceof RuleSetA);
	}
	
	public boolean isConflicting(ISchedulingRule rule) {
		if(conflict) {
			return (rule instanceof RuleSetA);
		}
		return (rule == this);
	}
	
	String getId() {
		return ("A");
	}
	
//	static void setConflict(boolean flag) {
//		conflict = flag;
//	}
	
	int incRule() {
		return ++nextRule;
	}
	
	public String toString() {
		return "ScheduleRule(Set" + getId() + "-" + ruleNumber + ")";
	}
	
}
