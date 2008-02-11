/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.application;

import org.eclipse.ui.internal.UISynchronizer;

/**
 * This class provides static methods that help RCP applications interact with
 * the Display.
 * 
 * @since 3.4
 */
public final class DisplayAccess {

	/**
	 * <p>
	 * This method allows threads spawned early in the workbench startup process
	 * to access the Display via the
	 * {@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)} and
	 * {@link org.eclipse.swt.widgets.Display#syncExec(Runnable)} methods.
	 * Without invoking this method from a given thread subsequent calls to the
	 * above Display methods will behave as follows:
	 * </p>
	 * 
	 * <ul>
	 * <li>runnables posted to
	 * {@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)} will not be
	 * invoked until after the Workbench is fully restored.</li>
	 * <li>calls made to
	 * {@link org.eclipse.swt.widgets.Display#syncExec(Runnable)} will block
	 * until the Workbench is fully restored.</li>
	 * </ul>
	 * 
	 * <p>
	 * This method MUST NOT be called from threads created by the workbench. If
	 * invoked from any thread owned by the Workbench this method will throw an
	 * {@link IllegalStateException}.
	 * </p>
	 * 
	 * <p>
	 * It is recommended that this method be used from ALL threads that touch
	 * the display during the startup process, even those that may have been
	 * created in the main application class.
	 * </p>
	 * 
	 * <p>
	 * This method has no effect after the workbench has been restored.
	 * </p>
	 * 
	 * @throws IllegalStateException
	 *             thrown if invoked from a thread created by the workbench.
	 */
	public static void accessDisplayDuringStartup() {
		UISynchronizer.overrideThread.set(Boolean.TRUE);
	}
}
