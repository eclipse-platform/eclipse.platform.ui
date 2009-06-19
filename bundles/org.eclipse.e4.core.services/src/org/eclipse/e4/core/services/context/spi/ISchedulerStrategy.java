/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.services.context.spi;

import org.eclipse.e4.core.services.context.IEclipseContext;

/**
 * A context strategy for queueing and invoking runnables that are tracking
 * changes in the context. Implementations of this strategy must queue and
 * invoke runnables in the order they are scheduled.
 * 
 * @see IEclipseContext#runAndTrack(Runnable)
 */
public interface ISchedulerStrategy extends IEclipseContextStrategy {

	/**
	 * Schedules a runnable for execution.
	 * 
	 * @param runnable
	 *            The runnable to execute
	 */
	public void schedule(Runnable runnable);

	/**
	 * This is the same method but for more involved listeners. It should pass
	 * in the context that has been changed, name of the changed service, and
	 * the arguments.
	 * 
	 * @return <code>true</code> if the runnable is still valid, or
	 *         <code>false</code> to indicate this runnable is no longer valid
	 *         and should be removed from the context.
	 */
	public boolean schedule(IEclipseContext context, IRunAndTrack runnable, String name,
			int eventType, Object[] args);

}
