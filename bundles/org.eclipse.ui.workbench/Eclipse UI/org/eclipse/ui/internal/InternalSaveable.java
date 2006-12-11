/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @since 3.3
 * 
 */
public class InternalSaveable {

	static class SerialPerObjectRule implements ISchedulingRule {

		private InternalSaveable lockObject = null;

		public SerialPerObjectRule(InternalSaveable lock) {
			lockObject = lock;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule instanceof SerialPerObjectRule) {
				SerialPerObjectRule otherRule = (SerialPerObjectRule) rule;
				return lockObject.equals(otherRule.lockObject);
			}
			return false;
		}

	}

	private boolean savingInBackground;

	/**
	 * @return Returns the lock.
	 */
	/* package */ISchedulingRule getSchedulingRule() {
		return new SerialPerObjectRule(this);
	}

	/**
	 * @return
	 */
	/* package */ boolean isSavingInBackground() {
		return savingInBackground;
	}

	/**
	 * @param savingInBackground The savingInBackground to set.
	 */
	/* package */ void setSavingInBackground(boolean savingInBackground) {
		this.savingInBackground = savingInBackground;
	}

}
