/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Adds a new file association to a content type.  
 * 
 * @since 3.1
 */
public class NewContentTypeFileAssociationDialog extends Dialog {

    private IContentType contentType;
    private Text textArea;
    private Button extButton;

    /**
     * @param parentShell
     * @param contentType 
     */
    public NewContentTypeFileAssociationDialog(Shell parentShell, IContentType contentType) {
        super(parentShell);
        this.contentType = contentType;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);
        extButton = new Button(composite, SWT.RADIO);
        extButton.setText("File &Extension"); //$NON-NLS-1$
        extButton.setFont(parent.getFont());
        Button nameButton = new Button(composite, SWT.RADIO);
        nameButton.setText("File &Name"); //$NON-NLS-1$
        nameButton.setFont(parent.getFont());
        
        Label label = new Label(composite, SWT.NONE);
        label.setFont(parent.getFont());
        label.setText("&Value:"); //$NON-NLS-1$
        textArea = new Text(composite, SWT.SINGLE | SWT.BORDER);
        textArea.setFont(parent.getFont());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        textArea.setLayoutData(data);
        return composite;
    }
    
    private String getText() {
        return textArea.getText();
    }
    
    private boolean isFileExtension() {
        return extButton.getSelection();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Add New File Association"); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        try {
            contentType.addFileSpec(getText(), isFileExtension() ? IContentType.FILE_EXTENSION_SPEC : IContentType.FILE_NAME_SPEC);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        super.okPressed();
    }
}
