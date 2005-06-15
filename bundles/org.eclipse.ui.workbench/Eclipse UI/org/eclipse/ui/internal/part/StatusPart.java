/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.1
 */
public class StatusPart {
    
    boolean showingDetails = false;
    private Button detailsButton;
    private Composite detailsArea;
    private Control details = null;
    private IStatus reason;
    
    public StatusPart(final Composite parent, IStatus reason_) {
        this.reason = reason_;
        GridLayout layout = new GridLayout();
        
        layout.numColumns = 3;
        
        int spacing = 8;
        int margins = 8;
        layout.marginBottom = margins;
        layout.marginTop = margins;
        layout.marginLeft = margins;
        layout.marginRight = margins;
        layout.horizontalSpacing = spacing;
        layout.verticalSpacing = spacing;
        parent.setLayout(layout);
        
        Label imageLabel = new Label(parent, SWT.NONE);
        Image image = getImage();
        if (image != null) {
            image.setBackground(imageLabel.getBackground());
            imageLabel.setImage(image);
            imageLabel.setLayoutData(new GridData(
                    GridData.HORIZONTAL_ALIGN_CENTER
                            | GridData.VERTICAL_ALIGN_BEGINNING));
        }
        
        Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        text.setBackground(text.getDisplay().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));

        //text.setForeground(JFaceColors.getErrorText(text.getDisplay()));
        text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
        text.setText(reason.getMessage()); 
        
        detailsButton = new Button(parent, SWT.PUSH);
        detailsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showDetails(!showingDetails);
            }
        });
        
        detailsButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
        detailsButton.setVisible(reason.getException() != null);
        
        updateDetailsText();
        
        detailsArea = new Composite(parent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        data.verticalSpan = 1;
        detailsArea.setLayoutData(data);
        detailsArea.setLayout(new FillLayout());
        parent.layout(true);
    }
    
    /**
     * Return the image for the upper-left corner of this part
     *
     * @return
     */
    private Image getImage() {
        Display d = Display.getCurrent();
        
        switch(reason.getSeverity()) {
        case IStatus.ERROR: 
            return d.getSystemImage(SWT.ICON_ERROR);
        case IStatus.WARNING:
            return d.getSystemImage(SWT.ICON_WARNING);
        default:
            return d.getSystemImage(SWT.ICON_INFORMATION);
        }
    }
    
    private void showDetails(boolean shouldShow) {
        if (shouldShow == showingDetails) {
            return;
        }
        this.showingDetails = shouldShow;
        updateDetailsText();
    }
    
    private void updateDetailsText() {
        if (details != null) {
            details.dispose();
            details = null;
        }
        
        if (showingDetails) {
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
            Text detailsText = new Text(detailsArea, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL 
                    | SWT.MULTI | SWT.READ_ONLY);
            detailsText.setText(getDetails(reason));
            detailsText.setBackground(detailsText.getDisplay().getSystemColor(
                    SWT.COLOR_LIST_BACKGROUND));
            details = detailsText;
            detailsArea.layout(true);
        } else {
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        }
    }

    
    private String getDetails(IStatus status) {
        if (status.getException() != null) {
            return getStackTrace(status.getException());
        }
        
        return ""; //$NON-NLS-1$
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter swriter = new StringWriter();
        PrintWriter pwriter = new PrintWriter(swriter);
        throwable.printStackTrace(pwriter);
        pwriter.flush();
        pwriter.close();
        return swriter.toString();
    }
}
