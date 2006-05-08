/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.ISaveablePart2;

/**
 * Mock view part that implements ISaveablePart.
 * Used for testing hideView and other view lifecycle on saveable views.
 * 
 * @since 3.0.1
 */
public class UserSaveableMockViewPart extends MockViewPart implements
		ISaveablePart2 {
	
	public static String ID = "org.eclipse.ui.tests.api.UserSaveableMockViewPart";

	private boolean isDirty = false;

	private boolean saveAsAllowed = false;

	private boolean saveNeeded = true;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		callTrace.add("doSave" );
		isDirty = false;
		saveNeeded = false;
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
		return saveAsAllowed ;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		callTrace.add("isSaveOnCloseNeeded" );
		return saveNeeded;
	}

	public void setDirty(boolean d) {
		this.isDirty = d;
        firePropertyChange(PROP_DIRTY);
	}
    
    public void setSaveAsAllowed(boolean isSaveAsAllowed) {
        this.saveAsAllowed = isSaveAsAllowed;
    }
    
    public void setSaveNeeded(boolean isSaveOnCloseNeeded) {
        this.saveNeeded = isSaveOnCloseNeeded;
    }

	public int promptToSaveOnClose() {
		return ISaveablePart2.DEFAULT;
	}
}
