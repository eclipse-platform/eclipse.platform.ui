package org.eclipse.debug.core;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

/**
 * Schedules asynchronous runnables for execution with the debug plug-in.
 * <p>
 * As debug event handlers may need to modify the state of a debug model in
 * response to a debug event, it is neccessary to provide a mechanism to
 * ensure that all debug events have been dispatched and processed before
 * the state of a debug model is modified. Since events may be handled
 * asynchronously by event handlers, the debug platform can not assume that
 * event processing is complete after all listeners are notified of an event
 * set.
 * </p>
 * <p>
 * A scheduler is registered with the debug plug-in for an object, representing
 * a lock or object on which event processing needs to by synchronized. During
 * event dispatch event handlers may register runnables with the debug plug-in,
 * and specify the object (lock) on which the runnable must be synchronized.
 * After event dispatch is complete, the debug plug-in notifies all schedulers
 * for which runnables have been registered, and it then becomes each
 * scheduler's responsibility to notify the debug plug-in when it is safe to
 * execute the registered runnables. When all pertinent schedulers have notified
 * the debug plug-in that it is safe to continue, the runnables are executed in
 * a seperate thread.
 * </p>
 * 
 * @see org.eclipse.debug.core.DebugPlugin#asyncExec(Runnable, Object)
 * @since 2.1
 */
public interface IScheduler {

	/**
	 * Notifies this scheduler that event dispatch is complete, and that
	 * runnables have been registered to run in corrdination with this
	 * scheduler. This scheduler must notify the debug plugin when it is safe to
	 * execute those runnables.
	 */
	public abstract void scheduleRunnables();
}
