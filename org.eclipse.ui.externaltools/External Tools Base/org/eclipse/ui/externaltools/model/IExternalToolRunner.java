package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;

/**
 * The implementation of this interface is responsible for running
 * an external tool within the specified context.
 * <p>
 * Clients using the extension point to define a new external
 * tool type must provide an implementation of this interface.
 * </p><p>
 * The implementation of this interface will be treated like
 * a singleton. That is, only one instance will be created
 * per tool type.
 * </p><p>
 * This interface is not intended to be extended by clients.
 * </p>
 */
public interface IExternalToolRunner {
	/**
	 * Runs an external tool using the specified context.
	 * 
	 * @param monitor the monitor to report progress or cancellation to
	 * @param runnerContext the context representing the tool to run
	 * @param status a multi status to report any problems while running tool
	 */
	public void run(IProgressMonitor monitor, IRunnerContext runnerContext, MultiStatus status);
}
