package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.preference.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A page to set the preferences for the console
 * 
 * @deprecated This preference page should not be used. To be removed by next release.
 */
public class LogConsolePreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	/**
	 * Create the console page.
	 */
	public LogConsolePreferencePage() {
		super(GRID);

		setDescription(ToolMessages.getString("LogConsolePreferencePage.description")); //$NON-NLS-1$

		IPreferenceStore store =
			ExternalToolsPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IHelpContextIds.LOG_CONSOLE_PREFERENCE_PAGE);
	}
	/**
	 * Create all field editors for this page
	 */
	public void createFieldEditors() {

		ColorFieldEditor errOut =
			new ColorFieldEditor(
				IPreferenceConstants.CONSOLE_ERROR_RGB,
				ToolMessages.getString("LogConsolePreferencePage.errorColor"), //$NON-NLS-1$
				getFieldEditorParent());
		ColorFieldEditor warnOut =
			new ColorFieldEditor(
				IPreferenceConstants.CONSOLE_WARNING_RGB,
				ToolMessages.getString("LogConsolePreferencePage.warningColor"), //$NON-NLS-1$
				getFieldEditorParent());
		ColorFieldEditor infoOut =
			new ColorFieldEditor(
				IPreferenceConstants.CONSOLE_INFO_RGB,
				ToolMessages.getString("LogConsolePreferencePage.infoColor"), //$NON-NLS-1$
				getFieldEditorParent());
		ColorFieldEditor verbOut =
			new ColorFieldEditor(
				IPreferenceConstants.CONSOLE_VERBOSE_RGB,
				ToolMessages.getString("LogConsolePreferencePage.verboseColor"), //$NON-NLS-1$
				getFieldEditorParent());
		ColorFieldEditor debugOut =
			new ColorFieldEditor(
				IPreferenceConstants.CONSOLE_DEBUG_RGB,
				ToolMessages.getString("LogConsolePreferencePage.debugColor"), //$NON-NLS-1$
				getFieldEditorParent());

		FontFieldEditor font =
			new FontFieldEditor(
				IPreferenceConstants.CONSOLE_FONT,
				ToolMessages.getString("LogConsolePreferencePage.font"), //$NON-NLS-1$
				getFieldEditorParent());

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
		IPreferenceStore pstore =
			ExternalToolsPlugin.getDefault().getPreferenceStore();
		FontData fontData =
			PreferenceConverter.getFontData(
				pstore,
				IPreferenceConstants.CONSOLE_FONT);
		return fontData;
	}
	/**
	 * Returns the a color based on the type.
	 */
	protected static Color getPreferenceColor(String type) {
		IPreferenceStore pstore =
			ExternalToolsPlugin.getDefault().getPreferenceStore();
		RGB outRGB = PreferenceConverter.getColor(pstore, type);
		return new Color(Display.getCurrent(), outRGB);
	}
	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}

}