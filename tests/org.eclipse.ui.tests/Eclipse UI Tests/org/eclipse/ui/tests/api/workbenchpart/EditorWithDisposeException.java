/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class EditorWithDisposeException extends EditorPart {

    @Override
	public void doSave(IProgressMonitor monitor) {
    }

    @Override
	public void doSaveAs() {
    }

    @Override
	public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {

        if (!(input instanceof IFileEditorInput)) {
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		}
        setSite(site);
        setInput(input);
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
	public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());

        Label testLabel = new Label(parent, SWT.NONE);

        testLabel.setText("This editor is supposed to throw an exception when closed");
    }

    @Override
	public void setFocus() {

    }

    @Override
	public void dispose() {
        throw new RuntimeException("This exception was thrown intentionally as part of an error handling test");
    }

}
