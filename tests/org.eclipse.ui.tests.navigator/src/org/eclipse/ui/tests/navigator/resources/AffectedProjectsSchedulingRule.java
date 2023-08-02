/*******************************************************************************
 * Copyright (c) 2023 ArSysOp
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nikifor Fedorov (ArSysOp) - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

final class AffectedProjectsSchedulingRule implements ISchedulingRule {

	private final List<IProject> projects;

	public AffectedProjectsSchedulingRule(List<IProject> projects) {
		this.projects = projects;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		if (rule == this) {
			return true;
		}
		return projects.stream().anyMatch(project -> project.contains(rule));
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return projects.stream().anyMatch(rule::isConflicting);
	}

}