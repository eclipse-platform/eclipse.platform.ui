/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;


import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class CVSRepositoryLocationPropertySource implements IPropertySource {
	ICVSRepositoryLocation location;
	
	// Property Descriptors
	static protected IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[5];
	{
		PropertyDescriptor descriptor;
		String category = Policy.bind("cvs"); //$NON-NLS-1$
		
		// host
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_HOST, Policy.bind("CVSRepositoryLocationPropertySource.host")); //$NON-NLS-1$
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[0] = descriptor;
		// user
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_USER, Policy.bind("CVSRepositoryLocationPropertySource.user")); //$NON-NLS-1$
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[1] = descriptor;
		// port
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_PORT, Policy.bind("CVSRepositoryLocationPropertySource.port")); //$NON-NLS-1$
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[2] = descriptor;
		// root
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_ROOT, Policy.bind("CVSRepositoryLocationPropertySource.root")); //$NON-NLS-1$
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[3] = descriptor;
		// method
		descriptor = new PropertyDescriptor(ICVSUIConstants.PROP_METHOD, Policy.bind("CVSRepositoryLocationPropertySource.method")); //$NON-NLS-1$
		descriptor.setAlwaysIncompatible(true);
		descriptor.setCategory(category);
		propertyDescriptors[4] = descriptor;
	}

	/**
	 * Create a PropertySource and store its file
	 */
	public CVSRepositoryLocationPropertySource(ICVSRepositoryLocation location) {
		this.location = location;
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
		if (id.equals(ICVSUIConstants.PROP_HOST)) {
			return location.getHost();
		}
		if (id.equals(ICVSUIConstants.PROP_USER)) {
			return location.getUsername();
		}
		if (id.equals(ICVSUIConstants.PROP_METHOD)) {
			return location.getMethod().getName();
		}
		if (id.equals(ICVSUIConstants.PROP_ROOT)) {
			return location.getRootDirectory();
		}
		if (id.equals(ICVSUIConstants.PROP_PORT)) {
			int port = location.getPort();
			if (port == ICVSRepositoryLocation.USE_DEFAULT_PORT) {
				return Policy.bind("CVSRepositoryLocationPropertySource.default"); //$NON-NLS-1$
			}
			return "" + port; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
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
