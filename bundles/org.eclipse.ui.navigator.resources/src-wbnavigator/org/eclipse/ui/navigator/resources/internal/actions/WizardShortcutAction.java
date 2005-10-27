/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * @author mdelder
 *  
 */
public class WizardShortcutAction extends Action implements IPluginContribution {
	private IWizardDescriptor descriptor;
	private IWorkbenchWindow window;

	/**
	 *  
	 */
	public WizardShortcutAction(IWorkbenchWindow window, IWizardDescriptor aDescriptor) {
		super(aDescriptor.getLabel());
		descriptor = aDescriptor;
		setToolTipText(descriptor.getDescription());
		setImageDescriptor(descriptor.getImageDescriptor());
		setId(ActionFactory.NEW.getId()); 
		this.window = window;
	}

	//	/**
	//	 * @param text
	//	 */
	//	public WizardShortcutAction(String text) {
	//		super(text);
	//	}
	//
	//	/**
	//	 * @param text
	//	 * @param image
	//	 */
	//	public WizardShortcutAction(String text, ImageDescriptor image) {
	//		super(text, image);
	//	}
	//
	//	/**
	//	 * @param text
	//	 * @param style
	//	 */
	//	public WizardShortcutAction(String text, int style) {
	//		super(text, style);
	//	}
	//	
	//	

	/**
	 * This action has been invoked by the user
	 * 
	 * @param context
	 *            Window
	 */
	public void run() {
		// create instance of target wizard

		IWorkbenchWizard wizard;
		try {
			wizard = descriptor.createWizard();
		} catch (CoreException e) {
			ErrorDialog.openError(window.getShell(), WorkbenchNavigatorMessages.NewProjectWizard_errorTitle,  
						WorkbenchNavigatorMessages.NewProjectAction_text,  
						e.getStatus());
			return;
		}

		ISelection selection = window.getSelectionService().getSelection();
		/*
		 * IStructuredSelection selectionToPass = StructuredSelection.EMPTY; if (selection
		 * instanceof IStructuredSelection) { selectionToPass =
		 * wizardElement.adaptedSelection((IStructuredSelection) selection); } else { // Build the
		 * selection from the IFile of the editor IWorkbenchPart part =
		 * window.getPartService().getActivePart(); if (part instanceof IEditorPart) { IEditorInput
		 * input = ((IEditorPart) part).getEditorInput(); if (input instanceof IFileEditorInput) {
		 * selectionToPass = new StructuredSelection(((IFileEditorInput) input).getFile()); } } }
		 */

		if (selection instanceof IStructuredSelection)
			wizard.init(window.getWorkbench(), (IStructuredSelection) selection);
		else
			wizard.init(window.getWorkbench(), StructuredSelection.EMPTY);

		Shell parent = window.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		//WorkbenchHelp.setHelp(dialog.getShell(), IWorkbenchHelpContextIds.NEW_WIZARD_SHORTCUT);
		dialog.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
	 */
	public String getLocalId() {
		return descriptor.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
	 */
	public String getPluginId() {
		return descriptor.getId();
	}


}