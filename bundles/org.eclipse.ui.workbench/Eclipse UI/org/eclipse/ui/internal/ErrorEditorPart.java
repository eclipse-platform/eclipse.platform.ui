/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
}
