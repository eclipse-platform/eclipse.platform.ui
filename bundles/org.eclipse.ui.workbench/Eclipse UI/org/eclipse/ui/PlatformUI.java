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
	 * @return <code>true</code> if the workbench was terminated with a call
	 * to <code>restart</code>, and <code>false</code> otherwise
	 * @since 3.0
	 * @issue consider returning an int or Object rather than a boolean
	 */
	public static boolean createAndRunWorkbench(WorkbenchAdviser adviser) {
		if (Workbench.getInstance() != null) {
			// app already created a workbench
			throw new IllegalStateException();
		}
		// create the workbench instance
		Workbench workbench = new Workbench(adviser);
		// run the workbench event loop
		return workbench.runUI();
	}
}