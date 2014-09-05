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
package org.eclipse.ui.tests.dialogs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The Field Editor Preference page is a test of the font field
 * editors with and without previewers.
 */
public class FontFieldEditorTestPreferencePage extends
        FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /**
     * Create the preference page.
     */
    public FontFieldEditorTestPreferencePage() {
        super(GRID);
    }

    /**
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
	protected void createFieldEditors() {

        Composite feParent = getFieldEditorParent();

        for (int i = 0; i < 3; i++) {
            //Create one with a preview
            addField(new FontFieldEditor("FontValue" + String.valueOf(i),
                    "Font Test" + String.valueOf(i), "Preview", feParent));

            //Create one without
            addField(new FontFieldEditor(
                    "FontValueDefault" + String.valueOf(i), "Font Test Default"
                            + String.valueOf(i), feParent));
        }

    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
	public void init(IWorkbench workbench) {
    }

}
