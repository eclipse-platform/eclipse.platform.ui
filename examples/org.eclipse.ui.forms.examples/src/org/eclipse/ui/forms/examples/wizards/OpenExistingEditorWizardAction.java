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
import org.eclipse.pde.internal.ui.wizards.plugin.NewPluginProjectWizard;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.forms.examples.internal.OpenFormEditorAction;
import org.eclipse.ui.internal.forms.WizardEditorInput;
/**
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenExistingEditorWizardAction
		extends OpenFormEditorAction {
	private static final String EDITOR_ID = "org.eclipse.ui.forms.examples.wizard-editor";

	public OpenExistingEditorWizardAction() {
	}
	
	public void run(IAction action) {
		NewPluginProjectWizard wizard = new NewPluginProjectWizard();
		WizardEditorInput input = new WizardEditorInput(wizard, false);
		openEditor(input, EDITOR_ID);
	}
}