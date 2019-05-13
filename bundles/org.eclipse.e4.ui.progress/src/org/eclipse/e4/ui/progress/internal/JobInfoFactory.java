/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
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
package org.eclipse.e4.ui.progress.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class JobInfoFactory {

	@Inject
	Services services;

	public JobInfo getJobInfo(Job enclosingJob) {
		return new JobInfo(enclosingJob,
				services.getService(ProgressManager.class),
				services.getService(FinishedJobs.class));
	}
}
