/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.IStatus;

/**
 * The IProgressManager is an interface to the progress manager provided by the
 * workbench. <b>NOTE: This is experimental API and subject to change at any
 * time</b>.
 */
public interface IProgressManager {

	/**
	 * Block the current thread until UIJob is served. The message is used to
	 * announce to the user a pending UI Job.
	 * 
	 * Note: This is experimental API and subject to change at any time.

	 * @param job
	 * @param message
	 * @return IStatus
	 * @since 3.0
	 */
	public IStatus requestInUI(UIJob job, String message);




}
