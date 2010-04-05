/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.internal.contexts;

import org.eclipse.e4.core.contexts.ContextChangeEvent;
import org.eclipse.e4.core.contexts.IRunAndTrack;

/**
 * A context strategy for queueing and invoking runnables that are tracking changes in the context.
 * Implementations of this strategy must queue and invoke runnables in the order they are scheduled.
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
	 * This is the same method but for more involved listeners. It should pass in the event
	 * describing the changes that occurred in the context
	 * 
	 * @return <code>true</code> if the runnable is still valid, or <code>false</code> to indicate
	 *         this runnable is no longer valid and should be removed from the context.
	 */
	public boolean schedule(IRunAndTrack runnable, ContextChangeEvent event);

}
