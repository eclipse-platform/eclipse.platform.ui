/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime.jobs;

import java.util.ArrayList;

/**
 * A MultiRule is a compound scheduling rule that represents a fixed group of child 
 * scheduling rules.  A MultiRule conflicts with another rule if any of its children conflict 
 * with that rule.  More formally, a compound rule represents a logical intersection 
 * of its child rules with respect to the <code>isConflicting</code> equivalence 
 * relation.
 * <p>
 * A MultiRule will never contain other MultiRules as children.  If a MultiRule is provided
 * as a child, its children will be added instead.
 * </p>
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * @since 3.0
 */
public class MultiRule implements ISchedulingRule {
	private final ISchedulingRule[] rules;
	/**
	 * Creates a new scheduling rule that composes a set of nested rules.
	 * 
	 * @param nestedRules the nested rules for this compound rule.
	 */
	public MultiRule(ISchedulingRule[] nestedRules) {
		this.rules = flatten(nestedRules);
	}
	private ISchedulingRule[] flatten(ISchedulingRule[] nestedRules) {
		ArrayList myRules = new ArrayList(nestedRules.length);
		for (int i = 0; i < nestedRules.length; i++) {
			if (nestedRules[i] instanceof MultiRule) {
				ISchedulingRule[] children = ((MultiRule) nestedRules[i]).getChildren();
				for (int j = 0; j < children.length; j++)
					myRules.add(children[j]);
			} else {
				myRules.add(nestedRules[i]);
			}
		}
		return (ISchedulingRule[]) myRules.toArray(new ISchedulingRule[myRules.size()]);
	}
	/**
	 * Returns the child rules within this rule.
	 * @return the child rules
	 */
	public ISchedulingRule[] getChildren() {
		return (ISchedulingRule[]) rules.clone();
	}
	public boolean contains(ISchedulingRule rule) {
		if (rule instanceof MultiRule) {
			ISchedulingRule[] otherRules = ((MultiRule) rule).getChildren();
			//for each child of the target, there must be some child in this rule that contains it.
			for (int other = 0; other < otherRules.length; other++) {
				boolean found = false;
				for (int mine = 0; !found && mine < rules.length; mine++)
					found = rules[mine].contains(otherRules[other]);
				if (!found)
					return false;
			}
		} else {
			for (int i = 0; i < rules.length; i++)
				if (rules[i].contains(rule))
					return true;
		}
		return false;
	}
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof MultiRule) {
			ISchedulingRule[] otherRules = ((MultiRule) rule).getChildren();
			for (int j = 0; j < otherRules.length; j++)
				for (int i = 0; i < rules.length; i++)
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