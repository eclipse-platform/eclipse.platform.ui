/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import javax.inject.Inject;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;

public class ClientEditor {

	@Inject
	private MDirtyable dirtyable;

	private boolean saveCalled = false;

	boolean focusCalled = false;

	private boolean throwException = false;

	public void setThrowException(boolean throwException) {
		this.throwException = throwException;
	}

	@Focus
	void delegateFocus() {
		focusCalled = true;
	}

	@Persist
	void doSave() {
		saveCalled = true;
		if (throwException) {
			throw new RuntimeException();
		}

		dirtyable.setDirty(false);
	}

	public boolean wasSaveCalled() {
		return saveCalled;
	}

	public boolean wasFocusCalled() {
		return focusCalled;
	}

}
