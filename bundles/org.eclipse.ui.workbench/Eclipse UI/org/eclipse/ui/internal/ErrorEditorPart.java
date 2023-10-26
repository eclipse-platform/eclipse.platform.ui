/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.internal.part.StatusPart;
import org.eclipse.ui.part.EditorPart;

/**
 * This part is shown instead the editors with errors.
 *
 * @since 3.3
 */
public class ErrorEditorPart extends EditorPart {

	private IStatus error;
	private Composite parentControl;

	/**
	 * Creates instance of the class
	 */
	public ErrorEditorPart() {
	}

	/**
	 * Creates instance of the class
	 *
	 * @param error the status
	 */
	public ErrorEditorPart(IStatus error) {
		this.error = error;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parentControl = parent;
		if (error != null) {
			new StatusPart(parent, error);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
		parentControl.setFocus();
	}

	@Override
	public void setPartName(String newName) {
		super.setPartName(newName);
	}

	@Override
	public void dispose() {
		super.dispose();
		parentControl = null;
	}

	/**
	 * @return Returns the error status or <code>null</code> if part was created
	 *         without an explicit error status.
	 */
	public IStatus getError() {
		return error;
	}
}
