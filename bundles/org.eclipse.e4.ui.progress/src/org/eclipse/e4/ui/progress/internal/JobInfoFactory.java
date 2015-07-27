/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
