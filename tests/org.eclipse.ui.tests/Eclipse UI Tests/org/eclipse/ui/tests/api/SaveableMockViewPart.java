/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.ISaveablePart;

/**
 * Mock view part that implements ISaveablePart.
 * Used for testing hideView and other view lifecycle on saveable views.
 * 
 * @since 3.0.1
 */
public class SaveableMockViewPart extends MockViewPart implements
		ISaveablePart {
	
	public static String ID = "org.eclipse.ui.tests.api.SaveableMockViewPart";

	private boolean isDirty = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		callTrace.add("doSave" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		callTrace.add("doSaveAs" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		callTrace.add("isDirty" );
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		callTrace.add("isSaveAsAllowed" );
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		callTrace.add("isSaveOnCloseNeeded" );
		return true;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
}
