/********************************************************************** * Copyright (c) 2002 IBM Corporation and others. * All rights reserved.   This program and the accompanying materials * are made available under the terms of the Common Public License v0.5 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/cpl-v05.html *  * Contributors:  * IBM - Initial API and implementation **********************************************************************/package org.eclipse.toolscript.ui.internal;import java.util.Iterator;import org.eclipse.jface.util.IPropertyChangeListener;import org.eclipse.jface.util.PropertyChangeEvent;import org.eclipse.swt.graphics.Color;import org.eclipse.swt.graphics.Font;import org.eclipse.swt.graphics.FontData;import org.eclipse.swt.widgets.Display;import org.eclipse.toolscript.core.internal.IPreferenceConstants;
public class AntPropertyChangeListener implements IPropertyChangeListener {
		// unique instance	private static AntPropertyChangeListener instance = new AntPropertyChangeListener();	// private constructor to ensure the singletonprivate AntPropertyChangeListener() {}// access to the singletonpublic static AntPropertyChangeListener getInstance() {	return instance;}
/**
 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
 */
public void propertyChange(PropertyChangeEvent event) {
//	String propertyName= event.getProperty();
//
//	if (propertyName.equals(IPreferenceConstants.CONSOLE_ERROR_RGB)) {
//		Color temp = AntConsole.ERROR_COLOR;
//		AntConsole.ERROR_COLOR = AntConsolePreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_ERROR_RGB);
//		temp.dispose();//		clearOutput();
//	} else if (propertyName.equals(IPreferenceConstants.CONSOLE_WARNING_RGB)) {
//		Color temp = AntConsole.WARN_COLOR;
//		AntConsole.WARN_COLOR = AntConsolePreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_WARNING_RGB);
//		temp.dispose();//		clearOutput();
//	} else if (propertyName.equals(IPreferenceConstants.CONSOLE_INFO_RGB)) {
//		Color temp = AntConsole.INFO_COLOR;
//		AntConsole.INFO_COLOR = AntConsolePreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_INFO_RGB);
//		temp.dispose();//		clearOutput();
//	} else if (propertyName.equals(IPreferenceConstants.CONSOLE_VERBOSE_RGB)) {
//		Color temp = AntConsole.VERBOSE_COLOR;
//		AntConsole.VERBOSE_COLOR = AntConsolePreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_VERBOSE_RGB);
//		temp.dispose();//		clearOutput();
//	} else if (propertyName.equals(IPreferenceConstants.CONSOLE_DEBUG_RGB)) {
//		Color temp = AntConsole.DEBUG_COLOR;
//		AntConsole.DEBUG_COLOR = AntConsolePreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_DEBUG_RGB);
//		temp.dispose();//		clearOutput();
//	} else if (propertyName.equals(IPreferenceConstants.CONSOLE_FONT)) {//		FontData data= AntConsolePreferencePage.getConsoleFontData();//		Font temp= AntConsole.ANT_FONT;//		AntConsole.ANT_FONT = new Font(Display.getCurrent(), data);//		temp.dispose();//		updateFont();	//	} else
//		return;
}
/** * Clears the output of all the consoles */private void clearOutput(){	LogConsoleDocument.getInstance().clearOutput();}/** * Updates teh font in all the consoles */private void updateFont() {	for (Iterator iterator = LogConsoleDocument.getInstance().getViews().iterator(); iterator.hasNext();)		((LogConsoleView) iterator.next()).updateFont();	}
}
