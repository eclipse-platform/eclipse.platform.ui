/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.registry;

public class CheatSheetListenerElement {
	private String listenerClassPluginID;
	private String listenerID;
	private String listenerClass;
	private String targetCheatsheetID;
	
	/*package*/ CheatSheetListenerElement(String listenerID, String listenerClass, String classPluginId, String targetID) {
		super();
 		this.listenerID = listenerID;
 		this.listenerClass = listenerClass;
 		this.targetCheatsheetID = targetID;
 		this.listenerClassPluginID = classPluginId;
	}

	/**
	 * @return
	 */
	public String getListenerClass() {
		return listenerClass;
	}

	/**
	 * @return
	 */
	public String getListenerID() {
		return listenerID;
	}

	/**
	 * @return
	 */
	public String getTargetCheatsheetID() {
		return targetCheatsheetID;
	}

	/**
	 * @param string
	 */
	public void setListenerClass(String string) {
		listenerClass = string;
	}

	/**
	 * @param string
	 */
	public void setListenerID(String string) {
		listenerID = string;
	}

	/**
	 * @param string
	 */
	public void setTargetCheatsheetID(String string) {
		targetCheatsheetID = string;
	}

	/**
	 * @return
	 */
	public String getListenerClassPluginID() {
		return listenerClassPluginID;
	}

	/**
	 * @param string
	 */
	public void setListenerClassPluginID(String string) {
		listenerClassPluginID = string;
	}

}
