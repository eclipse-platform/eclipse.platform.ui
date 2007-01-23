/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.splash;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;

/**
 * Baseclass for splash implementations. Please note that methods on this class
 * will be invoked while the Workbench is being instantiated. As such, any
 * resource provided by the workbench plug-in cannot be guarenteed to be
 * available to this class while executing. No attempt should be made to access
 * {@link IWorkbench} or any subordinate interfaces or resources.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time.
 * 
 * @since 3.3
 */
public abstract class AbstractSplashHandler {

	private Shell shell;

	/**
	 * Initialize this splash implementation. This is called very early in the
	 * workbench lifecycle before any window is created. The provided shell will
	 * already have a background image provided to it but subclasses are free to
	 * customize the shell in whatever way they see fit. Subclasses should
	 * ensure that they call the base implementation of this method at some
	 * point after their own method is invoked.
	 * 
	 * @param splash
	 *            the splash shell
	 */
	public void init(Shell splash) {
		this.shell = splash;
	}

	/**
	 * Signal the handler to end the splash and dispose of any resources.
	 * Subclasses should ensure that they call the base implementation of this
	 * method at some point after their own method is invoked.
	 */
	public void dispose() {
		shell.close();
		shell = null;
	}

	/**
	 * Return the progress monitor responsible for showing bundle loading.
	 * Default implementation returns a null progress monitor.
	 * 
	 * Calls made to methods on this progress monitor may be made from non-UI
	 * threads so implementors must take care to ensure proper synchronization
	 * with the UI thread if necessary.
	 * 
	 * @return the progress monitor
	 * @see NullProgressMonitor
	 */
	public IProgressMonitor getBundleProgressMonitor() {
		return new NullProgressMonitor();
	}

	/**
	 * Get the Shell associated with this splash screen. This is
	 * <code>null</code> until the {@link #init(Shell)} method is invoked.
	 * 
	 * @return the splash shell
	 */
	public Shell getSplash() {
		return shell;
	}
	
	/**
	 * Perform some update on the splash. If called from a non-UI thread it will
	 * be wrapped by a runnable that may be run before the workbench has been
	 * fully realized.
	 * 
	 * @param r
	 *            the update runnable
	 * @throws Throwable
	 */
	protected void updateUI(final Runnable r) throws Throwable {

		if (Thread.currentThread() == shell.getDisplay().getThread())
			r.run(); // run immediatley if we're on the UI thread
		else {
			// wrapper with a StartupRunnable to ensure that it will run before
			// the
			// UI is fully initialized
			StartupRunnable startupRunnable = new StartupRunnable() {

				public void runWithException() throws Throwable {
					r.run();
				}
			};
			shell.getDisplay().asyncExec(startupRunnable);
		}
	}
}
