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
package org.eclipse.ui.examples.propertysheet;

import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * An Organization Element
 */
public abstract class OrganizationElement implements IAdaptable,
        IPropertySource, IWorkbenchAdapter {
    //
    private GroupElement parent;

    private String name;

    private ImageDescriptor imageDescriptor;

    //
    private static Vector descriptors;
    static {
        descriptors = new Vector();
        PropertyDescriptor name = new TextPropertyDescriptor(
                IBasicPropertyConstants.P_TEXT, MessageUtil.getString("name")); //$NON-NLS-1$
        descriptors.addElement(name);
    }

    /**
     * Constructor.
     * Creates a new OrganizationElement within the passed parent GroupElement,
     *
     * @param name the name 
     * @param parent the parent
     */
    OrganizationElement(String name, GroupElement parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Deletes this OrganizationElement from its parentGroup
     */
    public void delete() {
        parent.delete(this);
    }

    /* (non-Javadoc)
     * Method declared on IAdaptable
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return this;
        }
        if (adapter == IWorkbenchAdapter.class) {
            return this;
        }
        return null;
    }

    /**
     * Returns the descriptors
     */
    static Vector getDescriptors() {
        return descriptors;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public Object getEditableValue() {
        return this;
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return imageDescriptor;
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public String getLabel(Object o) {
        return getName();
    }

    /**
     * Returns the name
     */
    String getName() {
        return name;
    }

    /**
     * Returns the parent
     */
    public Object getParent(Object o) {
        return parent;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) getDescriptors().toArray(
                new IPropertyDescriptor[getDescriptors().size()]);
    }

    /** 
     * The <code>OrganizationElement</code> implementation of this
     * <code>IPropertySource</code> method returns the following properties
     *
     * 	1) P_NAME returns String, name of this element
     *  this property key is defined in <code>IBasicPropertyConstants</code>
     */
    public Object getPropertyValue(Object propKey) {
        if (propKey.equals(IBasicPropertyConstants.P_TEXT))
            return getName();
        return null;
    }

    /**
     * Hook. Implemented by <code>GroupElement</code> for use instead of instanceof
     * @return boolean
     */
    public boolean isGroup() {
        return false;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public boolean isPropertySet(Object property) {
        return false;
    }

    /**
     * Hook. Implemented by <code>UserElement</code> for use instead of instanceof
     * @return boolean
     */
    public boolean isUser() {
        return false;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public void resetPropertyValue(Object property) {
    }

    /**
     * Sets the image descriptor
     */
    void setImageDescriptor(ImageDescriptor desc) {
        imageDescriptor = desc;
    }

    /**
     * Sets the name
     */
    void setName(String newName) {
        name = newName;
    }

    /**
     * Sets this instance's parent back pointer.
     */
    void setParent(GroupElement newParent) {
        parent = newParent;
    }

    /** 
     * The <code>OrganizationElement</code> implementation of this
     * <code>IPropertySource</code> method returns the following properties
     * defines the following Setable properties
     *
     *	1) P_NAME, expects String, sets the name of this OrganizationElement
     */
    public void setPropertyValue(Object name, Object value) {
        if (name.equals(IBasicPropertyConstants.P_TEXT)) {
            setName((String) value);
            return;
        }
    }
}
