/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

/**
 * The EncodingDefinition is the object that handles all of the
 * information from the plug-in definition.
 */
public class EncodingDefinition {

	private String id;
	private String label;
	private String value;
	
	//Initialize the optional values to empty string
	private String helpContextId = ""; //$NON-NLS-1$
	private String description = ""; //$NON-NLS-1$
	private String toolTip = ""; //$NON-NLS-1$	

	/**
	 * Create a new instance of the receiver with the supplied
	 * name, id and label.
	 */

	EncodingDefinition(String newId, String newLabel, String newValue) {
		this.id = newId;
		this.label = newLabel;
		this.value = newValue;
	}

	
	/**
	 * Returns the helpContextId.
	 * @return String
	 */
	public String getHelpContextId() {
		return helpContextId;
	}

	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the label.
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the value.
	 * @return String
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the helpContextId.
	 * @param helpContextId The helpContextId to set
	 */
	public void setHelpContextId(String helpContextId) {
		this.helpContextId = helpContextId;
	}

	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the toolTip.
	 * @return String
	 */
	public String getToolTip() {
		return toolTip;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the toolTip.
	 * @param toolTip The toolTip to set
	 */
	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

}
