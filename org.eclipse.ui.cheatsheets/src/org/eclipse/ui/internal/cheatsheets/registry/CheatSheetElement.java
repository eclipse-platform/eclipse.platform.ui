/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.internal.cheatsheets.registry;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 *	Instances represent registered cheatsheets.
 */
public class CheatSheetElement extends WorkbenchAdapter implements IAdaptable {
	private String contentFile;
	private String id;
	private String name;
	private ImageDescriptor imageDescriptor;
	private String description;
	private IConfigurationElement configurationElement;

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
	 * Answer the icon of this element.
	 */
	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	/**
	 * Returns the name of this cheatsheet element.
	 */
	public ImageDescriptor getImageDescriptor(Object element) {
		return imageDescriptor;
	}

	/**
	 * Returns the name of this cheatsheet element.
	 */
	public String getLabel(Object element) {
		return name;
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
	 * Set the icon of this element.
	 */
	public void setImageDescriptor(ImageDescriptor value) {
		imageDescriptor = value;
	}
}
