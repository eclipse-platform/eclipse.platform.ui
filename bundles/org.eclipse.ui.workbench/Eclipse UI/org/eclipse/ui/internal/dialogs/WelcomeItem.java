/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelp;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
/**
 * Holds the information for an item appearing in the welcome editor
 */
public class WelcomeItem {
	private String text;
	private int[][] boldRanges;
	private int[][] helpRanges;
	private String[] helpIds;
	private String[] helpHrefs;
	private int[][] actionRanges;
	private String[] actionPluginIds;
	private String[] actionClasses;
/**
 * Creates a new welcome item
 */
public WelcomeItem(
	String text,
	int[][] boldRanges,
	int[][] actionRanges,
	String[] actionPluginIds,
	String[] actionClasses,
	int[][] helpRanges,
	String[] helpIds,
	String[] helpHrefs) {
	    
	this.text = text;
	this.boldRanges = boldRanges;
	this.actionRanges = actionRanges;
	this.actionPluginIds = actionPluginIds;
	this.actionClasses = actionClasses;
	this.helpRanges = helpRanges;
	this.helpIds = helpIds;
	this.helpHrefs = helpHrefs;
}
/**
 * Returns the action ranges (character locations)
 */
public int[][] getActionRanges() {
	return actionRanges;
}
/**
 * Returns the bold ranges (character locations)
 */
public int[][] getBoldRanges() {
	return boldRanges;
}
/**
 * Returns the help ranges (character locations)
 */
public int[][] getHelpRanges() {
	return helpRanges;
}
/**
 * Returns the text to display
 */
public String getText() {
	return text;
}
/**
 * Returns true is a link (action or help) is present at the given character location
 */
public boolean isLinkAt(int offset) {
	// Check if there is a link at the offset
	for (int i = 0; i < helpRanges.length; i++){
		if (offset >= helpRanges[i][0] && offset < helpRanges[i][0] + helpRanges[i][1]) {
			return true;
		}
	}

	// Check if there is an action link at the offset
	for (int i = 0; i < actionRanges.length; i++){
		if (offset >= actionRanges[i][0] && offset < actionRanges[i][0] + actionRanges[i][1]) {
			return true;
		}
	}
	return false;
}
/**
 * Logs a error to the workbench log
 */
public void logActionLinkError(String actionPluginId, String actionClass) {
	WorkbenchPlugin.log(WorkbenchMessages.getString("WelcomeItem.unableToLoadClass") + actionPluginId + " " + actionClass); //$NON-NLS-1$	//$NON-NLS-2$
}
/**
 * Open a help topic
 */
private void openHelpTopic(String topic, String href) {
	IHelp helpSupport = WorkbenchHelp.getHelpSupport();
	if (helpSupport != null) {
		if (href != null) 
			helpSupport.displayHelpResource(href);
		else
			helpSupport.displayHelpResource(topic);
	}
}
/**
 * Run an action
 */
private void runAction(String pluginId, String className) {
	IPluginDescriptor desc = Platform.getPluginRegistry().getPluginDescriptor(pluginId);
	if (desc == null) {
		logActionLinkError(pluginId, className);
		return;
	}		
	Class actionClass;
	IAction action;
	try {
		actionClass = desc.getPluginClassLoader().loadClass(className);
	} catch (ClassNotFoundException e) {
		logActionLinkError(pluginId, className);
		return;
	}
	try {
		action = (IAction)actionClass.newInstance();
	} catch (InstantiationException e) {
		logActionLinkError(pluginId, className);
		return;
	} catch (IllegalAccessException e) {
		logActionLinkError(pluginId, className);
		return;
	} catch (ClassCastException e) {
		logActionLinkError(pluginId, className);
		return;
	}
	action.run();
}
/**
 * Triggers the link at the given offset (if there is one)
 */
public void triggerLinkAt(int offset) {
	// Check if there is a help link at the offset
	for (int i = 0; i < helpRanges.length; i++){
		if (offset >= helpRanges[i][0] && offset < helpRanges[i][0] + helpRanges[i][1]) {
			// trigger the link
			openHelpTopic(helpIds[i], helpHrefs[i]);
			return;
		}
	}

	// Check if there is an action link at the offset
	for (int i = 0; i < actionRanges.length; i++){
		if (offset >= actionRanges[i][0] && offset < actionRanges[i][0] + actionRanges[i][1]) {
			// trigger the link
			runAction(actionPluginIds[i], actionClasses[i]);
			return;
		}
	}
}
}
