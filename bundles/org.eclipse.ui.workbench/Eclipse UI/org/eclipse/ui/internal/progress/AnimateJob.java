/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.progress.ProgressMessages;

/**
 * AnimateJob is an abstract class for the job that runs the animations.
 */
abstract class AnimateJob extends Job {

	/**
	 * Create a new animate job with the supplied name.
	 * @param name
	 */
	public AnimateJob(String name) {
		super(name);
		setSystem(true);
	}

	/**
	 * Create an AnimateJob with the default name.
	 *
	 */
	public AnimateJob() {
		this(ProgressMessages.getString("AnimateJob.JobName")); //$NON-NLS-1$
	}

}
