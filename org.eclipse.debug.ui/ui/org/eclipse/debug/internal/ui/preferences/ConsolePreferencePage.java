package org.eclipse.debug.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

/**
 * A page to set the preferences for the console
 */
public class ConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {
	/**
	 * Create the console page.
	 */
	public ConsolePreferencePage() {
		super(GRID);
		setDescription(DebugPreferencesMessages.getString("ConsolePreferencePage.Console_settings")); //$NON-NLS-1$
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			parent,
			IDebugHelpContextIds.CONSOLE_PREFERENCE_PAGE );
	}
	
	/**
	 * Create all field editors for this page
	 */
	public void createFieldEditors() {
		
		addField(new BooleanFieldEditor(CONSOLE_OPEN_ON_OUT, DebugPreferencesMessages.getString("ConsolePreferencePage.Show_&Console_View_when_there_is_program_output_3"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(CONSOLE_OPEN_ON_ERR, DebugPreferencesMessages.getString("ConsolePreferencePage.Show_&Console_View_when_there_is_program_error_3"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$

		// Note: first String value is the key for the preference bundle and second the
		// second String value is the label displayed in front of the editor.
		ColorFieldEditor sysout= new ColorFieldEditor(CONSOLE_SYS_OUT_RGB, DebugPreferencesMessages.getString("ConsolePreferencePage.Standard_Out__2"), getFieldEditorParent()); //$NON-NLS-1$
		ColorFieldEditor syserr= new ColorFieldEditor(CONSOLE_SYS_ERR_RGB, DebugPreferencesMessages.getString("ConsolePreferencePage.Standard_Error__3"), getFieldEditorParent()); //$NON-NLS-1$
		ColorFieldEditor sysin= new ColorFieldEditor(CONSOLE_SYS_IN_RGB, DebugPreferencesMessages.getString("ConsolePreferencePage.Standard_In__4"), getFieldEditorParent()); //$NON-NLS-1$
		
		WorkbenchChainedTextFontFieldEditor editor= new WorkbenchChainedTextFontFieldEditor(CONSOLE_FONT,
				DebugPreferencesMessages.getString("ConsolePreferencePage.Console_font_setting___5"), getFieldEditorParent()); //$NON-NLS-1$
		
		addField(sysout);
		addField(syserr);
		addField(sysin);
		addField(editor);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Returns the font data that describes the font to use for the console
	 */
	public static FontData getConsoleFontData() {
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		FontData fontData= PreferenceConverter.getFontData(pstore, CONSOLE_FONT);
		return fontData;
	}
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT, true);
		store.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR, true);
		WorkbenchChainedTextFontFieldEditor.startPropagate(store, CONSOLE_FONT);
		
		PreferenceConverter.setDefault(store, CONSOLE_SYS_OUT_RGB, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(store, CONSOLE_SYS_IN_RGB, new RGB(0, 200, 125));
		PreferenceConverter.setDefault(store, CONSOLE_SYS_ERR_RGB, new RGB(255, 0, 0));
	}
		
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		DebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}

}