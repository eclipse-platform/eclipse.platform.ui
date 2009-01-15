/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * meken@users.sourceforge.net - bug 204837 commonWizard with no pages displays empty dialog
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.wizards;

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
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public class WizardShortcutAction extends Action implements IPluginContribution {
	private IWizardDescriptor descriptor;

	private IWorkbenchWindow window;

	/**
	 * 
	 * @param aWindow
	 *            The window to use for the shell and selection service.
	 * @param aDescriptor
	 *            The descriptor with information for triggering the desired
	 *            wizard.
	 */
	public WizardShortcutAction(IWorkbenchWindow aWindow,
			IWizardDescriptor aDescriptor) {
		super(aDescriptor.getLabel());
		descriptor = aDescriptor;
		setToolTipText(descriptor.getDescription());
		setImageDescriptor(descriptor.getImageDescriptor());
		setId(ActionFactory.NEW.getId());
		this.window = aWindow;
	}

	/**
	 * This action has been invoked by the user 
	 */
	public void run() {
		// create instance of target wizard

		IWorkbenchWizard wizard;
		try {
			wizard = descriptor.createWizard();
		} catch (CoreException e) {
			ErrorDialog.openError(window.getShell(),
					CommonNavigatorMessages.NewProjectWizard_errorTitle,
					CommonNavigatorMessages.NewProjectAction_text, e
							.getStatus());
			return;
		}

		ISelection selection = window.getSelectionService().getSelection();

		if (selection instanceof IStructuredSelection) {
			wizard
					.init(window.getWorkbench(),
							(IStructuredSelection) selection);
		} else {
			wizard.init(window.getWorkbench(), StructuredSelection.EMPTY);
		}
		
		if(descriptor.canFinishEarly() && !descriptor.hasPages()) {
			wizard.performFinish();
		} else {
			Shell parent = window.getShell();
			WizardDialog dialog = new WizardDialog(parent, wizard);
			dialog.create();
			// WorkbenchHelp.setHelp(dialog.getShell(),
			// IWorkbenchHelpContextIds.NEW_WIZARD_SHORTCUT);
			dialog.open();
		}
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
