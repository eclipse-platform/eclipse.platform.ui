/*******************************************************************************
 * Copyright (c) 2014, 2023 Wojciech Sudol and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wojciech Sudol - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.jobs;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * TestJobRule is a scheduling rules that makes all jobs sequential.
 *
 */
public class TestJobRule implements ISchedulingRule {
	private int jobOrder;

	public TestJobRule(int order) {
		jobOrder = order;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		if (rule instanceof IResource || rule instanceof TestJobRule)
			return true;
		return false;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof TestJobRule jobRule)
			return jobRule.getJobOrder() >= jobOrder;
		return false;
	}

	/**
	 * Return the order of this rule.
	 * @return
	 */
	public int getJobOrder() {
		return jobOrder;
	}

}
