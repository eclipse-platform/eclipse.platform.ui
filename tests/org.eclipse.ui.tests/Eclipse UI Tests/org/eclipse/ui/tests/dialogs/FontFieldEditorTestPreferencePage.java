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
public class FontFieldEditorTestPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	/**
	 * Create the preference page.
	 */
	public FontFieldEditorTestPreferencePage() {
		super(GRID);
	}

	/**
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {

		Composite feParent = getFieldEditorParent();

		for (int i = 0; i < 3; i++) {
			//Create one with a preview
			addField(
				new FontFieldEditor(
					"FontValue" + String.valueOf(i),
					"Font Test" + String.valueOf(i),
					"Preview",
					feParent));

			//Create one without
			addField(
				new FontFieldEditor(
					"FontValueDefault" + String.valueOf(i),
					"Font Test Default" + String.valueOf(i),
					feParent));
		}

	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
