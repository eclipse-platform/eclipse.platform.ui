package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class FontPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private ArrayList editors = new ArrayList();

	/*
	 * @see PreferencePage#createContents
	 */
	public Control createContents(Composite parent) {

		Composite editorParent = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorParent.setLayout(layout);

		createFieldEditor(
			JFaceResources.BANNER_FONT,
			WorkbenchMessages.getString("FontsPreference.BannerFont"),
			JFaceResources.getString("openChange"),
			editorParent);
		createFieldEditor(
			JFaceResources.DIALOG_FONT,
			WorkbenchMessages.getString("FontsPreference.DialogFont"),
			JFaceResources.getString("openChange2"),
			editorParent);
		createFieldEditor(
			JFaceResources.HEADER_FONT,
			WorkbenchMessages.getString("FontsPreference.HeaderFont"),
			JFaceResources.getString("openChange3"),
			editorParent);
		createFieldEditor(
			JFaceResources.TEXT_FONT,
			WorkbenchMessages.getString("FontsPreference.TextFont"),
			JFaceResources.getString("openChange4"),
			editorParent);

		return editorParent;
	}

	/**
	 * Create the preference page.
	 */
	public FontPreferencePage() {

		Plugin plugin = Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin = (AbstractUIPlugin) plugin;
			setPreferenceStore(uiPlugin.getPreferenceStore());
		}
	}

	/**
	 * Create a field editor for the setting. 
	 */

	private void createFieldEditor(
		String preferenceName,
		String title,
		String changeButtonLabel,
		Composite editorParent) {
		editors.add(
			new BorderedFontFieldEditor(
				preferenceName,
				title,
				WorkbenchMessages.getString("FontsPreference.SampleText"),
				changeButtonLabel,
				editorParent,
				getPreferenceStore()));
	}

	/*
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see IWorkbenchPreferencePage#performDefaults
	*/
	protected void performDefaults() {

		Iterator editorIterator = editors.iterator();
		while (editorIterator.hasNext()) {
			BorderedFontFieldEditor editor =
				(BorderedFontFieldEditor) editorIterator.next();
			editor.loadDefault();
		}
		super.performDefaults();
	}

	/*
	 * @see IWorkbenchPreferencePage#performDefaults
	*/
	public boolean performOk() {
		Iterator editorIterator = editors.iterator();
		while (editorIterator.hasNext()) {
			BorderedFontFieldEditor editor =
				(BorderedFontFieldEditor) editorIterator.next();
			editor.store();
		}
		return super.performOk();
	}

}