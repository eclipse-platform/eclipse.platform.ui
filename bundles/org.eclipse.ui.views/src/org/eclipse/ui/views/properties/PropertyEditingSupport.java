/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Ed Merks, IBM - Initial API and implementation (bug 220843)
 *
 */
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * A concrete subclass of {@link EditingSupport} that implements cell editing
 * support for column-based viewers (such as e.g. {@link TreeViewer} or
 * {@link TableViewer}) based on an {@link IPropertySourceProvider},
 * forwarding requests to the {@link IPropertySource} or
 * {@link IPropertyDescriptor} for the given property ID.
 * 
 * @since 3.3
 * 
 * @see PropertyColumnLabelProvider
 */
public class PropertyEditingSupport extends EditingSupport {
	protected IPropertySourceProvider propertySourceProvider;
	protected Object propertyID;

	/**
	 * Creates a new instance to be used with the given viewer, based on the
	 * given property source provider and property ID.
	 * 
	 * @param viewer
	 *            the column viewer
	 * @param propertySourceProvider
	 *            the property source provider
	 * @param propertyID
	 *            the property ID
	 */
	public PropertyEditingSupport(ColumnViewer viewer,
			IPropertySourceProvider propertySourceProvider, Object propertyID) {
		super(viewer);
		this.propertySourceProvider = propertySourceProvider;
		this.propertyID = propertyID;
	}

	protected boolean canEdit(Object object) {
		IPropertySource propertySource = propertySourceProvider
				.getPropertySource(object);
		IPropertyDescriptor[] propertyDescriptors = propertySource
				.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			IPropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			if (propertyID.equals(propertyDescriptor.getId())) {
				return true;
			}
		}
		return false;
	}

	protected CellEditor getCellEditor(Object object) {
		IPropertySource propertySource = propertySourceProvider
				.getPropertySource(object);
		IPropertyDescriptor[] propertyDescriptors = propertySource
				.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			IPropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			if (propertyID.equals(propertyDescriptor.getId())) {
				return propertyDescriptor
						.createPropertyEditor((Composite) getViewer()
								.getControl());
			}
		}
		return null;
	}

	protected Object getValue(Object object) {
		IPropertySource propertySource = propertySourceProvider
				.getPropertySource(object);
		Object value = propertySource.getPropertyValue(propertyID);
		IPropertySource valuePropertySource = propertySourceProvider
				.getPropertySource(value);
		if (valuePropertySource != null) {
			value = valuePropertySource.getEditableValue();
		}
		return value;
	}

	protected void setValue(Object object, Object value) {
		IPropertySource propertySource = propertySourceProvider
				.getPropertySource(object);
		propertySource.setPropertyValue(propertyID, value);
	}
}
