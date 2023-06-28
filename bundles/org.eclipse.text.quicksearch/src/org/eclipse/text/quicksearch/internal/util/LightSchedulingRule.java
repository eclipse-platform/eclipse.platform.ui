/*******************************************************************************
 * Copyright (c) 2013-2019 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.util;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A scheduling rule that conflicts only with itself and only contains itself.
 * <p>
 * Note that every new instance of this rule is distinct. Different instances do not conflict
 * with eachother (each instance only conflicts with itself.
 *
 * @author Kris De Volder
 */
public class LightSchedulingRule implements ISchedulingRule {
	private final String name;

	/**
	 * Create a scheduling rule that conflicts only with itself and only contains itself.
	 * Runnables that want to have a 'light' impact on blocking other jobs
	 * but still some guarantee that they won't trample over other things that require
	 * access to some internal shared resource that only they can access can use this
	 * rule to protect the resource.
	 */
	public LightSchedulingRule(String name) {
		this.name = name;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this || rule.contains(this);
	}

	@Override
	public String toString() {
		return name;
	}
}