/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
public abstract class ActionItem extends AbstractItem {
	private String actionClass;
	private String[] actionParams;
	private String actionPluginID;
//	private String buttonCodes;
	private boolean complete;
	private boolean perform;
	private boolean skip;

	public ActionItem() {
		super();
	}
	
 	
	/**
	 * This method returns the class specified to be run when the "click to perform" button is pressed for this item.
	 * @return the class name to be run for the item
	 */
	public String getActionClass() {
		return (actionClass!=null)?actionClass:null;
	}

	/**
	 * This method returns an array of parameters specified to be passed to the action class
	 * when it is run in the cheat sheet.
	 * @return an array of string parameters that are passed to the action class when it is run	 
	 */
	public String[] getActionParams() {
		return (actionParams!=null)?actionParams:null;
	}

	/**
	 * This method returns the string id of the plugin that contains the action class to be run.
	 * @return the id of the plugin that has the action class
	 */
	public String getActionPluginID() {
		return (actionPluginID!=null)?actionPluginID:null;
	}

	/**
	 * This method returns a comma separated string that represents the buttons displayed for this item in the cheat sheet.
	 * the code possibilities are :<li> 0 : perform action</li>
	 * 										   <li>1 : Skip this task</li>
	 * 										   <li>2 : click when done</li></p>
	 * <p>An example string to pass would be "0,1,2".  When using this in the cheat sheet content file,
	 * the full attribute for the item would be this:  actionphrase="0,1,2".</p> 
	 * @return a comma separated string of button possibilities
	 */
//	public String getButtonCodes() {
//		return (buttonCodes!=null)?buttonCodes:null;
//	}

	/**
	 * <p>This method accepts a comma separated string of the valid button codes,
	 * the code possibilities are : <li>0 : perform action</li>
	 * 										   <li>1 : Skip this task</li>
	 * 										   <li>2 : click when done</li>
	 * An example string to pass would be "0,1,2".  When using this in the cheat sheet content file,
	 * the full attribute for the item would be this:  actionphrase="0,1,2".</p> 
	 * @param codes the comma separated string of codes
	 */
//	public void setButtonCodes(String codes) {
//		this.buttonCodes = codes;
//	}

	/**
	 * @return Returns the complete.
	 * @todo Generated comment
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return Returns the perform.
	 * @todo Generated comment
	 */
	public boolean isPerform() {
		return perform;
	}
	/**
	 * @return Returns the skip.
	 * @todo Generated comment
	 */
	public boolean isSkip() {
		return skip;
	}

	/**
	 *  This method allows you to specify the class to be run when the perform button is pressed for this 
	 * item in the cheat sheet. 
	 * @param classname the class to be run by the item in the cheat sheet
	 */
	public void setActionClass(String aclass) {
		this.actionClass = aclass;
	}

	/**
	 * This method allows you to set the string parameters to be passed to the action class on running it 
	 * in the cheat sheet.
	 * @param params an array of strings that is passed to the action class on running the action
	 */
	public void setActionParams(String[] params) {
		this.actionParams = params;
	}

	/**
	 * This method allows to set the plugin id of the action to be run by this item in the cheat sheet.
	 * @param pluginId the id of the plugin containing the action class specified for this item
	 */
	public void setActionPluginID(String pluginId) {
		this.actionPluginID = pluginId;
	}

	/**
	 * @param complete The complete to set.
	 * @todo Generated comment
	 */
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	/**
	 * @param perform The perform to set.
	 * @todo Generated comment
	 */
	public void setPerform(boolean perform) {
		this.perform = perform;
	}
	/**
	 * @param skip The skip to set.
	 * @todo Generated comment
	 */
	public void setSkip(boolean skip) {
		this.skip = skip;
	}
}
