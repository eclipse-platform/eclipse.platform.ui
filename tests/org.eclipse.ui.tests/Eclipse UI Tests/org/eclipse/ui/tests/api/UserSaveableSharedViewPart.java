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
import org.eclipse.ui.ISaveablePart;

/**
 * Mock view part that implements ISaveablePart. Used for testing hideView and
 * other view lifecycle on saveable views.
 * 
 * @since 3.0.1
 */
public class UserSaveableSharedViewPart extends MockViewPart implements
		ISaveablePart {
	/**
	 * The shared ID.
	 */
	public static String ID = "org.eclipse.ui.tests.api.UserSaveableSharedViewPart";

	public static class SharedModel {
		public boolean isDirty = true;
	}
	
	private SharedModel fSharedModel = new SharedModel();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		callTrace.add("doSave");
		fSharedModel.isDirty = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		callTrace.add("doSaveAs");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		callTrace.add("isDirty");
		return fSharedModel.isDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		callTrace.add("isSaveOnCloseNeeded");
		return fSharedModel.isDirty;
	}
	
	public void setSharedModel(SharedModel s) {
		fSharedModel = s;
	}
	
	public SharedModel getSharedModel() {
		return fSharedModel;
	}
}
