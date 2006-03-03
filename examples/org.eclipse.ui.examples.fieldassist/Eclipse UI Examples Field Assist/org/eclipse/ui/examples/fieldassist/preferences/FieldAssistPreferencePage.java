package org.eclipse.ui.examples.fieldassist.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
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

public class FieldAssistPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Create the FieldAssistPreferencePage
	 */
	public FieldAssistPreferencePage() {
		super(GRID);
		setPreferenceStore(FieldAssistPlugin.getDefault().getPreferenceStore());
		setDescription(TaskAssistExampleMessages.Preferences_Description);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and
	 */
	public void createFieldEditors() {
		Label label = new Label(getFieldEditorParent(), SWT.WRAP);
		label.setText(TaskAssistExampleMessages.Preferences_ErrorIndicator);
		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWERRORMESSAGE,
				TaskAssistExampleMessages.Preferences_ShowErrorMessage,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWERRORDECORATION,
				TaskAssistExampleMessages.Preferences_ShowErrorDecorator,
				getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWERRORCOLOR,
				TaskAssistExampleMessages.Preferences_ShowErrorColor,
				getFieldEditorParent()));
		
		label = new Label(getFieldEditorParent(), SWT.WRAP);

		label = new Label(getFieldEditorParent(), SWT.WRAP);
		label.setText(TaskAssistExampleMessages.Preferences_RequiredFieldIndicator);
		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWREQUIREDFIELDDECORATION,
				TaskAssistExampleMessages.Preferences_ShowRequiredFieldDecorator,
				getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWREQUIREDFIELDCOLOR,
				TaskAssistExampleMessages.Preferences_ShowRequiredFieldColor,
				getFieldEditorParent()));

		label = new Label(getFieldEditorParent(), SWT.WRAP);
		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWWARNINGDECORATION,
				TaskAssistExampleMessages.Preferences_ShowWarningDecorator,
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceConstants.PREF_SHOWCONTENTPROPOSALCUE,
				TaskAssistExampleMessages.Preferences_ShowProposalCue,
				getFieldEditorParent()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}