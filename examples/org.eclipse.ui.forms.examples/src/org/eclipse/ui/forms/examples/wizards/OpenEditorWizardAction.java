/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.examples.wizards;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.forms.examples.internal.OpenFormEditorAction;
import org.eclipse.ui.internal.forms.WizardEditorInput;
/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenEditorWizardAction
		extends OpenFormEditorAction {
	private static final String EDITOR_ID = "org.eclipse.ui.forms.examples.wizard-editor";

	public OpenEditorWizardAction() {
	}
	
	public void run(IAction action) {
		//FormColors colors = new FormColors(getWindow().getShell().getDisplay());
		SampleEditorFormWizard wizard = new SampleEditorFormWizard();
		WizardEditorInput input = new WizardEditorInput(wizard, true);
		openEditor(input, EDITOR_ID);
	}
}