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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public class SWTUtils {

    public static GridData createGridData(int width, int height, boolean hFill, boolean vFill) {
        return createGridData(width, height, hFill ? SWT.FILL : SWT.BEGINNING, vFill ? SWT.FILL : SWT.BEGINNING, hFill, vFill);
    }
    
    public static GridData createGridData(int width, int height, int hAlign, int vAlign, boolean hGrab, boolean vGrab) {
        final GridData gd= new GridData(hAlign, vAlign, hGrab, vGrab);
        gd.widthHint= width;
        gd.heightHint= height;
        return gd;
    }

    
    public static GridData createHFillGridData() {
        return createGridData(0, SWT.DEFAULT, true, false);
    }
    
    public static GridData createHFillGridData(int span) {
        final GridData gd= createGridData(0, SWT.DEFAULT, true, false);
        gd.horizontalSpan= span;
        return gd;
    }
    
    public static GridData createHVFillGridData() {
        return createGridData(0, 0, true, true);
    }
    
    public static Composite createGridComposite(Composite parent, int numColumns) {
        final Composite composite= new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(numColumns, false));
        return composite;
    }
    
    public static GridLayout createGridLayout(int numColumns, int marginWidth, int marginHeight) {
        final GridLayout layout= new GridLayout(numColumns, false);
        layout.marginWidth= marginWidth;
        layout.marginHeight= marginHeight;
        return layout;
    }
    
}
