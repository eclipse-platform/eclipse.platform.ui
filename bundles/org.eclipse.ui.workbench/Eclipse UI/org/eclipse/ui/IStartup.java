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

/**
 * Plug-ins that register a startup extension will be activated after
 * the Workbench initializes and have an opportunity to run 
 * code that can't be implemented using the normal contribution 
 * mechanisms.
 * 
 * @since 2.0
 */
public interface IStartup {
    /**
     * Will be called in a separate thread after the workbench initializes.
     * <p>
     * Note that most workbench methods must be called in the UI thread
     * since they may access SWT.  For example, to obtain the current workbench
     * window, use:
     * <code>
     * <pre>
     * final IWorkbench workbench = PlatformUI.getWorkbench();
     * workbench.getDisplay().asyncExec(new Runnable() {
     *   public void run() {
     *     IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
     *     if (window != null) {
     *       // do something
     *     }
     *   }
     * });
     * </pre>
     * </code>
     * </p>
     * @see org.eclipse.swt.widgets.Display#asyncExec
     * @see org.eclipse.swt.widgets.Display#syncExec
     */
    public void earlyStartup();
}

