/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.fieldassist.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.examples.fieldassist.FieldAssistPlugin;
import org.eclipse.ui.examples.fieldassist.TaskAssistExampleMessages;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class ContentAssistPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 *  Create a ContentAssistPreferencePage
	 */
	public ContentAssistPreferencePage() {
		super(GRID);
		setPreferenceStore(FieldAssistPlugin.getDefault().getPreferenceStore());
		setDescription(TaskAssistExampleMessages.Preferences_ContentAssistDescription);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and
	 */
	@Override
	public void createFieldEditors() {
		addField(new RadioGroupFieldEditor(
				PreferenceConstants.PREF_CONTENTASSISTKEY,
				TaskAssistExampleMessages.Preferences_ContentAssistKey,
				1,
				new String[][] {
						{ PreferenceConstants.PREF_CONTENTASSISTKEY1,
								PreferenceConstants.PREF_CONTENTASSISTKEY1 },
						{ PreferenceConstants.PREF_CONTENTASSISTKEY2,
								PreferenceConstants.PREF_CONTENTASSISTKEY2 },
						{ PreferenceConstants.PREF_CONTENTASSISTKEYAUTO,
								PreferenceConstants.PREF_CONTENTASSISTKEYAUTO },
						{ PreferenceConstants.PREF_CONTENTASSISTKEYAUTOSUBSET,
								PreferenceConstants.PREF_CONTENTASSISTKEYAUTOSUBSET }, },
				getFieldEditorParent()));

		IntegerFieldEditor editor = new IntegerFieldEditor(
				PreferenceConstants.PREF_CONTENTASSISTDELAY,
				TaskAssistExampleMessages.Preferences_ContentAssistDelay,
				getFieldEditorParent());
		editor.setValidRange(0, 10000);
		addField(editor);

		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_CONTENTASSISTKEY_PROPAGATE,
				TaskAssistExampleMessages.Preferences_ContentAssistKeyPropagate,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWSECONDARYPOPUP,
				TaskAssistExampleMessages.Preferences_ShowSecondaryPopup,
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.PREF_CONTENTASSISTRESULT,
				TaskAssistExampleMessages.Preferences_ContentAssistResult,
				1,
				new String[][] {
						{
								TaskAssistExampleMessages.Preferences_ContentAssistResultReplace,
								PreferenceConstants.PREF_CONTENTASSISTRESULT_REPLACE },
						{
								TaskAssistExampleMessages.Preferences_ContentAssistResultInsert,
								PreferenceConstants.PREF_CONTENTASSISTRESULT_INSERT },
						{
								TaskAssistExampleMessages.Preferences_ContentAssistResultNone,
								PreferenceConstants.PREF_CONTENTASSISTRESULT_NONE } },
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.PREF_CONTENTASSISTFILTER,
				TaskAssistExampleMessages.Preferences_ContentAssistFilter,
				1,
				new String[][] {
						{
								TaskAssistExampleMessages.Preferences_ContentAssistFilterCharacter,
								PreferenceConstants.PREF_CONTENTASSISTFILTER_CHAR },
						{
								TaskAssistExampleMessages.Preferences_ContentAssistFilterCumulative,
								PreferenceConstants.PREF_CONTENTASSISTFILTER_CUMULATIVE },
						{
								TaskAssistExampleMessages.Preferences_ContentAssistFilterNone,
								PreferenceConstants.PREF_CONTENTASSISTFILTER_NONE } },
				getFieldEditorParent()));

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}