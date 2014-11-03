/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.api;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * @since 3.1
 *
 */
public class SessionEditorPart extends EditorPart {

	public static int instantiatedEditors = 0;

	private Composite fMainPanel;

	/**
	 *
	 */
	public SessionEditorPart() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof IFileEditorInput)) {
			throw new PartInitException(
					"Invalid Input: Must be IFileEditorInput");
		}
		setSite(site);
		setInput(input);
		++SessionEditorPart.instantiatedEditors;
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
	       fMainPanel = new Composite(parent, SWT.NONE);
	       fMainPanel.setLayout(new RowLayout(SWT.VERTICAL));

	       Label l = new Label(fMainPanel, SWT.NONE);
	       l.setText("Editor Title:");

	       l = new Label(fMainPanel, SWT.BORDER);
	       l.setText(getEditorInput().getName());
	}

	@Override
	public void setFocus() {
		fMainPanel.setFocus();
	}

}
