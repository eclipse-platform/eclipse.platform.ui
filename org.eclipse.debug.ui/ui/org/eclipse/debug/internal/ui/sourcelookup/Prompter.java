/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.swt.widgets.Display;

/**
 * Prompts the user in the UI (asyncronously), on behalf of a non-UI client,
 * blocking the calling thread until a response is received.
 * <p>
 * This status handler is registered for for the debug UI plug-in,
 * with a status code of <code>STATUS_HANDLER_PROMPT</code>.
 * </p>
 * @since 3.0
 */
public class Prompter implements IStatusHandler {
	/**
	 * Prompts the user for input based on the given status and source
	 * object, blocking the calling thread until the status is resolved.
	 * 
	 * @param status client status code for which a status handler must
	 *  be registered
	 * @param source object requesting the status to be resolved
	 * @return result of resolving the givne status
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(final IStatus status, final Object source) throws CoreException {
		final IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		if (handler == null) {
			// TODO: throw exception
		}
		Display display = DebugUIPlugin.getStandardDisplay();
		if (display.getThread().equals(Thread.currentThread())) {
			return handler.handleStatus(status, source);
		}
		final Object[] result = new Object[1];
		final CoreException[] exception = new CoreException[1];
		final Object lock = this;		
		Runnable r = new Runnable() {
			public void run() {
				try {
					result[0] = handler.handleStatus(status, source);
				} catch (CoreException e) {
					exception[0] = e;
				}
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};
		synchronized (lock) {
			DebugUIPlugin.getStandardDisplay().asyncExec(r);
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
		
		if (exception[0] != null ) {
			throw exception[0];
		}
		return result[0];
	}
}
