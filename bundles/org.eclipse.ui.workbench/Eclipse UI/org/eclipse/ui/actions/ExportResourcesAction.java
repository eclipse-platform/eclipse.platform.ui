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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ExportWizard;

/**
 * Action representing a generic export operation.
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
 * </p>
 * <p>
 * Note: Despite the name, an export operation can deal with things other than
 * resources; the current name was retained for historical reasons.
 * </p>
 * 
 * @since 2.0
 */
public class ExportResourcesAction extends BaseSelectionListenerAction
        implements ActionFactory.IWorkbenchAction {

    private static final int SIZING_WIZARD_WIDTH = 470;

    private static final int SIZING_WIZARD_HEIGHT = 550;

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /** 
     * Listen for the selection changing and update the
     * actions that are interested
     */
    private final ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structured = (IStructuredSelection) selection;
                ExportResourcesAction.this.selectionChanged(structured);
            }
        }
    };

    /**
     * Create a new instance of this class.
     */
    public ExportResourcesAction(IWorkbenchWindow window) {
        this(window, WorkbenchMessages.getString("ExportResourcesAction.text")); //$NON-NLS-1$
    }

    /**
     * Create a new instance of this class.
     */
    public ExportResourcesAction(IWorkbenchWindow window, String label) {
        super(label); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.file.export"); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages
                .getString("ExportResourcesAction.toolTip")); //$NON-NLS-1$
        setId("export"); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IWorkbenchHelpContextIds.EXPORT_ACTION);
        // self-register selection listener (new for 3.0)
        workbenchWindow.getSelectionService().addSelectionListener(
                selectionListener);

        setText(WorkbenchMessages
                .getString("ExportResourcesAction.fileMenuText")); //$NON-NLS-1$
        setImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ));
        setDisabledImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_EXPORT_WIZ_DISABLED));
    }

    /**
     * Create a new instance of this class
     * 
     * @deprecated use the constructor <code>ExportResourcesAction(IWorkbenchWindow)</code>
     */
    public ExportResourcesAction(IWorkbench workbench) {
        this(workbench.getActiveWorkbenchWindow());
    }

    /**
     * Create a new instance of this class
     *
     * @deprecated use the constructor <code>ExportResourcesAction(IWorkbenchWindow, String)</code>
     */
    public ExportResourcesAction(IWorkbench workbench, String label) {
        this(workbench.getActiveWorkbenchWindow(), label);
    }

    /**
     * Invoke the Export wizards selection Wizard.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        ExportWizard wizard = new ExportWizard();
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
                .getSection("ExportResourcesAction"); //$NON-NLS-1$
        if (wizardSettings == null)
            wizardSettings = workbenchSettings
                    .addNewSection("ExportResourcesAction"); //$NON-NLS-1$
        wizard.setDialogSettings(wizardSettings);
        wizard.setForcePreviousAndNextButtons(true);

        Shell parent = workbenchWindow.getShell();
        WizardDialog dialog = new WizardDialog(parent, wizard);
        dialog.create();
        dialog.getShell().setSize(
                Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
                SIZING_WIZARD_HEIGHT);
        WorkbenchHelp.setHelp(dialog.getShell(), IWorkbenchHelpContextIds.EXPORT_WIZARD);
        dialog.open();
    }

    /**
     * Sets the current selection. 
     * In for backwards compatability. Use selectionChanged() instead.
     * @param selection the new selection
     * @deprecated
     */
    public void setSelection(IStructuredSelection selection) {
        selectionChanged(selection);
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
        workbenchWindow.getSelectionService().removeSelectionListener(
                selectionListener);
        workbenchWindow = null;
    }
}