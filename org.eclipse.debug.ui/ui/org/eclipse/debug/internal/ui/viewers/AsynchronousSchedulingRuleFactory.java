/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule factory for asynchronous operations.
 * 
 * @since 3.2
 */
public class AsynchronousSchedulingRuleFactory {
	
	private static AsynchronousSchedulingRuleFactory fgFactory = null;

	/**
	 * Rule allows only one job to run at a time
	 */
	class SerialRule implements ISchedulingRule {

		public SerialRule() {
		}

		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof SerialRule;
		}
	}
	
   class SerialPerObjectRule implements ISchedulingRule {
    	
    	private Object fObject = null;
    	
    	public SerialPerObjectRule(Object lock) {
    		fObject = lock;
    	}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule instanceof SerialPerObjectRule) {
				SerialPerObjectRule vup = (SerialPerObjectRule) rule;
				return fObject == vup.fObject;
			}
			return false;
		}
    	
    }	
	
	private AsynchronousSchedulingRuleFactory() {}
	
	public static AsynchronousSchedulingRuleFactory getDefault() {
		if (fgFactory == null) {
			fgFactory = new AsynchronousSchedulingRuleFactory();
		}
		return fgFactory;
	}
	
	/**
	 * Returns a scheduling rule that allows all jobs with an instance
	 * of the rule to run one at a time.
	 *  
	 * @return scheduling rule
	 */
	public ISchedulingRule newSerialRule() {
		return new SerialRule();
	}
	
	/**
	 * Returns a scheduling rule that allows all jobs with an instance
	 * of the rule on the same object to run one at a time.
	 * 
	 * @param lock object to serialize one
	 * @return scheduling rule
	 */
	public ISchedulingRule newSerialPerObjectRule(Object lock) {
		return new SerialPerObjectRule(lock);
	}
	
}
