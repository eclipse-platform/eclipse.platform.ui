package org.eclipse.team.internal.ccvs.ui.model;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CVSRemoteFolderPropertySource implements IPropertySource {
	ICVSRemoteFolder folder;
	
	// Property Descriptors
	static protected IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[2];
	{
		PropertyDescriptor descriptor;
		String category = Policy.bind("cvs");
		
		// resource name
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_NAME, Policy.bind("CVSRemoteFolderPropertySource.name"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[0] = descriptor;
		// tag
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_TAG, Policy.bind("CVSRemoteFolderPropertySource.tag"));
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[1] = descriptor;
	}

	/**
	 * Create a PropertySource and store its file
	 */
	public CVSRemoteFolderPropertySource(ICVSRemoteFolder folder) {
		this.folder = folder;
	}
	
	/**
	 * Do nothing because properties are read only.
	 */
	public Object getEditableValue() {
		return this;
	}

	/**
	 * Return the Property Descriptors for the receiver.
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return propertyDescriptors;
	}

	/*
	 * @see IPropertySource#getPropertyValue(Object)
	 */
	public Object getPropertyValue(Object id) {
		if (id.equals(ICVSUIConstants.PROP_NAME)) {
			return folder.getName();
		}
		if (id.equals(ICVSUIConstants.PROP_TAG)) {
			CVSTag tag = folder.getTag();
			if (tag == null) {
				return Policy.bind("CVSRemoteFolderPropertySource.none");
			}
			return tag.getName();
		}
		return "";
	}

	/**
	 * Answer true if the value of the specified property 
	 * for this object has been changed from the default.
	 */
	public boolean isPropertySet(Object property) {
		return false;
	}
	/**
	 * Reset the specified property's value to its default value.
	 * Do nothing because properties are read only.
	 * 
	 * @param   property    The property to reset.
	 */
	public void resetPropertyValue(Object property) {
	}
	/**
	 * Do nothing because properties are read only.
	 */
	public void setPropertyValue(Object name, Object value) {
	}
}
