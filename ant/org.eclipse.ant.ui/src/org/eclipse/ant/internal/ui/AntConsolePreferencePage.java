package org.eclipse.ant.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.preference.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A page to set the preferences for the console
 */
public class AntConsolePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
		

/**
 * Create the console page.
 */
public AntConsolePreferencePage() {
	super(GRID);
	
	setDescription(Policy.bind("preferences.description"));
	
	IPreferenceStore store= AntUIPlugin.getPlugin().getPreferenceStore();
	setPreferenceStore(store);
}
public void createControl(Composite parent) {
	super.createControl(parent);
	
	// should do something here with the help..	
	/*
	WorkbenchHelp.setHelp(
		parent,
		new Object[] { IAntHelpContextIds.ANT_CONSOLE_PREFERENCE_PAGE });
	*/
}
/**
 * Create all field editors for this page
 */
public void createFieldEditors() {

	ColorFieldEditor errOut= new ColorFieldEditor(IAntPreferenceConstants.CONSOLE_ERROR_RGB, Policy.bind("preferences.errorColor"), getFieldEditorParent());
	ColorFieldEditor warnOut= new ColorFieldEditor(IAntPreferenceConstants.CONSOLE_WARNING_RGB, Policy.bind("preferences.warningColor"), getFieldEditorParent());
	ColorFieldEditor infoOut= new ColorFieldEditor(IAntPreferenceConstants.CONSOLE_INFO_RGB, Policy.bind("preferences.infoColor"), getFieldEditorParent());
	ColorFieldEditor verbOut= new ColorFieldEditor(IAntPreferenceConstants.CONSOLE_VERBOSE_RGB, Policy.bind("preferences.verboseColor"), getFieldEditorParent());
	ColorFieldEditor debugOut= new ColorFieldEditor(IAntPreferenceConstants.CONSOLE_DEBUG_RGB, Policy.bind("preferences.debugColor"), getFieldEditorParent());
	
	FontFieldEditor font= new FontFieldEditor(IAntPreferenceConstants.CONSOLE_FONT, Policy.bind("preferences.font"), getFieldEditorParent());

	addField(errOut);
	addField(warnOut);
	addField(infoOut);
	addField(verbOut);
	addField(debugOut);
	addField(font);
}
/**
 * Returns the font data that describes the font to use for the console
 */
protected static FontData getConsoleFontData() {
	IPreferenceStore pstore= AntUIPlugin.getPlugin().getPreferenceStore();
	FontData fontData= PreferenceConverter.getFontData(pstore, IAntPreferenceConstants.CONSOLE_FONT);
	return fontData;
}
/**
 * Returns the a color based on the type.
 */
protected static Color getPreferenceColor(String type) {
	IPreferenceStore pstore= AntUIPlugin.getPlugin().getPreferenceStore();
	RGB outRGB= PreferenceConverter.getColor(pstore, type);
	return new Color(Display.getCurrent() ,outRGB);
}
/**
 * @see IWorkbenchPreferencePage#init
 */
public void init(IWorkbench workbench) {
}

}
