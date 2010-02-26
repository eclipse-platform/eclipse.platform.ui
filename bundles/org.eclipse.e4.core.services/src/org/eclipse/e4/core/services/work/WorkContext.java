/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.work;

import org.eclipse.core.runtime.IStatus;

/**
 * Context reporting progress, responding to cancellation, and scheduling related background work.
 * This class is experimental and represents a work in progress.
 * 
 */
public abstract class WorkContext extends WorkScheduler {

	public final static int INITIAL_WORK_REMAINING = 1000;

	/**
	 * Adjusts the remaining work to be the given amount, and optionally sets the task name.
	 * 
	 * @param amount
	 *            the amount of work left
	 * @param optionalTaskName
	 *            an optional user-readable task name
	 */
	public abstract void setWorkRemaining(int amount, String... optionalTaskName);

	/**
	 * Notifies the work context that the given amount of work has been performed. The amount
	 * 
	 * @param amount
	 */
	public abstract void worked(int amount);

	/**
	 * Returns a new synchronous child work context, typically to be passed as an argument to a
	 * method that performs part of the work.
	 * 
	 * @param amount
	 * @param optionalSubtaskname
	 * @return
	 */
	public abstract WorkContext newChild(int amount, String... optionalSubtaskname);

	/**
	 * Returns a new asynchronous child work context responsible for work of the given amount, for
	 * use in asynchronous operations that logically belong to this work context. As the returned
	 * work context is notified about performed work, this work context (its parent) will be
	 * notified about the proportion of the passed amount.
	 * 
	 * @param amount
	 * @param optionalSubtaskname
	 * @return
	 */
	public abstract WorkContext asyncChild(int amount, String... optionalSubtaskname);

	/**
	 * Returns <code>true</code> if work in this work context is to be canceled.
	 * 
	 * @return
	 */
	public abstract boolean isCanceled();

	/**
	 * Indicates that this work is blocked by other activity not belonging to this work context. If
	 * a running operation ever calls <code>setBlocked</code>, it must eventually call
	 * <code>clearBlocked</code> before the operation completes.
	 * <p>
	 * The given status object may be adaptable in order to provide more information about the other
	 * activity.
	 * </p>
	 * 
	 * @param reason
	 *            an optional status object whose message describes the reason why this operation is
	 *            blocked, or <code>null</code> if this information is not available.
	 * @see #clearBlocked()
	 */
	public abstract void setBlocked(IStatus reason);

	/**
	 * Clears the blocked state of the running operation. If a running operation ever calls
	 * <code>setBlocked</code>, it must eventually call <code>clearBlocked</code> before the
	 * operation completes.
	 * 
	 * @see #setBlocked(IStatus)
	 */
	public abstract void clearBlocked();

	/**
	 * Registers the given runnable with this work context to be executed when cancellation is being
	 * requested.
	 * 
	 * @param runnable
	 */
	public abstract void onCancel(Runnable runnable);
}
