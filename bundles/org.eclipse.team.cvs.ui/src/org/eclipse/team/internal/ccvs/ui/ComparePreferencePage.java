/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.compare.CompareUI;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.team.internal.ui.SWTUtils;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

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
        return CVSUIMessages.ComparePreferencePage_0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
		addField(new BooleanFieldEditor(ICVSUIConstants.PREF_CONSIDER_CONTENTS,
				CVSUIMessages.ComparePreferencePage_4,
				BooleanFieldEditor.DEFAULT, getFieldEditorParent()) {
			// invert the UI
			protected void doLoad() {
				super.doLoad();
				getChangeControl(getFieldEditorParent()).setSelection(!getBooleanValue());
			}
			protected void doLoadDefault() {
				super.doLoadDefault();
				getChangeControl(getFieldEditorParent()).setSelection(!getBooleanValue());
			}
			protected void doStore() {
				getPreferenceStore().setValue(getPreferenceName(), !getBooleanValue());
			}
		});
		addField(new BooleanFieldEditor(
				ICVSUIConstants.PREF_SHOW_COMPARE_REVISION_IN_DIALOG,
				CVSUIMessages.ComparePreferencePage_3,
				BooleanFieldEditor.DEFAULT,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				ICVSUIConstants.PREF_COMMIT_SET_DEFAULT_ENABLEMENT,
				CVSUIMessages.ComparePreferencePage_2,
				BooleanFieldEditor.DEFAULT,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				ICVSUIConstants.PREF_ENABLE_MODEL_SYNC,
				CVSUIMessages.ComparePreferencePage_7,
				BooleanFieldEditor.DEFAULT,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				ICVSUIConstants.PREF_OPEN_COMPARE_EDITOR_FOR_SINGLE_FILE,
				CVSUIMessages.ComparePreferencePage_8,
				BooleanFieldEditor.DEFAULT,
				getFieldEditorParent()));

		IPreferencePageContainer container = getContainer();
		if (container instanceof IWorkbenchPreferenceContainer) {
			IWorkbenchPreferenceContainer workbenchContainer = (IWorkbenchPreferenceContainer) container;
			SWTUtils.createPreferenceLink(workbenchContainer, getFieldEditorParent(),
					CompareUI.PREFERENCE_PAGE_ID, CVSUIMessages.ComparePreferencePage_6);
		}
	}

}
