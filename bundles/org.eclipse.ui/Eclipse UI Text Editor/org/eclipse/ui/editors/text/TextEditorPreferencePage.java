package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.DialogPageContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * A preference page to set the font used in the default text editor.
 * This preference page uses the text editor's preference bundle and
 * uses the key <code>"PreferencePage.description"</code> to look up
 * the page description. In addition, it uses <code>"PreferencePage.fontEditor"</code>
 * for the editor description.
 */
public class TextEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	
	private static boolean fgInitialized= false;
	
	/**
	 * Create the preference page.
	 */
	public TextEditorPreferencePage() {
		super(GRID);
		
		setDescription(TextEditorMessages.getString("PreferencePage.description"));		 //$NON-NLS-1$
		Plugin plugin= Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin= (AbstractUIPlugin) plugin;
			setPreferenceStore(uiPlugin.getPreferenceStore());
		}
	}
	
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), new DialogPageContextComputer(this, ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE));
	}	
	
	/*
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	public void createFieldEditors() {
		addField(new FontFieldEditor(AbstractTextEditor.PREFERENCE_FONT, TextEditorMessages.getString("PreferencePage.fontEditor"), getFieldEditorParent())); //$NON-NLS-1$
	}

	/*
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	
	public static void initDefaults(IPreferenceStore store) {
		
		if (fgInitialized)
			return;
			
		fgInitialized= true;
		
		Font font= JFaceResources.getTextFont();
		if (font != null) {
			FontData[] data= font.getFontData();
			if (data != null && data.length > 0)
				PreferenceConverter.setDefault(store, AbstractTextEditor.PREFERENCE_FONT, data[0]);
		}
		
		Display display= Display.getDefault();
		Color color= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND, color.getRGB());
		
		color= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		PreferenceConverter.setDefault(store,  AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, color.getRGB());
	}
}
