/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.window.Window;

/**
 * This handler will pass along to the workbench advisor exceptions and errors
 * thrown while running the event loop. However, the <code>ThreadDeath</code>
 * error is simply thrown again, and is not passed along.
 */
public final class ExceptionHandler implements Window.IExceptionHandler {

	private static final ExceptionHandler instance = new ExceptionHandler();

	/**
	 * Returns the singleton exception handler.
	 *
	 * @return the singleton exception handler
	 */
	public static ExceptionHandler getInstance() {
		return instance;
	}

	private int exceptionCount = 0; // To avoid recursive errors

	private ExceptionHandler() {
		// prevents instantiation
	}

	@Override
	public void handleException(Throwable t) {
		try {
			// Ignore ThreadDeath error as its normal to get this when thread dies
			if (t instanceof ThreadDeath) {
				throw (ThreadDeath) t;
			}

			// Check to avoid recursive errors
			exceptionCount++;
			if (exceptionCount > 2) {
				if (t instanceof RuntimeException) {
					throw (RuntimeException) t;
				} else if (t instanceof Error) {
					throw (Error) t;
				} else {
					throw new RuntimeException(t);
				}
			}

			// Let the advisor handle this now
			Workbench wb = Workbench.getInstance();
			if (wb != null) {
				wb.getAdvisor().eventLoopException(t);
			}
		} finally {
			exceptionCount--;
		}
	}
}
