/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

/**
 * <p>Interface that represents a step with no sub steps in the cheat sheets view.
 * An IItem can be retrieved from the <code>ICheatSheetManager</code> by calling getItem with the 
 * id of the step in the cheat sheet.  You may use the methods from this interface 
 * to change and manipulate the buttons displayed for this step in the cheat sheet,
 * as well as change/set the action class and plugin id for the action that will be run by the
 * "click to perform" button.  You may also manipulate or add string parameters that will
 * be passed to the action for this step in the cheat sheet.  To convert this step to allow the addition of sub steps
 * to it in the cheat sheet, see <code>IItemWithSubItems</code> and <code>ICheatSheetManager</code>.</p>
 * 
 *<p> Note:  You may only use these methods to change the step if it has been marked as
 * "dynamic" in the cheat sheet content file.</p>
 */
public class Action {
	private String actionClass;
	private String[] params;
	private String pluginID;
	private boolean confirm;
	private String when;

	public Action() {
		super();
	}
	
 	
	/**
	 * This method returns the class specified to be run when the "click to perform" button is pressed for this item.
	 * @return the class name to be run for the item
	 */
	public String getActionClass() {
		return actionClass;
	}

	/**
	 * This method returns an array of parameters specified to be passed to the action class
	 * when it is run in the cheat sheet.
	 * @return an array of string parameters that are passed to the action class when it is run	 
	 */
	public String[] getParams() {
		return params;
	}

	/**
	 * This method returns the string id of the plugin that contains the action class to be run.
	 * @return the id of the plugin that has the action class
	 */
	public String getPluginID() {
		return pluginID;
	}

	/**
	 * This method returns the expression to be used when determining if this action should used. 
	 * @return the when expression to be used for this action
	 */
	public String getWhen() {
		return when;
	}

	/**
	 * Returns whether this action needs to be manually confirmed by the user.
	 * @return <code>true</code> when the action needs to be confirmed and <code>false</code> otherwise.
	 */
	public boolean isConfirm() {
		return confirm;
	}

	/**
	 *  This method allows you to specify the class to be run when the perform button is pressed for this 
	 * item in the cheat sheet. 
	 * @param classname the class to be run by the item in the cheat sheet
	 */
	public void setClass(String aclass) {
		this.actionClass = aclass;
	}

	/**
	 * This method allows you to set the string parameters to be passed to the action class on running it 
	 * in the cheat sheet.
	 * @param params an array of strings that is passed to the action class on running the action
	 */
	public void setParams(String[] params) {
		this.params = params;
	}

	/**
	 * This method allows to set the plugin id of the action to be run by this item in the cheat sheet.
	 * @param pluginId the id of the plugin containing the action class specified for this item
	 */
	public void setPluginID(String pluginId) {
		this.pluginID = pluginId;
	}

	/**
	 * Set whether this action needs to be manually confirmed by the user.
	 * @param value The new value of the confirm state.
	 */
	public void setConfirm(boolean value) {
		this.confirm = value;
	}

	/**
	 * Indicates this action is to be used if and only if the value of the condition attribute
	 * of the containing <perform-when> element matches this string value. This attribute is
	 * ignored if the <action> element is not a child of  a <perform-when> element.
	 * @param when The expression to use when determine if this action should be used.
	 */
	public void setWhen(String when) {
		this.when = when;
	}
}
