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

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.internal.SelectionEnabler;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 *	Instances represent registered wizards.
 */
public class WorkbenchWizardElement extends WorkbenchAdapter implements IAdaptable {
	private String id;
	private String name;
	private ImageDescriptor imageDescriptor;
	private String description;
	private SelectionEnabler selectionEnabler;
	private IConfigurationElement configurationElement;
/**
 *	Create a new instance of this class
 *
 *	@param name java.lang.String
 */
public WorkbenchWizardElement(String name) {
	this.name = name;
}
/**
 *	Answer a boolean indicating whether the receiver is able to handle the
 *	passed selection
 *
 *	@return boolean
 *	@param selection IStructuredSelection
 */
public boolean canHandleSelection(IStructuredSelection selection) {
	return getSelectionEnabler().isEnabledForSelection(selection);
}

/**
 *	Answer the selection for the reciever based on whether the 
 *  it can handle the selection. If it can return the selection.
 *  If it can handle the adapted to IResource value of the selection.
 *  If it satisfies neither of these conditions return an empty
 *  IStructuredSelection.
 *
 *	@return IStructuredSelection
 *	@param selection IStructuredSelection
 */
public IStructuredSelection adaptedSelection(IStructuredSelection selection) {
	if (canHandleSelection(selection))
		return selection;
		
	IStructuredSelection adaptedSelection = convertToResources(selection);
	if (canHandleSelection(adaptedSelection))
		return adaptedSelection;
		
	//Couldn't find one that works so just return
	return StructuredSelection.EMPTY;
}

/**
 * Create an the instance of the object described by the configuration
 * element. That is, create the instance of the class the isv supplied in
 * the extension point.
 */
public Object createExecutableExtension ()  throws CoreException {
	return WorkbenchPlugin.createExtension(configurationElement,
		WizardsRegistryReader.ATT_CLASS);
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
 * Returns the name of this wizard element.
 */
public ImageDescriptor getImageDescriptor(Object element) {
	return imageDescriptor;
}
/**
 * Returns the name of this wizard element.
 */
public String getLabel(Object element) {
	return name;
}
/**
 *	Answer self's action enabler, creating it first iff necessary
 */
protected SelectionEnabler getSelectionEnabler() {
	if (selectionEnabler == null)
		selectionEnabler = new SelectionEnabler(configurationElement);
		
	return selectionEnabler;
}
/**
 * 
 * @param newConfigurationElement IConfigurationElement
 */
public void setConfigurationElement(IConfigurationElement newConfigurationElement) {
	configurationElement = newConfigurationElement;
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

/**
 * Attempt to convert the elements in the passed selection into resources
 * by asking each for its IResource property (iff it isn't already a resource).
 * If all elements in the initial selection can be converted to resources then
 * answer a new selection containing these resources; otherwise answer a new
 * empty selection
 *
 * @param originalSelection IStructuredSelection
 * @return IStructuredSelection
 */
private IStructuredSelection convertToResources(IStructuredSelection originalSelection) {
	List result = new ArrayList();
	Iterator elements = originalSelection.iterator();
	
	while (elements.hasNext()) {
		Object currentElement = elements.next();
		if (currentElement instanceof IResource) {				// already a resource
			result.add(currentElement);
		} else if (!(currentElement instanceof IAdaptable))	{	// cannot be converted to resource
			return StructuredSelection.EMPTY;					// so fail
		} else {
			Object adapter = ((IAdaptable)currentElement).getAdapter(IResource.class);
			if (!(adapter instanceof IResource))				// chose not to be converted to resource
				return StructuredSelection.EMPTY;				// so fail
			result.add(adapter);							// add the converted resource
		}
	}
	
	return new StructuredSelection(result.toArray());						// all converted fine, answer new selection
}
}
