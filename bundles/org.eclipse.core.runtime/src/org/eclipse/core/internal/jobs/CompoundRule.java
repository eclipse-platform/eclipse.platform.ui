/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.ArrayList;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A compound scheduling rule is a fixed group of scheduling rules.  A compound
 * rule conflicts with another rule if any of its rules conflict with that rule.  More
 * formally, a compound rule represents a logical intersection of its nested rules
 * with respect to the <code>isConflicting</code> equivalence relation.
 * 
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 */
public class CompoundRule implements ISchedulingRule {
	private final ISchedulingRule[] rules;
	/**
	 * Creates a new scheduling rule that composes a set of nested rules.
	 * 
	 * @param nestedRules the nested rules for this compound rule.
	 */
	public CompoundRule(ISchedulingRule[] nestedRules) {
		ArrayList myRules = new ArrayList(nestedRules.length);
		for (int i = 0; i < nestedRules.length; i++)
			add(myRules, nestedRules[i]);
		this.rules = (ISchedulingRule[]) myRules.toArray(new ISchedulingRule[myRules.size()]);
	}
	private void add(ArrayList myRules, ISchedulingRule rule) {
		if (rule.getClass() != CompoundRule.class) {
			myRules.add(rule);
		} else {
			ISchedulingRule[] children = ((CompoundRule) rule).rules;
			for (int i = 0; i < children.length; i++)
				add(myRules, children[i]);
		}
	}
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule.getClass() == CompoundRule.class) {
			ISchedulingRule[] otherRules = ((CompoundRule) rule).rules;
			for (int i = 0; i < rules.length; i++)
				for (int j = 0; j < otherRules.length; j++)
					if (rules[i].isConflicting(otherRules[j]))
						return true;
		} else {
			for (int i = 0; i < rules.length; i++)
				if (rules[i].isConflicting(rule))
					return true;
		}
		return false;
	}
}
