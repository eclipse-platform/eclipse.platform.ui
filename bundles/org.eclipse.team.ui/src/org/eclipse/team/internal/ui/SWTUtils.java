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

package org.eclipse.team.internal.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public class SWTUtils {
	
	public static final int MARGINS_DEFAULT= -1;
	public static final int MARGINS_NONE= 0;
	public static final int MARGINS_DIALOG= 1;
	
    public static GridData createGridData(int width, int height, boolean hFill, boolean vFill) {
        return createGridData(width, height, hFill ? SWT.FILL : SWT.BEGINNING, vFill ? SWT.FILL : SWT.TOP, hFill, vFill);
    }
    
    public static GridData createGridData(int width, int height, int hAlign, int vAlign, boolean hGrab, boolean vGrab) {
        final GridData gd= new GridData(hAlign, vAlign, hGrab, vGrab);
        gd.widthHint= width;
        gd.heightHint= height;
        return gd;
    }

    public static GridData createHFillGridData() {
        return createHFillGridData(1);
    }
    
    public static GridData createHFillGridData(int span) {
        final GridData gd= createGridData(0, SWT.DEFAULT, true, false);
        gd.horizontalSpan= span;
        return gd;
    }
    
    public static GridData createHVFillGridData() {
        return createHVFillGridData(1);
    }
    
    public static GridData createHVFillGridData(int span) {
        final GridData gd= createGridData(0, 0, true, true);
        gd.horizontalSpan= span;
        return gd;
    }

    
    /**
	 * Create a grid layout with the specified number of columns and the
	 * standard spacings.
	 * 
	 * @param numColumns
	 *                the number of columns
	 * @param converter
	 *                the pixel converter
	 * @param margins
	 *                One of <code>MARGINS_DEFAULT</code>,
	 *                <code>MARGINS_NONE</code> or <code>MARGINS_DIALOG</code>.
	 * @return the grid layout
	 */
    public static GridLayout createGridLayout(int numColumns, PixelConverter converter, int margins) {
    	Assert.isTrue(margins == MARGINS_DEFAULT || margins == MARGINS_NONE || margins == MARGINS_DIALOG);
    	
        final GridLayout layout= new GridLayout(numColumns, false);
        layout.horizontalSpacing= converter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing= converter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        
        switch (margins) {
        case MARGINS_NONE:
            layout.marginWidth= 0;
            layout.marginHeight= 0;
            break;
        case MARGINS_DIALOG:
            layout.marginWidth= converter.convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
            layout.marginHeight= converter.convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
            break;
        case MARGINS_DEFAULT:
        	// nop
        }
        return layout;
    }
    
    
    public static Label createLabel(Composite parent, String message) {
        return createLabel(parent, message, 1);
    }

    public static Label createLabel(Composite parent, String message, int span) {
        final Label label= new Label(parent, SWT.WRAP);
        label.setText(message);
        label.setLayoutData(createHFillGridData(span));
        return label;
    }
    
    public static Button createCheckBox(Composite parent, String message) {
        return createCheckBox(parent, message, 1);
    }

    public static Button createCheckBox(Composite parent, String message, int span) {
        final Button button= new Button(parent, SWT.CHECK);
        button.setText(message);
        button.setLayoutData(createHFillGridData(span));
        return button;
    }
    
    public static Control createPlaceholder(Composite parent, PixelConverter converter, int heightInChars) {
    	Assert.isTrue(heightInChars > 0);
    	final Control placeHolder= new Composite(parent, SWT.NONE);
    	final GridData gd= new GridData(SWT.BEGINNING, SWT.TOP, false, false);
    	gd.heightHint= converter.convertHeightInCharsToPixels(heightInChars);
    	placeHolder.setLayoutData(gd);
    	return placeHolder;
    }
    
    public static PixelConverter createDialogPixelConverter(Control control) {
    	Dialog.applyDialogFont(control);
    	return new PixelConverter(control);
    }
    
	public static int calculateButtonSize(PixelConverter converter, Button [] buttons) {
		int minimum= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		for (int i = 0; i < buttons.length; i++) {
			final int length= buttons[i].computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
			if (minimum < length)
				minimum= length;
		}
		return minimum;
	}
}
