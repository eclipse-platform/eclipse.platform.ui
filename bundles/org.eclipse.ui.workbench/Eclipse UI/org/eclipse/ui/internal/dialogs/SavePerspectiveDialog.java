/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.activities.ws.ActivityViewerFilter;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * The SavePerspectiveDialog can be used to get the name of a new
 * perspective or the descriptor of an old perspective.  The results
 * are returned by <code>getNewPerspName</code> and 
 * <code>getOldPersp</code>.
 */
public class SavePerspectiveDialog extends org.eclipse.jface.dialogs.Dialog
        implements ISelectionChangedListener, ModifyListener {
    private Text text;

    private TableViewer list;

    private Button okButton;

    private PerspectiveRegistry perspReg;

    private String perspName;

    private IPerspectiveDescriptor persp;

    private IPerspectiveDescriptor initialSelection;

    private boolean ignoreSelection = false;

    final private static int LIST_WIDTH = 40;

    final private static int TEXT_WIDTH = 40;

    final private static int LIST_HEIGHT = 14;

    /**
     * PerspectiveDialog constructor comment.
     * @param parentShell the parent shell
     * @param perspReg the perspective registry
     */
    public SavePerspectiveDialog(Shell parentShell, PerspectiveRegistry perspReg) {
        super(parentShell);
        this.perspReg = perspReg;
    }

    /* (non-Javadoc)
     * Method declared in Window.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell
                .setText(WorkbenchMessages.SavePerspective_shellTitle); 
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_DIALOG);
    }

    /**
     * Add buttons to the dialog's button bar.
     *
     * @param parent the button bar composite
     */
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        updateButtons();
        text.setFocus();
    }

    /**
     * Creates and returns the contents of the upper part 
     * of this dialog (above the button bar).
     *
     * @param parent the parent composite to contain the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) {
        Font font = parent.getFont();
        // Run super.
        Composite composite = (Composite) super.createDialogArea(parent);

        // description
        Label descLabel = new Label(composite, SWT.WRAP);
        descLabel.setText(WorkbenchMessages.SavePerspectiveDialog_description);
        descLabel.setFont(parent.getFont());

        // Spacer.
        Label label = new Label(composite, SWT.NONE);
        GridData data = new GridData();
        data.heightHint = 8;
        label.setLayoutData(data);

        // Create name group.
        Composite nameGroup = new Composite(composite, SWT.NONE);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = layout.marginHeight = 0;
        nameGroup.setLayout(layout);

        // Create name label.
        label = new Label(nameGroup, SWT.NONE);
        label.setText(WorkbenchMessages.SavePerspective_name);
        label.setFont(font);

        // Add text field.
        text = new Text(nameGroup, SWT.BORDER);
        text.setFocus();
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = convertWidthInCharsToPixels(TEXT_WIDTH);
        text.setLayoutData(data);
        text.setFont(font);
        text.addModifyListener(this);

        // Spacer.
        label = new Label(composite, SWT.NONE);
        data = new GridData();
        data.heightHint = 5;
        label.setLayoutData(data);

        // Another label.
        label = new Label(composite, SWT.NONE);
        label.setText(WorkbenchMessages.SavePerspective_existing);
        label.setFont(font);

        // Add perspective list.
        list = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        list.setLabelProvider(new PerspectiveLabelProvider());
        list.setContentProvider(new PerspContentProvider());
        list.addFilter(new ActivityViewerFilter());
        list.setComparator(new ViewerComparator());
        list.setInput(perspReg);
        list.addSelectionChangedListener(this);
        list.getTable().setFont(font);

        // Set perspective list size.
        Control ctrl = list.getControl();
        GridData spec = new GridData(GridData.FILL_BOTH);
        spec.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
        spec.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
        ctrl.setLayoutData(spec);

        // Set the initial selection
        if (initialSelection != null) {
            StructuredSelection sel = new StructuredSelection(initialSelection);
            list.setSelection(sel, true);
        }
        text.selectAll();

        // Return results.
        return composite;
    }

    /**
     * Returns the target name.
     * 
     * @return the target name
     */
    public IPerspectiveDescriptor getPersp() {
        return persp;
    }

    /**
     * Returns the target name.
     * 
     * @return the target name
     */
    public String getPerspName() {
        return perspName;
    }

    /**
     * The user has typed some text.
     */
    public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
        // Get text.
        perspName = text.getText();

        // Transfer text to persp list.
        ignoreSelection = true;
        persp = perspReg.findPerspectiveWithLabel(perspName);
        if (persp == null) {
            StructuredSelection sel = new StructuredSelection();
            list.setSelection(sel);
        } else {
            StructuredSelection sel = new StructuredSelection(persp);
            list.setSelection(sel);
        }
        ignoreSelection = false;

        updateButtons();
    }

    /**
     * Notifies that the ok button of this dialog has been pressed.
     * <p>
     * The default implementation of this framework method sets
     * this dialog's return code to <code>Window.OK</code>
     * and closes the dialog. Subclasses may override.
     * </p>
     */
    protected void okPressed() {
        perspName = text.getText();
        persp = perspReg.findPerspectiveWithLabel(perspName);
        if (persp != null) {
            // Confirm ok to overwrite
            String message = NLS.bind(WorkbenchMessages.SavePerspective_overwriteQuestion,perspName ); 
            String[] buttons = new String[] { IDialogConstants.YES_LABEL,
                    IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
            MessageDialog d = new MessageDialog(this.getShell(),
                    WorkbenchMessages.SavePerspective_overwriteTitle, 
                    null, message, MessageDialog.QUESTION, buttons, 0);

            switch (d.open()) {
            case 0: //yes
                break;
            case 1: //no
                return;
            case 2: //cancel
                cancelPressed();
                return;
            default:
                return;
            }
        }

        super.okPressed();
    }

    /**
     * Notifies that the selection has changed.
     *
     * @param event event object describing the change
     */
    public void selectionChanged(SelectionChangedEvent event) {
        // If a selection is caused by modifyText ignore it.
        if (ignoreSelection) {
			return;
		}

        // Get selection.
        IStructuredSelection sel = (IStructuredSelection) list.getSelection();
        persp = null;
        if (!sel.isEmpty()) {
			persp = (IPerspectiveDescriptor) sel.getFirstElement();
		}

        // Transfer selection to text field.
        if (persp != null) {
            perspName = persp.getLabel();
            text.setText(perspName);
        }

        updateButtons();
    }

    /**
     * Sets the initial selection in this dialog.
     *
     * @param selectedElement the perspective descriptor to select
     */
    public void setInitialSelection(IPerspectiveDescriptor selectedElement) {
        initialSelection = selectedElement;
    }

    /**
     * Update the OK button.
     */
    private void updateButtons() {
        if (okButton != null) {
            String label = text.getText();
            okButton.setEnabled(perspReg.validateLabel(label));
        }
    }
}
