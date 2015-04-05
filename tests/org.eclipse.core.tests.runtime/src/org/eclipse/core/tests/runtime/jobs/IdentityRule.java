/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A scheduling rule that always conflicts with an identical instance, but not with any
 * other rules.
 */
public class IdentityRule implements ISchedulingRule {
	private static int nextRule = 0;
	private final int ruleNumber = nextRule++;

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public String toString() {
		return "IdentityRule(" + ruleNumber + ")";
	}
}
