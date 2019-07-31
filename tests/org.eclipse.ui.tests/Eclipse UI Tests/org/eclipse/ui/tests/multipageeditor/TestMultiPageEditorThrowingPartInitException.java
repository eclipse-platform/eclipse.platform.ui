/*******************************************************************************
 * Copyright (c) 2019 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;

/**
 * A multi-page editor that throws a {@link PartInitException} in
 * {@link #init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)}
 * without assigning the arguments (calling super).
 * <p>
 * It implements additional spy functionality. Consumers should call
 * {@link #resetSpy()} in test teardown.
 */
public final class TestMultiPageEditorThrowingPartInitException extends MultiPageEditorPart {

	public static boolean disposeCalled;
	public static Throwable exceptionWhileDisposing;

	/**
	 * Reset spy.
	 */
	public static void resetSpy() {
		disposeCalled = false;
		exceptionWhileDisposing = null;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// no call to super!
		throw new PartInitException("Simulated exception");
	}

	@Override
	protected void createPages() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void dispose() {
		disposeCalled = true;
		try {
			super.dispose();
		} catch (Exception e) {
			exceptionWhileDisposing = e;
			throw e;
		}
	}

}
