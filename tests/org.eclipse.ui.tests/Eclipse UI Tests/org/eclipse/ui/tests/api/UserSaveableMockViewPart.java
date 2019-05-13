/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public void doSave(IProgressMonitor monitor) {
		callTrace.add("doSave" );
		setDirty(false);
		saveNeeded = false;
	}

	@Override
	public void doSaveAs() {
		callTrace.add("doSaveAs" );
	}

	@Override
	public boolean isDirty() {
		callTrace.add("isDirty" );
		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		callTrace.add("isSaveAsAllowed" );
		return saveAsAllowed ;
	}

	@Override
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

	@Override
	public int promptToSaveOnClose() {
		return ISaveablePart2.DEFAULT;
	}
}
