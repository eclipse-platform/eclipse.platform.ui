package org.eclipse.ui.internal.dialogs;

import java.util.Locale;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class FontPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	/*
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	public void createFieldEditors() {

		Composite editorParent = getFieldEditorParent();

		createFieldEditor(
			JFaceResources.getBannerFont(),
			JFaceResources.BANNER_FONT,
			"Banner Font: ",
			editorParent);
		createFieldEditor(
			JFaceResources.getDialogFont(),
			JFaceResources.DIALOG_FONT,
			"Dialog Font: ",
			editorParent);
		createFieldEditor(
			JFaceResources.getTerminalFont(),
			JFaceResources.TERMINAL_FONT,
			"Terminal Font: ",
			editorParent);
		createFieldEditor(
			JFaceResources.getTextFont(),
			JFaceResources.TEXT_FONT,
			"Text Font: ",
			editorParent);
	}

	/**
	 * Create the preference page.
	 */
	public FontPreferencePage() {
		super(GRID);

		Plugin plugin = Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin = (AbstractUIPlugin) plugin;
			setPreferenceStore(uiPlugin.getPreferenceStore());
		}
	}

	/**
	 * Create a field editor for the setting. Also initialize 
	 * the setting to the current font
	 */

	private void createFieldEditor(
		Font currentSetting,
		String preferenceName,
		String title,
		Composite editorParent) {

		addField(new FontFieldEditor(preferenceName, title, editorParent));
	}

	/*
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * Set the value for the font in the registry based on the 
	 * current value at preferenceName.
	 */

	private void setRegistryValue(String preferenceKey) {
		FontData[] data = new FontData[1];
		data[0] = PreferenceConverter.getFontData(getPreferenceStore(), preferenceKey);

		//Only update if there has been a change
		FontData[] currentSetting =
			JFaceResources.getFontRegistry().get(preferenceKey).getFontData();
		if (currentSetting[0].equals(data[0]))
			return;

		JFaceResources.getFontRegistry().put(preferenceKey, data);
	}

	/** 
	* The font editor preference page implementation of this 
	* <code>PreferencePage</code> method saves all field editors by
	* calling <code>FieldEditor.store</code>. It then will
	* update the entries in the FontRegistry with the new
	* values.
	*
	* @see FieldEditor#store()
	*/
	public boolean performOk() {

		if (super.performOk()) {
			setRegistryValue(JFaceResources.BANNER_FONT);
			setRegistryValue(JFaceResources.DIALOG_FONT);
			setRegistryValue(JFaceResources.TERMINAL_FONT);
			setRegistryValue(JFaceResources.TEXT_FONT);
			return true;
		}
		return false;

	}

}