package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.jface.preference.*;import org.eclipse.swt.graphics.*;import org.eclipse.ui.IWorkbench;import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A page to set the preferences for the console
 */
public class ConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {
		
	private static final String PREFIX= "console_preferences.";
	private static final String DESCRIPTION= PREFIX + "description";
	private static final String OUT= PREFIX + "out";
	private static final String ERR= PREFIX + "err";
	private static final String IN= PREFIX + "in";
	private static final String FONT=PREFIX + "font";

	/**
	 * Create the console page.
	 */
	public ConsolePreferencePage() {
		super(GRID);
		setDescription(DebugUIUtils.getResourceString(DESCRIPTION));
		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	/**
	 * Create all field editors for this page
	 */
	public void createFieldEditors() {

		// Note: first String value is the key for the preference bundle and second the
		// second String value is the label displayed in front of the editor.
		ColorFieldEditor sysout= new ColorFieldEditor(CONSOLE_SYS_OUT_RGB, DebugUIUtils.getResourceString(OUT), getFieldEditorParent());
		ColorFieldEditor syserr= new ColorFieldEditor(CONSOLE_SYS_ERR_RGB, DebugUIUtils.getResourceString(ERR), getFieldEditorParent());
		ColorFieldEditor sysin= new ColorFieldEditor(CONSOLE_SYS_IN_RGB, DebugUIUtils.getResourceString(IN), getFieldEditorParent());
		
		FontFieldEditor font= new FontFieldEditor(CONSOLE_FONT, DebugUIUtils.getResourceString(FONT), getFieldEditorParent());
		addField(sysout);
		addField(syserr);
		addField(sysin);
		addField(font);
	}

	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * Returns the a color based on the type.
	 */
	protected static Color getPreferenceColor(String type) {
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		RGB outRGB= PreferenceConverter.getColor(pstore, type);
		ColorManager colorManager= DebugUIPlugin.getDefault().getColorManager();
		return colorManager.getColor(outRGB);
	}
	
	/**
	 * Returns the font data that describes the font to use for the console
	 */
	protected static FontData getConsoleFontData() {
		IPreferenceStore pstore= DebugUIPlugin.getDefault().getPreferenceStore();
		FontData fontData= PreferenceConverter.getFontData(pstore, CONSOLE_FONT);
		return fontData;
	}
}

