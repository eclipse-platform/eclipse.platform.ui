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

package org.eclipse.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.NewWizard;

/**
 * Invoke the resource creation wizard selection Wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * This method automatically registers listeners so that it can keep its
 * enablement state up to date. Ordinarily, the window's references to these
 * listeners will be dropped automatically when the window closes. However,
 * if the client needs to get rid of an action while the window is still open,
 * the client must call #dispose() to give the
 * action an opportunity to deregister its listeners and to perform any other
 * cleanup.
 * </p>
 */
public class NewWizardAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The wizard dialog width
     */
    private static final int SIZING_WIZARD_WIDTH = 500;

    /**
     * The wizard dialog height
     */
    private static final int SIZING_WIZARD_HEIGHT = 500;

    /**
     * The id of the category to show or <code>null</code> to
     * show all the categories.
     */
    private String categoryId = null;

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new instance of this class.
     * @param window
     */
    public NewWizardAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("NewWizardAction.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        // @issues should be IDE-specific images
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
        setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
        setToolTipText(WorkbenchMessages.getString("NewWizardAction.toolTip")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IHelpContextIds.NEW_ACTION);
        setActionDefinitionId("org.eclipse.ui.newWizard"); //$NON-NLS-1$
    }

    /**
     * Create a new instance of this class
     * 
     * @deprecated use the constructor <code>NewWizardAction(IWorkbenchWindow)</code>
     */
    public NewWizardAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * Returns the id of the category of wizards to show
     * or <code>null</code> to show all categories.
     * @return String
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the id of the category of wizards to show
     * or <code>null</code> to show all categories.
     * @param id
     */
    public void setCategoryId(String id) {
        categoryId = id;
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        NewWizard wizard = new NewWizard();
        wizard.setCategoryId(categoryId);

        ISelection selection = workbenchWindow.getSelectionService()
                .getSelection();
        IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
        if (selection instanceof IStructuredSelection) {
            selectionToPass = (IStructuredSelection) selection;
        } else {
            // @issue the following is resource-specific legacy code
            // Build the selection from the IFile of the editor
            Class resourceClass = LegacyResourceSupport.getResourceClass();
            if (resourceClass != null) {
                IWorkbenchPart part = workbenchWindow.getPartService()
                        .getActivePart();
                if (part instanceof IEditorPart) {
                    IEditorInput input = ((IEditorPart) part).getEditorInput();
                    Object resource = input.getAdapter(resourceClass);
                    if (resource != null) {
                        selectionToPass = new StructuredSelection(resource);
                    }
                }
            }
        }

        wizard.init(workbenchWindow.getWorkbench(), selectionToPass);
        IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings wizardSettings = workbenchSettings
                .getSection("NewWizardAction"); //$NON-NLS-1$
        if (wizardSettings == null)
            wizardSettings = workbenchSettings.addNewSection("NewWizardAction"); //$NON-NLS-1$
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);

        Shell parent = workbenchWindow.getShell();
        WizardDialog dialog = new WizardDialog(parent, wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT);
        WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.NEW_WIZARD);
        dialog.open();
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     * @since 3.0
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        workbenchWindow = null;
    }

}