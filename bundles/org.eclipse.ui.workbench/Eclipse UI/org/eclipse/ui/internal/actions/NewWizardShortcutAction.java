/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Opens a specific new wizard. 
 */
public class NewWizardShortcutAction extends Action implements
        IPluginContribution {
    private IWizardDescriptor wizardElement;

    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 500;

    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 500;
    
    private IWorkbenchWindow window;

    /**
     * Create an instance of this class.
     *
     * @param window the workbench window in which this action will appear
     * @param wizardDesc a wizard element
     */
    public NewWizardShortcutAction(IWorkbenchWindow window,
            IWizardDescriptor wizardDesc) {
        super(wizardDesc.getLabel());
        setToolTipText(wizardDesc.getDescription());
        setImageDescriptor(wizardDesc.getImageDescriptor());
        setId(ActionFactory.NEW.getId());
        wizardElement = wizardDesc;
        this.window = window;
    }

    /**
     * Get the wizard descriptor for this action.
     * 
     * @return the wizard descriptor 
     */
    public IWizardDescriptor getWizardDescriptor() {
		return wizardElement;
	}
   
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        // create instance of target wizard

        INewWizard wizard;
        try {
            wizard = (INewWizard) wizardElement.createWizard();
        } catch (CoreException e) {
            ErrorDialog.openError(window.getShell(), WorkbenchMessages.NewWizardShortcutAction_errorTitle,
                    WorkbenchMessages.NewWizardShortcutAction_errorMessage,
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
                    Object file = Util.getAdapter(input, fileClass);
                    if (file != null) {
                        selectionToPass = new StructuredSelection(file);
                    }
                }
            }
        }

        // even tho we MAY finish early without showing a dialog, prep the
		// wizard with a dialog and such in case it's logic requires it
		// - yes, it wastes a dialog but they are plentiful...
		wizard.init(window.getWorkbench(), selectionToPass);

        Shell parent = window.getShell();
        WizardDialog dialog = new WizardDialog(parent, wizard);
        dialog.create();
        Point defaultSize = dialog.getShell().getSize();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, defaultSize.x),
                Math.max(SIZING_WIZARD_HEIGHT, defaultSize.y));
        window.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.NEW_WIZARD_SHORTCUT);
        
        // if the wizard can finish early and doesn't have any pages, just finish it.
        if (wizardElement.canFinishEarly() && !wizardElement.hasPages()) {
			wizard.performFinish();
			dialog.close();
		} else {
			dialog.open();
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
    	IPluginContribution contribution = getPluginContribution();
    	if (contribution != null) {
			return contribution.getLocalId();
		}
    	return wizardElement.getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
    	IPluginContribution contribution = getPluginContribution();
    	if (contribution != null) {
			return contribution.getPluginId();
		}
    	return null;
    }
    
    /**
     * Return the plugin contribution associated with the wizard.
     * 
     * @return the contribution or <code>null</code>
     * @since 3.1
     */
    private IPluginContribution getPluginContribution() {
		return (IPluginContribution) Util.getAdapter(wizardElement,
				IPluginContribution.class);
	}
}
