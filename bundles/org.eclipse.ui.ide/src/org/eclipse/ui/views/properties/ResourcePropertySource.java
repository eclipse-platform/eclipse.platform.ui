/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
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

    /**
     * Return the value for the date string for the timestamp of the supplied resource.
     * 
     * @param resource the resource to query
     * @return the date string for the resource 
     */
    private String getDateStringValue(IResource resource) {

        if (!resource.isLocal(IResource.DEPTH_ZERO))
            return NOT_LOCAL_TEXT;

        IPath location = resource.getLocation();
        if (location == null) {
            if (resource.isLinked())
                return UNDEFINED_PATH_VARIABLE;

            return FILE_NOT_FOUND;
        } else {
            File localFile = location.toFile();
            if (localFile.exists()) {
                DateFormat format = new SimpleDateFormat();
                return format.format(new Date(localFile.lastModified()));
            }
            return FILE_NOT_FOUND;
        }
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource.
     */
    public Object getEditableValue() {
        return this;
    }

    /**
     * Get the location of a resource
     */
    private String getLocationText(IResource resource) {
        if (!resource.isLocal(IResource.DEPTH_ZERO))
            return NOT_LOCAL_TEXT;

        IPath resolvedLocation = resource.getLocation();
        IPath location = resolvedLocation;
        if (resource.isLinked()) {
            location = resource.getRawLocation();
        }
        if (location == null) {
            return FILE_NOT_FOUND;
        } else {
            String locationString = location.toOSString();
            if (resolvedLocation != null && !isPathVariable(resource)) {
                // No path variable used. Display the file not exist message 
                // in the location. Fixes bug 33318. 
                File file = resolvedLocation.toFile();
                if (!file.exists()) {
                    locationString += " " + FILE_NOT_EXIST_TEXT; //$NON-NLS-1$ 
                }
            }
            return locationString;
        }
    }

    /**
     * Get the resolved location of a resource.
     * This resolves path variables if present in the resource path.
     */
    private String getResolvedLocationText(IResource resource) {
        if (!resource.isLocal(IResource.DEPTH_ZERO))
            return NOT_LOCAL_TEXT;

        IPath location = resource.getLocation();
        if (location == null) {
            if (resource.isLinked())
                return UNDEFINED_PATH_VARIABLE;

            return FILE_NOT_FOUND;
        } else {
            String locationString = location.toOSString();
            File file = location.toFile();

            if (!file.exists()) {
                locationString += " " + FILE_NOT_EXIST_TEXT; //$NON-NLS-1$ 
            }
            return locationString;
        }
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource.
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (isPathVariable(element))
            return propertyDescriptorsLinkVariable;
        else
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
                return IDEPropertiesMessages.ResourceProperty_false;
            else
                return IDEPropertiesMessages.ResourceProperty_true;
        }
        if (name.equals(IResourcePropertyConstants.P_DERIVED_RES)) {
            return String.valueOf(element.isDerived());
        }
        if (name.equals(IResourcePropertyConstants.P_LINKED_RES)) {
            return String.valueOf(element.isLinked());
        }
        if (name.equals(IResourcePropertyConstants.P_LOCATION_RES)) {
            return getLocationText(element);
        }
        if (name.equals(IResourcePropertyConstants.P_RESOLVED_LOCATION_RES)) {
            return getResolvedLocationText(element);
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
        if (!resource.isLinked())
            return false;

        IPath resolvedLocation = resource.getLocation();
        if (resolvedLocation == null) {
            // missing path variable
            return true;
        }
        IPath rawLocation = resource.getRawLocation();
        if (resolvedLocation.equals(rawLocation))
            return false;

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
        if (location == null)
            return null;
        else
            return location.toFile();
    }

}
