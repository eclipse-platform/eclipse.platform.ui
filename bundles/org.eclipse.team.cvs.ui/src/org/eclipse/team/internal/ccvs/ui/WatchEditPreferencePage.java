/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;

/**
 * This page contains preferences related to the cvs watch/edit commands
 */
public class WatchEditPreferencePage extends CVSFieldEditorPreferencePage {
	
	private RadioGroupFieldEditor promptEditor;
	private RadioGroupFieldEditor actionEditor;
	private IPreferenceStore store;

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.CVSFieldEditorPreferencePage#getPageHelpContextId()
	 */
	protected String getPageHelpContextId() {
		return IHelpContextIds.WATCH_EDIT_PREFERENCE_PAGE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.CVSFieldEditorPreferencePage#getPageDescription()
	 */
	protected String getPageDescription() {
		return Policy.bind("WatchEditPreferencePage.description"); //$NON-NLS-1$;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
			ICVSUIConstants.PREF_CHECKOUT_READ_ONLY, 
			Policy.bind("WatchEditPreferencePage.checkoutReadOnly"),  //$NON-NLS-1$
			BooleanFieldEditor.DEFAULT, 
			getFieldEditorParent()));
		
		actionEditor = new RadioGroupFieldEditor(
			ICVSUIConstants.PREF_EDIT_ACTION,
			Policy.bind("WatchEditPreferencePage.validateEditSaveAction"), //$NON-NLS-1$
			1,
			new String[][] {{Policy.bind("WatchEditPreferencePage.edit"), ICVSUIConstants.PREF_EDIT_PROMPT_EDIT},  //$NON-NLS-1$
							{Policy.bind("WatchEditPreferencePage.highjack"), ICVSUIConstants.PREF_EDIT_PROMPT_HIGHJACK}, //$NON-NLS-1$
							}, 	//$NON-NLS-1$ //$NON-NLS-2$
			getFieldEditorParent(), true);
		addField(actionEditor);

		
		promptEditor = new RadioGroupFieldEditor(
			ICVSUIConstants.PREF_EDIT_PROMPT,
			Policy.bind("WatchEditPreferencePage.editPrompt"), //$NON-NLS-1$
			1,
			new String[][] {{Policy.bind("WatchEditPreferencePage.alwaysPrompt"), ICVSUIConstants.PREF_EDIT_PROMPT_ALWAYS}, //$NON-NLS-1$
							{Policy.bind("WatchEditPreferencePage.onlyPrompt"), ICVSUIConstants.PREF_EDIT_PROMPT_IF_EDITORS}, //$NON-NLS-1$
							{Policy.bind("WatchEditPreferencePage.neverPrompt"), ICVSUIConstants.PREF_EDIT_PROMPT_NEVER}, //$NON-NLS-1$
							},	//$NON-NLS-1$ //$NON-NLS-2$
			getFieldEditorParent(), true);
		store = getCVSPreferenceStore();
		addField(promptEditor);
	}

	private boolean isEditEnabled() {
		return store.getString(ICVSUIConstants.PREF_EDIT_ACTION).equals(ICVSUIConstants.PREF_EDIT_PROMPT_EDIT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.CVSFieldEditorPreferencePage#pushPreferences()
	 */
	protected void pushPreferences() {
		store = getCVSPreferenceStore();
		Preferences target = CVSProviderPlugin.getPlugin().getPluginPreferences();
		target.setValue(
			CVSProviderPlugin.READ_ONLY,
			store.getBoolean(ICVSUIConstants.PREF_CHECKOUT_READ_ONLY));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == actionEditor) {
			promptEditor.setEnabled(
				event.getNewValue().equals(ICVSUIConstants.PREF_EDIT_PROMPT_EDIT), 
				getFieldEditorParent());
		}
		super.propertyChange(event);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
	 */
	protected void initialize() {
		super.initialize();
		promptEditor.setEnabled(isEditEnabled(), getFieldEditorParent());
	}
}
