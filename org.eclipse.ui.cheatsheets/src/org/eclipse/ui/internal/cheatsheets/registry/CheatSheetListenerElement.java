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
