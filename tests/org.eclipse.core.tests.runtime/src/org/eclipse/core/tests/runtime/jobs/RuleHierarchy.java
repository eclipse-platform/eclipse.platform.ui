
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Form a rule hierarchy for testing
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
	
	public String toString() {
		return "HierarchyRule (" + rule + ")";
	}
}
