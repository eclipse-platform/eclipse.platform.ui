/*******************************************************************************
 * Copyright (C) 2014 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.monitoring;

import org.eclipse.ui.internal.monitoring.EventLoopMonitorThread;

/**
 * All classes logging {@link UiFreezeEvent}s have to implement this interface.
 *
 * @since 1.0
 */
public interface IUiFreezeEventLogger {
	/**
	 * Invoked from the {@link EventLoopMonitorThread} whenever a {@link UiFreezeEvent} is ready to
	 * be logged. Implementations of this function must end quickly or else it will impact system
	 * performance. All time-consuming tasks should be executed asynchronously.
	 */
	void log(UiFreezeEvent event);
}
