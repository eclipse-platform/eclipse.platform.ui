/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.jface.progress.UIJob;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The WorkbenchUIJob is a job that can attempt to find a display to
 * use if the current display is not valid.
 */
public abstract class WorkbenchUIJob extends UIJob {

	/**
	 * Create a new instance of the receiver with an unspecified
	 * display.
	 */
	public WorkbenchUIJob() {
		super();
		
	}

	/**
	 * Create a new instance of the receiver with the supplied display.
	 * @param jobDisplay
	 */
	public WorkbenchUIJob(Display jobDisplay) {
		super(jobDisplay);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.progress.UIJob#getDisplay()
	 */
	public Display getDisplay() {
		Display display = super.getDisplay();
		if(display == null){
			IWorkbenchWindow windows[] = WorkbenchPlugin.getDefault().getWorkbench().getWorkbenchWindows();
			if(windows.length == 0)
				return null;
			else
				return windows[0].getShell().getDisplay();
		}
		else
			return display;
	}

}
