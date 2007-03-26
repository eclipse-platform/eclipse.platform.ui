/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.HashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Contains an instance of IStatus subclass. Used during status handling
 * process. Can carry all additional information about status either by using
 * properties or by adding new adapter.
 * </p>
 * 
 * @since 3.3
 */
public class StatusAdapter implements IAdaptable {

	/**
	 * Common prefix for properties defined in this class.
	 */
	static final String PROPERTY_PREFIX = PlatformUI.PLUGIN_ID
			+ ".workbench.statusHandlers.adapters"; //$NON-NLS-1$

	/**
	 * This property is used to add title to the adapter. If the adapter is
	 * shown in a dialog, this property is used to create title of the dialog.
	 */
	public static final QualifiedName TITLE_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "title"); //$NON-NLS-1$

	/**
	 * This property is used to add timestamp to the adapter. If the adapter is
	 * shown in the UI, this property can be used for sorting and showing
	 * information about the time of status creation.
	 */
	public static final QualifiedName TIMESTAMP_PROPERTY = new QualifiedName(
			PROPERTY_PREFIX, "timestamp"); //$NON-NLS-1$

	private IStatus status;

	private HashMap properties;

	private HashMap adapters;

	/**
	 * Creates an instance of this class.
	 *
	 * @param status
	 *            the status set in the adapter, not null
	 */
	public StatusAdapter(IStatus status) {
		this.status = status;
	}

	/**
	 * Adds new adapter.
	 *
	 * @param adapter
	 *            the adapter class
	 * @param object
	 *            the adapter instance
	 */
	public void addAdapter(Class adapter, Object object) {
		if (adapters == null) {
			adapters = new HashMap();
		}
		adapters.put(adapter, object);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapters == null) {
			return null;
		}
		return adapters.get(adapter);
	}

	/**
	 * Returns the status set in the adapter.
	 * 
	 * @return Returns the status.
	 */
	public IStatus getStatus() {
		return status;
	}

	/**
	 * Sets a new status for this adapter.
	 * 
	 * @param status
	 *            The status to set.
	 */
	public void setStatus(IStatus status) {
		this.status = status;
	}

	/**
	 * Returns the value of the property of this adapter identified by the given
	 * key, or <code>null</code> if this adapter has no such property.
	 * 
	 * @param key
	 *            the name of the property
	 * @return the value of the property, or <code>null</code> if this job has
	 *         no such property
	 */
	public Object getProperty(QualifiedName key) {
		if (properties == null) {
			return null;
		}
		return properties.get(key);
	}

	/**
	 * Sets the value of the property of this adapter identified by the given
	 * key.
	 * 
	 * @param key
	 *            the qualified name of the property
	 * @param value
	 *            the value of the property,
	 */
	public void setProperty(QualifiedName key, Object value) {
		if (properties == null) {
			properties = new HashMap();
		}
		properties.put(key, value);
	}
}
