package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Enumeration;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

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
 *	Answer a boolean indicating whether self is able to handle the
 *	passed selection
 *
 *	@return boolean
 *	@param selection IStructuredSelection
 */
public boolean canHandleSelection(IStructuredSelection selection) {
	return getSelectionEnabler().isEnabledForSelection(selection);
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
}
