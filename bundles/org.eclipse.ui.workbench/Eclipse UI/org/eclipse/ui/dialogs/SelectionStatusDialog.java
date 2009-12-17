/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *     font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.MessageLine;

/**
 * An abstract base class for dialogs with a status bar and ok/cancel buttons.
 * The status message must be passed over as StatusInfo object and can be
 * an error, warning or ok. The OK button is enabled or disabled depending
 * on the status.
 * 
 * @since 2.0
 */
public abstract class SelectionStatusDialog extends SelectionDialog {

    private MessageLine fStatusLine;

    private IStatus fLastStatus;

    private Image fImage;

    private boolean fStatusLineAboveButtons = false;

    /**
     * Creates an instance of a <code>SelectionStatusDialog</code>.
     * @param parent
     */
    public SelectionStatusDialog(Shell parent) {
        super(parent);
    }

    /**
     * Controls whether status line appears to the left of the buttons (default)
     * or above them.
     *
     * @param aboveButtons if <code>true</code> status line is placed above buttons; if
     * 	<code>false</code> to the right
     */
    public void setStatusLineAboveButtons(boolean aboveButtons) {
        fStatusLineAboveButtons = aboveButtons;
    }

    /**
     * Sets the image for this dialog.
     * @param image the image.
     */
    public void setImage(Image image) {
        fImage = image;
    }

    /**
     * Returns the first element from the list of results. Returns <code>null</code>
     * if no element has been selected.
     *
     * @return the first result element if one exists. Otherwise <code>null</code> is
     *  returned.
     */
    public Object getFirstResult() {
        Object[] result = getResult();
        if (result == null || result.length == 0) {
			return null;
		}
        return result[0];
    }

    /**
     * Sets a result element at the given position.
     * @param position
     * @param element
     */
    protected void setResult(int position, Object element) {
        Object[] result = getResult();
        result[position] = element;
        setResult(Arrays.asList(result));
    }

    /**
     * Compute the result and return it.
     */
    protected abstract void computeResult();

    /*
     * @see Window#configureShell(shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (fImage != null) {
			shell.setImage(fImage);
		}
    }

    /**
     * Update the dialog's status line to reflect the given status. It is safe to call
     * this method before the dialog has been opened.
     * @param status
     */
    protected void updateStatus(IStatus status) {
        fLastStatus = status;
        if (fStatusLine != null && !fStatusLine.isDisposed()) {
            updateButtonsEnableState(status);
            fStatusLine.setErrorStatus(status);
        }
    }

    /**
     * Update the status of the ok button to reflect the given status. Subclasses
     * may override this method to update additional buttons.
     * @param status
     */
    protected void updateButtonsEnableState(IStatus status) {
        Button okButton = getOkButton();
        if (okButton != null && !okButton.isDisposed()) {
			okButton.setEnabled(!status.matches(IStatus.ERROR));
		}
    }

    /*
     * @see Dialog#okPressed()
     */
    protected void okPressed() {
        computeResult();
        super.okPressed();
    }

    /*
     * @see Window#create()
     */
    public void create() {
        super.create();
        if (fLastStatus != null) {
			updateStatus(fLastStatus);
		}
    }

    /*
     * @see Dialog#createButtonBar(Composite)
     */
    protected Control createButtonBar(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        if (!fStatusLineAboveButtons) {
            layout.numColumns = 2;
        }
        layout.marginHeight = 0;
        layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);

        if (!fStatusLineAboveButtons && isHelpAvailable()) {
        	createHelpControl(composite);
        }
        fStatusLine = new MessageLine(composite);
        fStatusLine.setAlignment(SWT.LEFT);
        GridData statusData = new GridData(GridData.FILL_HORIZONTAL);
        fStatusLine.setErrorStatus(null);
        fStatusLine.setFont(font);
        if (fStatusLineAboveButtons && isHelpAvailable()) {
        	statusData.horizontalSpan = 2;
        	createHelpControl(composite);
        }
        fStatusLine.setLayoutData(statusData);

		/*
		 * Create the rest of the button bar, but tell it not to
		 * create a help button (we've already created it).
		 */
		boolean helpAvailable = isHelpAvailable();
		setHelpAvailable(false);
		super.createButtonBar(composite);
		setHelpAvailable(helpAvailable);
        return composite;
    }

}
