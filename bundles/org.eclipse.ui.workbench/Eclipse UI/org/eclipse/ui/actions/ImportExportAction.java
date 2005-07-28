/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.PerspectiveTracker;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportExportWizard;

/**
 * Action representing a generic import/export operation.
 * <p>
 * This class may be instantiated. It is not intended to be subclassed.
 * </p>
 * <p>
 * This method automatically registers listeners so that it can keep its
 * enablement state up to date. Ordinarily, the window's references to these
 * listeners will be dropped automatically when the window closes. However,
 * if the client needs to get rid of an action while the window is still open,
 * the client must call IWorkbenchAction#dispose to give the
 * action an opportunity to deregister its listeners and to perform any other
 * cleanup.
 * 
 * </p>
 * 
 * @since 3.2
 */
public class ImportExportAction extends BaseSelectionListenerAction
        implements ActionFactory.IWorkbenchAction {

    private static final int SIZING_WIZARD_WIDTH = 470;

    private static final int SIZING_WIZARD_HEIGHT = 550;

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Tracks perspective activation, to update this action's
     * enabled state.
     */
    private PerspectiveTracker tracker;

    /** 
     * Listen for the selection changing and update the
     * actions that are interested
     */
    private final ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structured = (IStructuredSelection) selection;
                ImportExportAction.this.selectionChanged(structured);
            }
        }
    };

    /**
     * Create a new instance of this class.
     * 
     * @param window the window
     */
    public ImportExportAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.ImportExportAction_text);	//$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        tracker = new PerspectiveTracker(window, this);
        setActionDefinitionId("org.eclipse.ui.file.importexport"); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.ImportExportAction_toolTip);
        setId("importexport"); //$NON-NLS-1$
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.IMPORT_EXPORT_ACTION);
        // self-register selection listener (new for 3.0)
        workbenchWindow.getSelectionService().addSelectionListener(
                selectionListener);

        // TODO get new icon for import/export
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_IMPORT_WIZ));
    }

    /**
     * Invoke the Import wizards selection Wizard.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        ImportExportWizard wizard = new ImportExportWizard();
        IStructuredSelection selectionToPass;
        // get the current workbench selection
        ISelection workbenchSelection = workbenchWindow.getSelectionService()
                .getSelection();
        if (workbenchSelection instanceof IStructuredSelection) {
            selectionToPass = (IStructuredSelection) workbenchSelection;
        } else {
            selectionToPass = StructuredSelection.EMPTY;
        }

        wizard.init(workbenchWindow.getWorkbench(), selectionToPass);
        IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault()
                .getDialogSettings();
        IDialogSettings wizardSettings = workbenchSettings
                .getSection("ImportExportAction"); //$NON-NLS-1$
        if (wizardSettings == null)
            wizardSettings = workbenchSettings
                    .addNewSection("ImportExportAction"); //$NON-NLS-1$
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);

        Shell parent = workbenchWindow.getShell();
        WizardDialog dialog = new WizardDialog(parent, wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
				IWorkbenchHelpContextIds.IMPORT_EXPORT_ACTION);
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
        tracker.dispose();
        workbenchWindow.getSelectionService().removeSelectionListener(
                selectionListener);
        workbenchWindow = null;
    }
}
