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
package org.eclipse.team.internal.ccvs.ui.operations;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.ui.sync.SubscriberAction;

/**
 * This context uses the JobStatusHandler from SubscriberAction to ensure
 * proper busy indication in the sync view.
 */
public class CVSSubscriberNonblockingContext extends CVSNonblockingRunnableContext {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CVSNonblockingRunnableContext#schedule(org.eclipse.core.runtime.jobs.Job)
	 */
	protected void schedule(Job job) {
		SubscriberAction.getJobStatusHandler().schedule(job);
	}

}
