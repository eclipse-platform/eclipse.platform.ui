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
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page for configuring CVS comparisons
 */
public class ComparePreferencePage extends CVSFieldEditorPreferencePage {

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.CVSFieldEditorPreferencePage#getPageHelpContextId()
     */
    protected String getPageHelpContextId() {
        return IHelpContextIds.COMPARE_PREFERENCE_PAGE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.CVSFieldEditorPreferencePage#getPageDescription()
     */
    protected String getPageDescription() {
        return Policy.bind("ComparePreferencePage.0"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
		        ICVSUIConstants.PREF_CONSIDER_CONTENTS, 
				Policy.bind("ComparePreferencePage.4"),  //$NON-NLS-1$
				BooleanFieldEditor.DEFAULT, 
				getFieldEditorParent()) {
            protected Button getChangeControl(Composite parent) {
                Button button = super.getChangeControl(parent);
                WorkbenchHelp.setHelp(button, IHelpContextIds.PREF_CONSIDER_CONTENT);
                return button;
            }
		});
		addField(new BooleanFieldEditor(
		        ICVSUIConstants.PREF_SHOW_COMPARE_REVISION_IN_DIALOG, 
		        Policy.bind("ComparePreferencePage.3"),  //$NON-NLS-1$
				BooleanFieldEditor.DEFAULT, 
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
		        ICVSUIConstants.PREF_SHOW_AUTHOR_IN_EDITOR, 
		        Policy.bind("ComparePreferencePage.1"), //$NON-NLS-1$
				BooleanFieldEditor.DEFAULT, 
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
		        ICVSUIConstants.PREF_COMMIT_SET_DEFAULT_ENABLEMENT, 
		        Policy.bind("ComparePreferencePage.2"), //$NON-NLS-1$
				BooleanFieldEditor.DEFAULT, 
				getFieldEditorParent()));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.ui.CVSFieldEditorPreferencePage#pushPreferences()
     */
    protected void pushPreferences() {
        super.pushPreferences();
    }
}
