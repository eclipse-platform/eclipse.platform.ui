/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

import java.io.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;
import org.eclipse.ui.internal.views.properties.IDEPropertiesMessages;

/**
 * A Resource property source.
 */
public class ResourcePropertySource implements IPropertySource {
    protected static String NOT_LOCAL_TEXT = IDEPropertiesMessages.PropertySource_notLocal;

    protected static String FILE_NOT_FOUND = IDEPropertiesMessages.PropertySource_notFound;

    protected static String UNDEFINED_PATH_VARIABLE = IDEPropertiesMessages.PropertySource_undefinedPathVariable;

    protected static String FILE_NOT_EXIST_TEXT = IDEPropertiesMessages.PropertySource_fileNotExist;

    // The element for the property source
    protected IResource element;

    // Error message when setting a property incorrectly
    protected String errorMessage = IDEPropertiesMessages.PropertySource_readOnly;

    // Property Descriptors
    static protected IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[7];

    static protected IPropertyDescriptor[] propertyDescriptorsLinkVariable = new IPropertyDescriptor[8];
    static {
        PropertyDescriptor descriptor;

        // resource name
        descriptor = new PropertyDescriptor(IBasicPropertyConstants.P_TEXT,
                IResourcePropertyConstants.P_LABEL_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[0] = descriptor;
        propertyDescriptorsLinkVariable[0] = descriptor;

        // Relative path
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_PATH_RES,
                IResourcePropertyConstants.P_DISPLAYPATH_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[1] = descriptor;
        propertyDescriptorsLinkVariable[1] = descriptor;

        // readwrite state
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_EDITABLE_RES,
                IResourcePropertyConstants.P_DISPLAYEDITABLE_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[2] = descriptor;
        propertyDescriptorsLinkVariable[2] = descriptor;

        // derived state
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_DERIVED_RES,
                IResourcePropertyConstants.P_DISPLAYDERIVED_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[3] = descriptor;
        propertyDescriptorsLinkVariable[3] = descriptor;

        // last modified state
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_LAST_MODIFIED_RES,
                IResourcePropertyConstants.P_DISPLAY_LAST_MODIFIED);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[4] = descriptor;
        propertyDescriptorsLinkVariable[4] = descriptor;

        // link state
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_LINKED_RES,
                IResourcePropertyConstants.P_DISPLAYLINKED_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[5] = descriptor;
        propertyDescriptorsLinkVariable[5] = descriptor;

        // location
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_LOCATION_RES,
                IResourcePropertyConstants.P_DISPLAYLOCATION_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptors[6] = descriptor;
        propertyDescriptorsLinkVariable[6] = descriptor;

        // resolved location
        descriptor = new PropertyDescriptor(
                IResourcePropertyConstants.P_RESOLVED_LOCATION_RES,
                IResourcePropertyConstants.P_DISPLAYRESOLVED_LOCATION_RES);
        descriptor.setAlwaysIncompatible(true);
        descriptor
                .setCategory(IResourcePropertyConstants.P_FILE_SYSTEM_CATEGORY);
        propertyDescriptorsLinkVariable[7] = descriptor;

    }

    /**
     * Creates a PropertySource and stores its IResource
     *
     * @param res the resource for which this is a property source
     */
    public ResourcePropertySource(IResource res) {
        Assert.isNotNull(res);
        this.element = res;
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
        if (isPathVariable(element)) {
			return propertyDescriptorsLinkVariable;
		}
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
            return TextProcessor.process(element.getFullPath().toString());
        }
        if (name.equals(IResourcePropertyConstants.P_LAST_MODIFIED_RES)) {
            return IDEResourceInfoUtils.getDateStringValue(element);
        }
        if (name.equals(IResourcePropertyConstants.P_EDITABLE_RES)) {
            final ResourceAttributes attributes = element.getResourceAttributes();
			if (attributes == null || attributes.isReadOnly()) {
				return IDEPropertiesMessages.ResourceProperty_false;
			} 
			return IDEPropertiesMessages.ResourceProperty_true;
        }
        if (name.equals(IResourcePropertyConstants.P_DERIVED_RES)) {
            if (element.isDerived())
            	return IDEPropertiesMessages.ResourceProperty_true;
        	return IDEPropertiesMessages.ResourceProperty_false;
        }
        if (name.equals(IResourcePropertyConstants.P_LINKED_RES)) {
        	if (element.isLinked())
        		return IDEPropertiesMessages.ResourceProperty_true;
        	return IDEPropertiesMessages.ResourceProperty_false;
        }
        if (name.equals(IResourcePropertyConstants.P_LOCATION_RES)) {
            return TextProcessor.process(IDEResourceInfoUtils.getLocationText(element));
        }
        if (name.equals(IResourcePropertyConstants.P_RESOLVED_LOCATION_RES)) {
            return TextProcessor.process(IDEResourceInfoUtils.getResolvedLocationText(element));
        }
        return null;
    }

    /**
     * Returns whether the given resource is a linked resource bound 
     * to a path variable.
     * 
     * @param resource resource to test
     * @return boolean <code>true</code> the given resource is a linked 
     * 	resource bound to a path variable. <code>false</code> the given 
     * 	resource is either not a linked resource or it is not using a
     * 	path variable.  
     */
    private boolean isPathVariable(IResource resource) {
        if (!resource.isLinked()) {
			return false;
		}

        IPath resolvedLocation = resource.getLocation();
        if (resolvedLocation == null) {
            // missing path variable
            return true;
        }
        IPath rawLocation = resource.getRawLocation();
        if (resolvedLocation.equals(rawLocation)) {
			return false;
		}

        return true;
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
    public void resetPropertyValue(Object property) {
    }

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
     * <code>null</code>
     * @param resource the resource to lookup
     * @return java.io.File or <code>null</code>.
     */
    protected File getFile(IResource resource) {
        IPath location = resource.getLocation();
        if (location == null) {
			return null;
		}
		return location.toFile();
    }

}
