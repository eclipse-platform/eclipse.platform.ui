/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A page that is used to indicate an error in loading a page within the
 * workbench.
 * 
 * @since 3.0
 */
public class ErrorPreferencePage extends EmptyPreferencePage {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Text text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
        text.setForeground(JFaceColors.getErrorText(text.getDisplay()));
        text.setBackground(text.getDisplay().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));
        text.setText(WorkbenchMessages
                .getString("ErrorPreferencePage.errorMessage")); //$NON-NLS-1$
        return text;
    }
}