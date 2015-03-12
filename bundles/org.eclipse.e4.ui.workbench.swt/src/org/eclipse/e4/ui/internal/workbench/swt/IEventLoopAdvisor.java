/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import org.eclipse.swt.widgets.Display;

/**
 * Allow client applications to interact with the event loop.
 */
public interface IEventLoopAdvisor {
	/**
	 * Performs arbitrary work or yields when there are no events to be
	 * processed.
	 * <p>
	 * This method is called when there are currently no more events on the
	 * queue to be processed at the moment.
	 * </p>
	 * <p>
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation yields until new events enter the
	 * queue. Subclasses may override or extend this method. It is generally a
	 * bad idea to override with an empty method.
	 * </p>
	 *
	 * @param display
	 *            the main display of the rendering UI
	 */
	public void eventLoopIdle(Display display);

	/**
	 * Performs arbitrary actions when the event loop crashes (the code that
	 * handles a UI event throws an exception that is not caught).
	 * <p>
	 * This method is called when the code handling a UI event throws an
	 * exception. In a perfectly functioning application, this method would
	 * never be called. In practice, it comes into play when there are bugs in
	 * the code that trigger unchecked runtime exceptions. It is also activated
	 * when the system runs short of memory, etc. Fatal errors (ThreadDeath) are
	 * not passed on to this method, as there is nothing that could be done.
	 * </p>
	 * <p>
	 * Clients must not call this method directly (although super calls are
	 * okay). The default implementation logs the problem so that it does not go
	 * unnoticed. Subclasses may override or extend this method. It is generally
	 * a bad idea to override with an empty method, and you should be especially
	 * careful when handling Errors.
	 * </p>
	 *
	 * @param exception
	 *            the uncaught exception that was thrown inside the UI event
	 *            loop
	 */
	public void eventLoopException(Throwable exception);
}
