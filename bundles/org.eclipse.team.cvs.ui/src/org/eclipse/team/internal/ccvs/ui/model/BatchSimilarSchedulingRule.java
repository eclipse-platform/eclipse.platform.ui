/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A simple job scheduling rule for serializing jobs that shouldn't be run
 * concurrently.
 */
public class BatchSimilarSchedulingRule implements ISchedulingRule {
	public String id;
	public BatchSimilarSchedulingRule(String id) {
		this.id = id;
	}		
	public boolean isConflicting(ISchedulingRule rule) {
		if(rule instanceof BatchSimilarSchedulingRule) {
			return ((BatchSimilarSchedulingRule)rule).id.equals(id);
		}
		return false;
	}
	public boolean contains(ISchedulingRule rule) {		
		return isConflicting(rule);
	}
}