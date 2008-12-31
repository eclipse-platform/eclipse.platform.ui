/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.filebuffers.ISynchronizationContext;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Synchronization context for file buffers whose documents are shown in the UI.
 * The synchronization runnable is executed in the UI thread.
 *
 * @since 3.0
 */
public class UISynchronizationContext implements ISynchronizationContext {

	/*
	 * @see org.eclipse.core.filebuffers.ISynchronizationContext#run(java.lang.Runnable)
	 */
	public void run(Runnable runnable) {
		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			IWorkbench workbench= PlatformUI.getWorkbench();
			IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
			if (windows != null && windows.length > 0) {
				Display display= windows[0].getShell().getDisplay();
				display.asyncExec(runnable);
			} else {
				runnable.run();
			}
		}
	}
}
