package org.eclipse.ui.examples.templateeditor.preferences;

import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.examples.templateeditor.editors.TemplateEditorUI;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * @see PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage implements IWorkbenchPreferencePage {
	
	public TemplatesPreferencePage() {
		setPreferenceStore(TemplateEditorUI.getDefault().getPreferenceStore());
		setTemplateStore(TemplateEditorUI.getDefault().getTemplateStore());
		setContextTypeRegistry(TemplateEditorUI.getDefault().getContextTypeRegistry());
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}
	
	
	public boolean performOk() {
		boolean ok= super.performOk();
		
		TemplateEditorUI.getDefault().savePluginPreferences();
		
		return ok;
	}
}
