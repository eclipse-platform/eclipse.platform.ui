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
package org.eclipse.ui.internal.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;

/**
 * Opens a specific new wizard. 
 */
public class NewWizardShortcutAction extends Action implements
        IPluginContribution {
    private WorkbenchWizardElement wizardElement;

    private IWorkbenchWindow window;

    /**
     * Create an instance of this class.
     *
     * @param window the workbench window in which this action will appear
     * @param element a wizard element
     */
    public NewWizardShortcutAction(IWorkbenchWindow window,
            WorkbenchWizardElement element) {
        super(element.getLabel(element));
        setToolTipText(element.getDescription());
        setImageDescriptor(element.getImageDescriptor());
        setId(ActionFactory.NEW.getId());
        wizardElement = element;
        this.window = window;
    }

    /**
     *	This action has been invoked by the user
     */
    public void run() {
        // create instance of target wizard

        INewWizard wizard;
        try {
            wizard = (INewWizard) wizardElement.createExecutableExtension();
        } catch (CoreException e) {
            ErrorDialog.openError(window.getShell(), WorkbenchMessages
                    .getString("NewWizardShortcutAction.errorTitle"), //$NON-NLS-1$
                    WorkbenchMessages
                            .getString("NewWizardShortcutAction.errorMessage"), //$NON-NLS-1$
                    e.getStatus());
            return;
        }

        ISelection selection = window.getSelectionService().getSelection();
        IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
        if (selection instanceof IStructuredSelection) {
            selectionToPass = wizardElement
                    .adaptedSelection((IStructuredSelection) selection);
        } else {
            // Build the selection from the IFile of the editor
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart) part).getEditorInput();
                Class fileClass = LegacyResourceSupport.getFileClass();
                if (input != null && fileClass != null) {
                    Object file = input.getAdapter(fileClass);
                    if (file != null) {
                        selectionToPass = new StructuredSelection(file);
                    }
                }
            }
        }

        wizard.init(window.getWorkbench(), selectionToPass);

        Shell parent = window.getShell();
        WizardDialog dialog = new WizardDialog(parent, wizard);
        dialog.create();
        WorkbenchHelp.setHelp(dialog.getShell(),
                IWorkbenchHelpContextIds.NEW_WIZARD_SHORTCUT);
        dialog.open();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return wizardElement.getLocalId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return wizardElement.getPluginId();
    }
}