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

/**
 * Scheduling rules are used by jobs to indicate when they need exclusive access
 * to a resource.  The job manager guarantees that no two jobs with conflicting
 * scheduling rules will run concurrently. The job manager provides no implementations
 * of scheduling rules, but clients are free to implement their own.
 * 
 * @see Job#getRule
 * @see Job#setRule
 * @since 3.0
 */
public interface ISchedulingRule {
	/**
	 * Returns whether this scheduling rule is compatible with another scheduling rule.
	 * If <code>false</code> is returned, then no job with this rule will be run at the 
	 * same time as a job with the conflicting rule.  If <code>true</code> is returned, 
	 * then the job manager is free to run jobs with these rules at the same time.
	 * <p>
	 * Implementations of this method must be sure to obey the standard rules
	 * of equivalence relations.  That is, implementations must be reflexive,
	 * symmetric, transitive, and consistent.    Implementations of this method
	 * must return <code>false</code> when compared to a rule they know nothing
	 * about.
	 *
	 * @param rule the rule to check for conflicts
	 * @return <code>true</code> if the rule is conflicting, and <code>false</code>
	 * 	otherwise.
	 */
	public boolean isConflicting(ISchedulingRule rule);
}
