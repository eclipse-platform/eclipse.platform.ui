/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.ui.application.WorkbenchAdviser;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The central class for access to the Eclipse Platform User Interface. 
 * This class cannot be instantiated; all functionality is provided by 
 * static methods.
 * 
 * Features provided:
 * <ul>
 * <li>creation of the workbench.</li>
 * <li>access to the workbench.</li>
 * </ul>
 * <p>
 *
 * @see IWorkbench
 */
public final class PlatformUI {
	/**
	 * Identifies the workbench plug-in.
	 */
	public static final String PLUGIN_ID = "org.eclipse.ui"; //$NON-NLS-1$
	
	/**
	 * Return code (value 0) indicating that the workbench terminated normally.
	 * 
	 * @see #createAndRunWorkbench
	 */
	public static final int RETURN_OK = 0;

	/**
	 * Return code (value 1) indicating that the workbench was terminated with
	 * a call to <code>IWorkbench.restart</code>.
	 * 
	 * @see #createAndRunWorkbench
	 * @see IWorkbench#restart
	 */
	public static final int RETURN_RESTART = 1;

	/**
	 * Return code (value 2) indicating that the workbench failed to start.
	 * 
	 * @see #createAndRunWorkbench
	 * @see IWorkbench#restart
	 */
	public static final int RETURN_UNSTARTABLE = 2;

	/**
	 * Return code (value 3) indicating that the workbench was terminated with
	 * a call to <code>IWorkbenchConfigurer.emergencyClose</code>.
	 * 
	 * @see #createAndRunWorkbench
	 * @see IWorkbenchConfigurer#emergencyClose
	 */
	public static final int RETURN_EMERGENCY_CLOSE = 3;

	/**
	 * Block instantiation.
	 */
	private PlatformUI() {
	}
	
	/**
	 * Returns the workbench. Fails if the workbench has not been created yet.
	 * 
	 * @return the workbench
	 */
	public static IWorkbench getWorkbench() {
		if (Workbench.getInstance() == null) {
			// app forgot to call createAndRunWorkbench beforehand
			throw new IllegalStateException(WorkbenchMessages.getString("PlatformUI.NoWorkbench")); //$NON-NLS-1$
		}
		return Workbench.getInstance();
	}
	
	/**
	 * Creates the workbench and associates it with the given workbench adviser,
	 * and runs the workbench UI. This entails processing and dispatching
	 * events until the workbench is closed or restarted.
	 * <p>
	 * This method is intended to be called by the main class (the "application").
	 * Fails if the workbench UI has already been created.
	 * </p>
	 * <p>
	 * Note that this method is intended to be called by the application
	 * (<code>org.eclipse.core.boot.IPlatformRunnable</code>). It must be
	 * called exactly once, and early on before anyone else asks
	 * <code>getWorkbench()</code> for the workbench.
	 * </p>
	 * 
	 * @param adviser the application-specific adviser that configures and
	 * specializes the workbench
	 * @return return code {@link #RETURN_OK RETURN_OK} for normal exit; 
	 * {@link #RETURN_RESTART RETURN_RESTART} if the workbench was terminated
	 * with a call to {@link IWorkbench#restart IWorkbench.restart}; 
	 * {@link #RETURN_UNSTARTABLE RETURN_UNSTARTABLE} if the workbench could
	 * not be started; other values reserved for future use
	 * @since 3.0
	 */
	public static int createAndRunWorkbench(WorkbenchAdviser adviser) {
		return Workbench.createAndRunWorkbench(adviser);
	}
}