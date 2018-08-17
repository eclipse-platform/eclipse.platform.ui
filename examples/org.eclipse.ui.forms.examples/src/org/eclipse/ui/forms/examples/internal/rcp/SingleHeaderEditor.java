/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * A form editor that has several pages but only one stable header.
 */
public class SingleHeaderEditor extends SharedHeaderFormEditor {
	/**
	 *
	 */
	public SingleHeaderEditor() {
	}

	@Override
	protected FormToolkit createToolkit(Display display) {
		// Create a toolkit that shares colors between editors.
		return new FormToolkit(ExamplesPlugin.getDefault().getFormColors(
				display));
	}

	@Override
	protected void createHeaderContents(IManagedForm headerForm) {
		final ScrolledForm sform = headerForm.getForm();
		//sform.setText("Shared header for all the pages");
		getToolkit().decorateFormHeading(sform.getForm());
		addToolBar(sform.getForm());
		sform.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FILE));
		headerForm.getForm().getDisplay().timerExec(5000, () -> sform.setText("<Another text>"));
		//sform.setMessage("Static text", 0);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new HeadlessPage(this, 1));
			addPage(new HeadlessPage(this, 2));
			addPage(new HeadlessPage(this, 3));
		} catch (PartInitException e) {
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

	private void addToolBar(Form form) {
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
			}
		};
		haction.setChecked(true);
		haction.setToolTipText("Horizontal orientation");
		haction.setImageDescriptor(ExamplesPlugin.getDefault()
				.getImageRegistry()
				.getDescriptor(ExamplesPlugin.IMG_HORIZONTAL));
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) {
			@Override
			public void run() {
			}
		};
		vaction.setChecked(false);
		vaction.setToolTipText("Vertical orientation");
		vaction.setImageDescriptor(ExamplesPlugin.getDefault()
				.getImageRegistry().getDescriptor(ExamplesPlugin.IMG_VERTICAL));
		ControlContribution save = new ControlContribution("save") {
			@Override
			protected Control createControl(Composite parent) {
				Button saveButton = new Button(parent, SWT.PUSH);
				saveButton.setText("Save");
				return saveButton;
			}
		};
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
		form.getToolBarManager().add(save);
		form.getToolBarManager().update(true);
	}
}
