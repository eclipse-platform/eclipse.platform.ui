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
 * A scheduling rule class that is a subset of RuleSetA
 * 
 */
public class RuleSetD extends RuleSetA {
	private static int nextRule = 0;
	
	public boolean contains(ISchedulingRule rule) {
		return (rule instanceof RuleSetD);
	}
	
	/*public boolean isConflicting(ISchedulingRule rule) {
		if(conflict) {
			return ((rule instanceof RuleSetD) || (rule instanceof RuleSetC));
		}
		return (rule == this);
	}*/
	
	int incRule() {
		return ++nextRule;
	}
	
	String getId() {
		return "D";
	}
}
