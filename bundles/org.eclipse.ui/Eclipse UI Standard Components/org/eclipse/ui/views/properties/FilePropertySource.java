package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import java.io.File;

/**
 * The FilePropertySource gives the extra information that is shown for files
 */
public class FilePropertySource extends ResourcePropertySource {

	// last modified state
	private static IPropertyDescriptor[] fileDescriptors;
/**
 * Creates an property source for a file resource.
 * @param file the file resource
 */
public FilePropertySource(IFile file) {
	super(file);
}
/**
 * Get a PropertyDescriptor that defines the size property
 * @return the PropertyDescriptor
 */
private static PropertyDescriptor getInitialPropertyDescriptor() {
	PropertyDescriptor descriptor =
		new PropertyDescriptor(
			IResourcePropertyConstants.P_SIZE_RES,
			IResourcePropertyConstants.P_DISPLAY_SIZE);
	descriptor.setAlwaysIncompatible(true);
	descriptor.setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
	return descriptor;

}
/* (non-Javadoc)
 * Method declared on IPropertySource.
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	if (fileDescriptors == null)
		initializeFileDescriptors();
	return fileDescriptors;
}
/* (non-Javadoc)
 * Method declared on IPropertySource.
 */
public Object getPropertyValue(Object key) {

	Object returnValue = super.getPropertyValue(key);
	
	if(returnValue != null)
		return returnValue;
		
	if (key.equals(IResourcePropertyConstants.P_SIZE_RES)) 
		return getSizeString((IFile) element);
	
	return null;
}
/**
 * Return a String that indicates the size of the supplied file.
 */
private String getSizeString(IFile file) {
	if (!file.isLocal(IResource.DEPTH_ZERO))
		return NOT_LOCAL_TEXT;
	else {
		File localFile = getFile(file);
		if(localFile == null)
			return FILE_NOT_FOUND;
		else
			return Long.toString(localFile.length());
	}
}

/**
 * Return the Property Descriptors for the file type.
 */
private void initializeFileDescriptors() {
	
	IPropertyDescriptor[] superDescriptors = super.getPropertyDescriptors();
	int superLength = superDescriptors.length;
	fileDescriptors = new IPropertyDescriptor[superLength + 1];
	System.arraycopy(superDescriptors, 0, fileDescriptors, 0, superLength);
	fileDescriptors[superLength] = getInitialPropertyDescriptor();
	
}
}
