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

import org.eclipse.ui.internal.views.properties.IDEPropertiesMessages;

/**
 * This interface documents the property constants used by the resource
 * property source.
 */
public interface IResourcePropertyConstants {
    /** 
     * The <code>IResource</code> property key for name.
     */
    public static final String P_LABEL_RES = IDEPropertiesMessages.IResourcePropertyConstants_name;

    /** 
     * The <code>IResource</code> property key for path.
     */
    public static final String P_PATH_RES = "org.eclipse.ui.path"; //$NON-NLS-1$

    /** 
     * The <code>IResource</code> property key for display path.
     */
    public static final String P_DISPLAYPATH_RES = IDEPropertiesMessages.IResourcePropertyConstants_path;

    /** 
     * The <code>IResource</code> property key for read-only.
     */
    public static final String P_EDITABLE_RES = "org.eclipse.ui.editable"; //$NON-NLS-1$

    /** 
     * The <code>IResource</code> property key for display read-only.
     */
    public static final String P_DISPLAYEDITABLE_RES = IDEPropertiesMessages.IResourcePropertyConstants_editable;

    /** 
     * The <code>IResource</code> property key for read-only.
     */
    public static final String P_DERIVED_RES = "org.eclipse.ui.derived"; //$NON-NLS-1$

    /** 
     * The <code>IResource</code> property key for display read-only.
     */
    public static final String P_DISPLAYDERIVED_RES = IDEPropertiesMessages.IResourcePropertyConstants_derived;

    /** 
     * The <code>IResource</code> property key for location.
     */
    public static final String P_LOCATION_RES = "org.eclipse.ui.location"; //$NON-NLS-1$

    /** 
     * The <code>IResource</code> property key for display location.
     */
    public static final String P_DISPLAYLOCATION_RES = IDEPropertiesMessages.IResourcePropertyConstants_location;

    /** 
     * The <code>IResource</code> property key for resolved location.
     */
    public static final String P_RESOLVED_LOCATION_RES = "org.eclipse.ui.resolvedLocation"; //$NON-NLS-1$,

    /** 
     * The <code>IResource</code> property key for display resolved location.
     */
    public static final String P_DISPLAYRESOLVED_LOCATION_RES = IDEPropertiesMessages.IResourcePropertyConstants_resolvedLocation;

    /** 
     * The <code>IResource</code> property key for linked.
     */
    public static final String P_LINKED_RES = "org.eclipse.ui.linked"; //$NON-NLS-1$,

    /** 
     * The <code>IResource</code> property key for display linked.
     */
    public static final String P_DISPLAYLINKED_RES = IDEPropertiesMessages.IResourcePropertyConstants_linked;

    /**
     * The <code>IResource</code> category for the base values
     */
    public static final String P_FILE_SYSTEM_CATEGORY = IDEPropertiesMessages.IResourcePropertyConstants_info;

    /** 
     * The <code>IResource</code> property key for path.
     */
    public static final String P_SIZE_RES = "org.eclipse.ui.size"; //$NON-NLS-1$

    /**
     * The <code>IResource</code> property key for displaying size
     */
    public static final String P_DISPLAY_SIZE = IDEPropertiesMessages.IResourcePropertyConstants_size;

    /** 
     * The <code>IResource</code> property key for path.
     */
    public static final String P_LAST_MODIFIED_RES = "org.eclipse.ui.lastmodified"; //$NON-NLS-1$

    /**
     * The <code>IResource</code> category for last modified
     */
    public static final String P_DISPLAY_LAST_MODIFIED = IDEPropertiesMessages.IResourcePropertyConstants_lastModified;

}
