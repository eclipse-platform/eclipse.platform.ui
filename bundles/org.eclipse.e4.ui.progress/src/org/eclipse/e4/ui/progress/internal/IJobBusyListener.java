/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.progress.internal;

import org.eclipse.core.runtime.jobs.Job;

/**
 * The IJobBusyListener is used to listen for running and
 * terminating of jobs of a particular family.
 */
interface IJobBusyListener {

    /**
     * Increment the busy count for job.
     * @param job
     */
    public void incrementBusy(Job job);

    /**
     * Decrement the busy count for job.
     * @param job
     */
    public void decrementBusy(Job job);

}
