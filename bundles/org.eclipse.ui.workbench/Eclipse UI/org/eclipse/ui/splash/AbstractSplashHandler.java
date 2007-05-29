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
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * Baseclass for splash implementations. Please note that methods on this class
 * will be invoked while the Workbench is being instantiated. As such, any
 * resource provided by the workbench plug-in cannot be guarenteed to be
 * available to this class while executing. No attempt should be made to access
 * {@link IWorkbench} or any subordinate interfaces or resources.
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
	 * <p>
	 * Calls to this method will be made from the UI thread.
	 * </p>
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
	 * 
	 * <p>
	 * Calls to this method will be made from the UI thread.
	 * </p>
	 */
	public void dispose() {
		shell.close();
		shell = null;
	}

	/**
	 * Return the progress monitor responsible for showing bundle loading.
	 * Default implementation returns a null progress monitor.
	 * 
	 * <p>
	 * Calls made to methods on this progress monitor may be made from non-UI
	 * threads so implementors must take care to ensure proper synchronization
	 * with the UI thread if necessary.
	 * </p>
	 * 
	 * <p>
	 * Please note that progress will only be shown if the
	 * "org.eclipse.ui/SHOW_PROGRESS_ON_STARTUP" property has been set to
	 * <code>true</code>. Because this property defaults to <code>false</code>
	 * RCP developers must set this property via a
	 * <code>plugin_customization.ini</code> file or by setting the preference
	 * on the Platform UI perference store in the
	 * {@link WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)}
	 * method if they wish to have progress reported on startup.
	 * </p>
	 * 
	 * @return the progress monitor
	 * @see NullProgressMonitor
	 * @see PlatformUI#getPreferenceStore()
	 * @see IWorkbenchPreferenceConstants#SHOW_PROGRESS_ON_STARTUP
	 * @see WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
	 */
	public IProgressMonitor getBundleProgressMonitor() {
		return new NullProgressMonitor();
	}

	/**
	 * Get the {@link Shell} associated with this splash screen. If this method
	 * returns a non-<code>null</code> value prior to the
	 * {@link #init(Shell)} being invoked then this shell will be used for the
	 * splash shell and it will subsequently be passed to the
	 * {@link #init(Shell)} method. In this way a splash handler may participate
	 * in splash processes prior to the workbench startup.
	 * 
	 * <p>
	 * Calls to this method may be made from any thread. Implementors must take
	 * care to ensure proper synchronization with the UI thread if necessary.
	 * </p>
	 * 
	 * @return the splash shell
	 */
	public Shell getSplash() {
		return shell;
	}
}
