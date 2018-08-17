/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.FormToolkit;
/**
 * A simple multi-page form editor that uses Eclipse Forms support.
 * Example plug-in is configured to create one instance of
 * form colors that is shared between multiple editor instances.
 */
public class SimpleFormEditor extends FormEditor {
	/**
	 *
	 */
	public SimpleFormEditor() {
	}
	@Override
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(ExamplesPlugin.getDefault().getFormColors(
				display));
	}
	@Override
	protected void addPages() {
		try {
		addPage(new NewStylePage(this));
		addPage(new ErrorMessagesPage(this));
		addPage(new FreeFormPage(this));
		addPage(new SecondPage(this));
		int index = addPage(new Composite(getContainer(), SWT.NULL));
		setPageText(index, "Composite");
		addPage(new ThirdPage(this));
		addPage(new ScrolledPropertiesPage(this));
		addPage(new PageWithSubPages(this));
		}
		catch (PartInitException e) {
			//
		}
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
}
