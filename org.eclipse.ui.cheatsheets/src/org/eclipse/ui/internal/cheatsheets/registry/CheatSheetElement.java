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

import org.eclipse.core.runtime.*;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 *	Instances represent registered cheatsheets.
 */
public class CheatSheetElement extends WorkbenchAdapter implements IAdaptable {
	private String contentFile;
	private String id;
	private String name;
	private String description;
	private IConfigurationElement configurationElement;
	private String listenerClass;

	/**
	 *	Create a new instance of this class
	 *
	 *	@param name java.lang.String
	 */
	public CheatSheetElement(String name) {
		this.name = name;
	}

	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * 
	 * @return IConfigurationElement
	 */
	public IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	/**
	 *	Answer the contentFile parameter of this element
	 *
	 *	@return java.lang.String
	 */
	public String getContentFile() {
		return contentFile;
	}

	/**
	 *	Answer the description parameter of this element
	 *
	 *	@return java.lang.String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *	Answer the id as specified in the extension.
	 *
	 *	@return java.lang.String
	 */
	public String getID() {
		return id;
	}

	/**
	 * Returns the name of this cheatsheet element.
	 */
	public String getLabel(Object element) {
		return name;
	}

	/**
	 * Returns the listener class name of this cheatsheet element.
	 */
	public String getListenerClass() {
		return listenerClass;
	}

	/**
	 * 
	 * @param newConfigurationElement IConfigurationElement
	 */
	public void setConfigurationElement(IConfigurationElement newConfigurationElement) {
		configurationElement = newConfigurationElement;
	}

	/**
	 *	Set the contentFile parameter of this element
	 *
	 *	@param value java.lang.String
	 */
	public void setContentFile(String value) {
		contentFile = value;
	}

	/**
	 *	Set the description parameter of this element
	 *
	 *	@param value java.lang.String
	 */
	public void setDescription(String value) {
		description = value;
	}

	/**
	 *	Set the id parameter of this element
	 *
	 *	@param value java.lang.String
	 */
	public void setID(String value) {
		id = value;
	}

	/**
	 * Set the listener class name of this element.
	 */
	public void setListenerClass(String value) {
		listenerClass = value;
	}
}
