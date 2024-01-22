/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.model;

import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.model.ContributionComparator;
import org.eclipse.ui.model.IContributionService;

public class ContributionService implements IContributionService {

	private WorkbenchAdvisor advisor;

	public ContributionService(WorkbenchAdvisor advisor) {
		this.advisor = advisor;
	}

	@Override
	public ContributionComparator getComparatorFor(String contributionType) {
		return advisor.getComparatorFor(contributionType);
	}
}