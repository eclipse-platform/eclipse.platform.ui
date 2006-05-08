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
package org.eclipse.ui.examples.readmetool;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * This dialog is an example of a detached window launched
 * from an action in the workbench.
 */
public class SectionsDialog extends Dialog {
    protected IAdaptable input;

    /**
     * Creates a new SectionsDialog.
     */
    public SectionsDialog(Shell parentShell, IAdaptable input) {
        super(parentShell);
        this.input = input;
    }

    /* (non-Javadoc)
     * Method declared on Window.
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(MessageUtil.getString("Readme_Sections")); //$NON-NLS-1$
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				IReadmeConstants.SECTIONS_DIALOG_CONTEXT);
    }

    /* (non-Javadoc)
     * Method declared on Dialog
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        List list = new List(composite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        list.setLayoutData(data);
        ListViewer viewer = new ListViewer(list);
        viewer.setContentProvider(new WorkbenchContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setInput(input);

        return composite;
    }
}
