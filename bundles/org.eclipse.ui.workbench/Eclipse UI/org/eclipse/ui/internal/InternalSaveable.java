/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.jobs.Job;

/**
 * @since 3.3
 * 
 */
public class InternalSaveable {

	private Job backgroundSaveJob;

	/**
	 * @return
	 */
	/* package */Job getBackgroundSaveJob() {
		return backgroundSaveJob;
	}

	/**
	 * @param savingInBackground
	 *            The savingInBackground to set.
	 */
	/* package */void setBackgroundSaveJob(Job backgroundSaveJob) {
		this.backgroundSaveJob = backgroundSaveJob;
	}

	/**
	 * @return
	 */
	/* package */ boolean isSavingInBackground() {
		Job saveJob = backgroundSaveJob;
		if (saveJob == null) {
			return false;
		}
		return (backgroundSaveJob.getState() & (Job.WAITING | Job.RUNNING)) != 0;
	}

}
