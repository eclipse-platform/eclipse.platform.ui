/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/**
	 * @param advisor
	 */
	public ContributionService(WorkbenchAdvisor advisor) {
		this.advisor = advisor;
	}
	
	public ContributionComparator getComparatorFor(String contributionType) {
		return advisor.getComparatorFor(contributionType);
	}
}