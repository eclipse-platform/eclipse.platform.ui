/*******************************************************************************
 * Copyright (c) 2011, 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - bug fixes
 *******************************************************************************/
package org.eclipse.e4.ui.di;

/**
 * Widget toolkit abstract to synchronize back into the UI-Thread from other
 * threads
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public abstract class UISynchronize {
	/**
	 * Executes the runnable on the UI-Thread and blocks until the runnable is
	 * finished
	 * 
	 * @param runnable
	 *            the runnable to execute
	 */
	public abstract void syncExec(Runnable runnable);

	/**
	 * Schedules the runnable on the UI-Thread for execution and returns
	 * immediately
	 * 
	 * @param runnable
	 *            the runnable to execute
	 */
	public abstract void asyncExec(Runnable runnable);
}
