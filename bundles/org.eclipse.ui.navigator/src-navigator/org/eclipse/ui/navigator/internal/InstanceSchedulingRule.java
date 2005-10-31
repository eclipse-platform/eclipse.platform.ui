/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;

import org.eclipse.core.runtime.jobs.ISchedulingRule;


/** 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 *  
 */
public class InstanceSchedulingRule implements ISchedulingRule {

	private Object instanceTarget = null;

	/**
	 *  
	 */
	public InstanceSchedulingRule(Object target) {
		this.instanceTarget = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean contains(ISchedulingRule rule) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	public boolean isConflicting(ISchedulingRule otherRule) {
		if (otherRule instanceof InstanceSchedulingRule) {
			return getInstanceTarget() == ((InstanceSchedulingRule) otherRule).getInstanceTarget();
		}
		return false;
	}

	/**
	 * @return Returns the instanceTarget.
	 */
	protected Object getInstanceTarget() {
		return instanceTarget;
	}

}