package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A Resource property source.
 */
public class ResourcePropertySource implements IPropertySource {
	protected static String NOT_LOCAL_TEXT = PropertiesMessages.getString("PropertySource.notLocal"); //$NON-NLS-1$
	protected static String FILE_NOT_FOUND = PropertiesMessages.getString("PropertySource.notFound"); //$NON-NLS-1$
	
	// The element for the property source
	protected IResource element;

	// Error message when setting a property incorrectly
	protected String errorMessage = PropertiesMessages.getString("PropertySource.readOnly"); //$NON-NLS-1$

	// Property Descriptors
	static protected IPropertyDescriptor[] propertyDescriptors =
		new IPropertyDescriptor[4];
	{
		PropertyDescriptor descriptor;

		// resource name
		descriptor =
			new PropertyDescriptor(
				IBasicPropertyConstants.P_TEXT,
				IResourcePropertyConstants.P_LABEL_RES);
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
		propertyDescriptors[0] = descriptor;

		// Relative path
		descriptor =
			new PropertyDescriptor(
				IResourcePropertyConstants.P_PATH_RES,
				IResourcePropertyConstants.P_DISPLAYPATH_RES);
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
		propertyDescriptors[1] = descriptor;

		// readwrite state
		descriptor =
			new PropertyDescriptor(
				IResourcePropertyConstants.P_EDITABLE_RES,
				IResourcePropertyConstants.P_DISPLAYEDITABLE_RES);
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
		propertyDescriptors[2] = descriptor;

		// last modified state
		descriptor =
			new PropertyDescriptor(
				IResourcePropertyConstants.P_LAST_MODIFIED_RES,
				IResourcePropertyConstants.P_DISPLAY_LAST_MODIFIED);
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
		propertyDescriptors[3] = descriptor;

	}
/**
 * Creates a PropertySource and stores its IResource
 *
 * @param res the resource for which this is a property source
 */
public ResourcePropertySource(IResource res) {
	this.element = res;
}
/**
 * Return the value for the date String for the timestamp of the supplied resource.
 * @return String
 * @param IResource - the resource to query
 */
private String getDateStringValue(IResource resource) {

	if (!resource.isLocal(IResource.DEPTH_ZERO))
		return NOT_LOCAL_TEXT;
	else {
		File localFile = getFile(resource);
		if(localFile == null)
			return FILE_NOT_FOUND;
		else{
			DateFormat format = new SimpleDateFormat();
			return format.format(new Date(localFile.lastModified()));
		}
	}
}
/* (non-Javadoc)
 * Method declared on IPropertySource.
 */
public Object getEditableValue() {
	return this;
}
/* (non-Javadoc)
 * Method declared on IPropertySource.
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	return propertyDescriptors;
}
/* (non-Javadoc)
 * Method declared on IPropertySource.
 */
public Object getPropertyValue(Object name) {
	if (name.equals(IBasicPropertyConstants.P_TEXT)) {
		return element.getName();
	}
	if (name.equals(IResourcePropertyConstants.P_PATH_RES)) {
		return element.getFullPath().toString();
	}
	if (name.equals(IResourcePropertyConstants.P_LAST_MODIFIED_RES)) {
		return getDateStringValue(element);
	}
	if (name.equals(IResourcePropertyConstants.P_EDITABLE_RES)) {
		if (element.isReadOnly())
			return "false";//$NON-NLS-1$
		else
			return "true";//$NON-NLS-1$
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IPropertySource.
 */
public boolean isPropertySet(Object property) {
	return false;
}
/**
 * The <code>ResourcePropertySource</code> implementation of this
 * <code>IPropertySource</code> method does nothing since all
 * properties are read-only.
 */
public void resetPropertyValue(Object property) {}
/**
 * The <code>ResourcePropertySource</code> implementation of this
 * <code>IPropertySource</code> method does nothing since all
 * properties are read-only.
 */
public void setPropertyValue(Object name, Object value) {
}

/** 
 * Get the java.io.File equivalent of the passed
 * IFile. If the location does not exist then return
 * null
 * @param IFile
 * @return java.io.File or <code>null</code>.
 */
protected File getFile(IResource resource) {
	IPath location = resource.getLocation();
	if(location == null)
		return null;
	else
		return location.toFile();
}

}
