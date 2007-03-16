/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.testing.TestableObject;

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
     * @since 3.0
     */
    public static final int RETURN_OK = 0;

    /**
     * Return code (value 1) indicating that the workbench was terminated with
     * a call to <code>IWorkbench.restart</code>.
     * 
     * @see #createAndRunWorkbench
     * @see IWorkbench#restart
     * @since 3.0
     */
    public static final int RETURN_RESTART = 1;

    /**
     * Return code (value 2) indicating that the workbench failed to start.
     * 
     * @see #createAndRunWorkbench
     * @see IWorkbench#restart
     * @since 3.0
     */
    public static final int RETURN_UNSTARTABLE = 2;

    /**
     * Return code (value 3) indicating that the workbench was terminated with
     * a call to IWorkbenchConfigurer#emergencyClose.
     * 
     * @see #createAndRunWorkbench
     * @since 3.0
     */
    public static final int RETURN_EMERGENCY_CLOSE = 3;

    /**
     * Block instantiation.
     */
    private PlatformUI() {
        // do nothing
    }

    /**
     * Returns the workbench. Fails if the workbench has not been created yet.
     * 
     * @return the workbench
     */
    public static IWorkbench getWorkbench() {
        if (Workbench.getInstance() == null) {
            // app forgot to call createAndRunWorkbench beforehand
            throw new IllegalStateException(WorkbenchMessages.PlatformUI_NoWorkbench); 
        }
        return Workbench.getInstance();
    }

    /**
	 * Returns whether {@link #createAndRunWorkbench createAndRunWorkbench} has
	 * been called to create the workbench, and the workbench has yet to
	 * terminate.
	 * <p>
	 * Note that this method may return <code>true</code> while the workbench
	 * is still being initialized, so it may not be safe to call workbench API
	 * methods even if this method returns true. See bug 49316 for details.
	 * </p>
	 * 
	 * @return <code>true</code> if the workbench has been created and is
	 *         still running, and <code>false</code> if the workbench has not
	 *         yet been created or has completed
	 * @since 3.0
	 */
    public static boolean isWorkbenchRunning() {
        return Workbench.getInstance() != null
                && Workbench.getInstance().isRunning();
    }

    /**
     * Creates the workbench and associates it with the given display and workbench
     * advisor, and runs the workbench UI. This entails processing and dispatching
     * events until the workbench is closed or restarted.
     * <p>
     * This method is intended to be called by the main class (the "application").
     * Fails if the workbench UI has already been created.
     * </p>
     * <p>
     * Use {@link #createDisplay createDisplay} to create the display to pass in.
     * </p>
     * <p>
     * Note that this method is intended to be called by the application
     * (<code>org.eclipse.core.boot.IPlatformRunnable</code>). It must be
     * called exactly once, and early on before anyone else asks
     * <code>getWorkbench()</code> for the workbench.
     * </p>
     * 
     * @param display the display to be used for all UI interactions with the workbench
     * @param advisor the application-specific advisor that configures and
     * specializes the workbench
     * @return return code {@link #RETURN_OK RETURN_OK} for normal exit; 
     * {@link #RETURN_RESTART RETURN_RESTART} if the workbench was terminated
     * with a call to {@link IWorkbench#restart IWorkbench.restart}; 
     * {@link #RETURN_UNSTARTABLE RETURN_UNSTARTABLE} if the workbench could
     * not be started; 
     * {@link #RETURN_EMERGENCY_CLOSE RETURN_EMERGENCY_CLOSE} if the UI quit
     * because of an emergency; other values reserved for future use
     * @since 3.0
     */
    public static int createAndRunWorkbench(Display display,
            WorkbenchAdvisor advisor) {
        return Workbench.createAndRunWorkbench(display, advisor);
    }

    /**
     * Creates the <code>Display</code> to be used by the workbench.
     * It is the caller's responsibility to dispose the resulting <code>Display</code>, 
     * not the workbench's.
     * 
     * @return the display
     * @since 3.0
     */
    public static Display createDisplay() {
        return Workbench.createDisplay();
    }

    /**
     * Returns the testable object facade, for use by the test harness.
     * <p>
     * IMPORTANT: This method is only for use by the test harness.
     * Applications and regular plug-ins should not call this method.
     * </p> 
     * 
     * @return the testable object facade
     * @since 3.0
     */
    public static TestableObject getTestableObject() {
        return Workbench.getWorkbenchTestable();
    }

    /**
     * Returns the preference store used for publicly settable workbench preferences.
     * Constants for these preferences are defined on 
     * {@link org.eclipse.ui.IWorkbenchPreferenceConstants}.
     * 
     * @return the workbench public preference store
     * @since 3.0
     */
    public static IPreferenceStore getPreferenceStore() {
        return PrefUtil.getAPIPreferenceStore();
    }
}
