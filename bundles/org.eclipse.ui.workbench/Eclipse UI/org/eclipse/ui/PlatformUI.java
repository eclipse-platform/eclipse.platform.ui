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
	 * Returns the workbench. Fails if the workbench has not been
	 * created yet.
	 * 
	 * @return the workbench
	 * @throws IllegalStateException If <code>createWorkbench</code> not called once beforehand
	 */
	public static IWorkbench getWorkbench() {
		if (Workbench.getInstance() == null) {
			// app forgot to call createWorkbench beforehand
			throw new IllegalStateException(WorkbenchMessages.getString("PlatformUI.NoWorkbench")); //$NON-NLS-1$
		}
		return Workbench.getInstance();
	}

	/**
	 * Returns whether the workbench has been created.
	 * 
	 * @return <code>true</code> if workbench created, <code>false</code> otherwise
	 */
	public static boolean isWorkbenchCreated() {
		return Workbench.getInstance() != null;
	}
	
	/**
	 * Creates the workbench and associates it with the given workbench adviser.
	 * <p>
	 * Note that this method is intended to be called by the application
	 * (<code>org.eclipse.core.boot.IPlatformRunnable</code>). It must be
	 * called exactly once, and early on before anyone else asks
	 * <code>getWorkbench()</code> for the workbench.
	 * </p>
	 * 
	 * @param adviser the application-specific adviser that configures and
	 * specializes the workbench
	 * @return the created workbench
	 * @throws IllegalArgumentException If adviser is invalid
	 * @throws IllegalStateException If <code>createWorkbench</code> has been called before
	 * @since 3.0
	 */
	public static IWorkbench createWorkbench(WorkbenchAdviser adviser) {
		// create the workbench instance (but do not run the UI, the app will do that)
		return new Workbench(adviser);
	}
}