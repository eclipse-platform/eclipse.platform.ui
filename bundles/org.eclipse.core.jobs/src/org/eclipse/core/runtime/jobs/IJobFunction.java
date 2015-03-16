/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.*;

/**
 * This is a functional interface representation of {@link Job}, suitable
 * for use in lambda expressions.
 *
 * @see Job#create(String, IJobFunction)
 * @since 3.6
 */
public interface IJobFunction {
	/**
	 * Executes this job.  Returns the result of the execution.
	 * <p>
	 * The provided monitor can be used to report progress and respond to
	 * cancellation.  If the progress monitor has been canceled, the job
	 * should finish its execution at the earliest convenience and return a result
	 * status of severity {@link IStatus#CANCEL}.  The singleton
	 * cancel status {@link Status#CANCEL_STATUS} can be used for
	 * this purpose.  The monitor is only valid for the duration of the invocation
	 * of this method.
	 * <p>
	 * This method must not be called directly by clients.  Clients should call
	 * <code>schedule</code>, which will in turn cause this method to be called.
	 * <p>
	 * Jobs can optionally finish their execution asynchronously (in another thread) by
	 * returning a result status of {@link Job#ASYNC_FINISH}.  Jobs that finish
	 * asynchronously <b>must</b> specify the execution thread by calling
	 * <code>setThread</code>, and must indicate when they are finished by calling
	 * the method <code>done</code>.
	 *
	 * @param monitor the monitor to be used for reporting progress and
	 * responding to cancelation. The monitor is never <code>null</code>
	 * @return resulting status of the run. The result must not be <code>null</code>
	 * @see Job#ASYNC_FINISH
	 * @see Job#done(IStatus)
	 */
	public IStatus run(IProgressMonitor monitor);

}
