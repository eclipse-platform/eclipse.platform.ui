/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.data;

import org.eclipse.ui.cheatsheets.IItem;

public abstract class ActionItem extends AbstractItem implements IItem {
	private String buttonCodes;
	private String actionClass;
	private String actionPluginID;
	private String[] actionParams;

	public ActionItem() {
		super();
	}
	
 	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#getActionClass()
	 */
	public String getActionClass() {
		return (actionClass!=null)?actionClass:null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#getActionParams()
	 */
	public String[] getActionParams() {
		return (actionParams!=null)?actionParams:null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#getActionPluginID()
	 */
	public String getActionPluginID() {
		return (actionPluginID!=null)?actionPluginID:null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#getButtonCodes()
	 */
	public String getButtonCodes() {
		return (buttonCodes!=null)?buttonCodes:null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#setActionClass(java.lang.String)
	 */
	public void setActionClass(String aclass) {
		this.actionClass = aclass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#setActionParams(java.lang.String[])
	 */
	public void setActionParams(String[] params) {
		this.actionParams = params;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#setActionPluginID(java.lang.String)
	 */
	public void setActionPluginID(String pluginId) {
		this.actionPluginID = pluginId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.cheatsheets.IItem#setButtonCodes(java.lang.String)
	 */
	public void setButtonCodes(String codes) {
		this.buttonCodes = codes;
	}

}
