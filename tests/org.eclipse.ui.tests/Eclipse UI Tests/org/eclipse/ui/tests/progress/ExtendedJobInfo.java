/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.progress;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.progress.JobInfo;

/**
 * Only provides better readable {@link #toString()} method.
 */
class ExtendedJobInfo extends JobInfo {

	public ExtendedJobInfo(Job enclosingJob) {
		super(enclosingJob);
	}

	@Override
	public String toString() {
		return "ExtendedJobInfo [getName()=" + getJob().getName() + ", getPriority()=" + getJob().getPriority()
				+ ", getState()=" + getJob().getState() + ", isSystem()=" + getJob().isSystem() + ", isUser()="
				+ getJob().isUser() + "]";
	}
}