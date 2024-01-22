/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.jobs.Job;

/**
 * @since 3.3
 */
public class InternalSaveable {

	private Job backgroundSaveJob;

	/* package */Job getBackgroundSaveJob() {
		return backgroundSaveJob;
	}

	/* package */void setBackgroundSaveJob(Job backgroundSaveJob) {
		this.backgroundSaveJob = backgroundSaveJob;
	}

	/* package */ boolean isSavingInBackground() {
		Job saveJob = backgroundSaveJob;
		if (saveJob == null) {
			return false;
		}
		return (backgroundSaveJob.getState() & (Job.WAITING | Job.RUNNING)) != 0;
	}

}
