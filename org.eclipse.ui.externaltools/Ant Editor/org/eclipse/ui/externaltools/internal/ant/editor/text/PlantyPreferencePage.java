package org.eclipse.ui.externaltools.internal.ant.editor.text;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;


public class PlantyPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
		
	public PlantyPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
        setDescription("Ant Editor Settings:");
	}

	protected void createFieldEditors() {
        addSourceColorFields();
	}

    private void addSourceColorFields() {
        addField(
            new ColorFieldEditor(
                PlantyColorConstants.P_DEFAULT,
                "&Text:",
                getFieldEditorParent()));
        addField(
            new ColorFieldEditor(
                PlantyColorConstants.P_PROC_INSTR,
                "Pr&ocessing Instructions:",
                getFieldEditorParent()));
        addField(
            new ColorFieldEditor(
                PlantyColorConstants.P_STRING,
                "&Constant Strings:",
                getFieldEditorParent()));
        addField(
            new ColorFieldEditor(
                PlantyColorConstants.P_TAG,
                "Ta&gs:",
                getFieldEditorParent()));
        addField(
            new ColorFieldEditor(
                PlantyColorConstants.P_XML_COMMENT,
                "Co&mments:",
                getFieldEditorParent()));
    }

	public void init(IWorkbench workbench) {
	}

    public boolean performOk() {
        boolean value = super.performOk();
		ExternalToolsPlugin.getDefault().savePluginPreferences();
        return value;
    }


}