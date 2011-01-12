/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.1
 */
public class StatusPart {
    
	private static final String LOG_VIEW_ID = "org.eclipse.pde.runtime.LogView"; //$NON-NLS-1$
    boolean showingDetails = false;
    private Button detailsButton;
    private Composite detailsArea;
    private Control details = null;
    private IStatus reason;
    
    public StatusPart(final Composite parent, IStatus reason_) {
    	Color bgColor= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		Color fgColor= parent.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    	
		parent.setBackground(bgColor);
		parent.setForeground(fgColor);

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
        imageLabel.setBackground(bgColor);
        Image image = getImage();
        if (image != null) {
            image.setBackground(bgColor);
            imageLabel.setImage(image);
            imageLabel.setLayoutData(new GridData(
                    GridData.HORIZONTAL_ALIGN_CENTER
                            | GridData.VERTICAL_ALIGN_BEGINNING));
        }
        
        Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        text.setBackground(bgColor);
        text.setForeground(fgColor);

        //text.setForeground(JFaceColors.getErrorText(text.getDisplay()));
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text.setText(reason.getMessage());
        
        Composite buttonParent = new Composite(parent, SWT.NONE);
        buttonParent.setBackground(parent.getBackground());
        GridLayout buttonsLayout = new GridLayout();
        buttonsLayout.numColumns = 2;
        buttonsLayout.marginHeight = 0;
        buttonsLayout.marginWidth  = 0;
        buttonsLayout.horizontalSpacing = 0;
		buttonParent.setLayout(buttonsLayout);
        
        
        detailsButton = new Button(buttonParent, SWT.PUSH);
        detailsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                showDetails(!showingDetails);
            }
        });
        
        detailsButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
        detailsButton.setVisible(reason.getException() != null);
        
        createShowLogButton(buttonParent);
        
        updateDetailsText();
        
        detailsArea = new Composite(parent, SWT.NONE);
        detailsArea.setBackground(bgColor);
        detailsArea.setForeground(fgColor);
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
     * @return the image
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
                    | SWT.MULTI | SWT.READ_ONLY | SWT.LEFT_TO_RIGHT);
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
    
    private void createShowLogButton(Composite parent){
		IViewDescriptor descriptor = PlatformUI.getWorkbench().getViewRegistry()
				.find(LOG_VIEW_ID);
		if (descriptor == null) {
			return;
		}
		Button button = new Button(parent, SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(LOG_VIEW_ID);
				} catch (CoreException ce) {
					StatusManager.getManager().handle(ce,
							WorkbenchPlugin.PI_WORKBENCH);
				}
			}
		});
		final Image image = descriptor.getImageDescriptor().createImage();
		button.setImage(image);
		button.setToolTipText(WorkbenchMessages.ErrorLogUtil_ShowErrorLogTooltip);
		button.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});
    }
}
