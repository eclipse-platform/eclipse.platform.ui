/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime;

/**
 * An extension to the IProgressMonitor interface for monitors that want to support
 * feedback when an activity is blocked due to concurrent activity in another thread.
 * <p>
 * This interface should be implemented by the same object that implements 
 * <code>IProgressMonitor</code>.  When a monitor that supports this extension
 * is passed to an operation, the operation should call <code>setBlocked(true)</code>
 * whenever it knows that it must wait for a lock that is currently held by another
 * thread.  The operation should continue to check for and respond to cancelation 
 * requests while blocked.  When the operation is no longer blocked, it must
 * call <code>setBlocked(false)</code> to clear the flag.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IProgressMonitor
 * @since 3.0
 */
public interface IProgressBlockedMonitor {
	/**
	 * Returns whether the current operation is in a blocked state.
	 *
	 * @return <code>true</code> if the current operation is blocked on a lock,
	 *    and <code>false</code> otherwise
	 * @see #setBlocked
	 */
	public boolean isBlocked();
	/**
	 * Sets the blocked state of the running operation.  If a running operation ever sets
	 * the blocked state to <code>true</code>, it must be set back to <code>false</code>
	 * before the operation completes.
	 * 
	 * @param value <code>true</code> indicates that the running operation
	 * 	is blocked due to a lock held by another thread;
	 *     <code>false</code> clears this flag, indicating the running operation
	 * 	is no longer blocked and is either proceeding normally or terminating.
	 * @see #isBlocked
	 */
	public void setBlocked(boolean blocked);
}