/**
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Ed Merks, IBM - Initial API and implementation (bug 220843)
 */
package org.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A column label provider that returns label text and images based on a
 * {@link IPropertySourceProvider}, forwarding requests for text and image to
 * the label provider returned from the property descriptor for the given
 * property id.
 *
 * @see IPropertyDescriptor#getLabelProvider()
 *
 * @since 3.3
 *
 * @see PropertyEditingSupport
 */
public class PropertyColumnLabelProvider extends ColumnLabelProvider {
	protected IPropertySourceProvider propertySourceProvider;
	protected Object propertyID;

	/**
	 * Creates a new instance based on the given property source provider and
	 * property id.
	 *
	 * @param propertySourceProvider
	 *            the property source provider
	 * @param propertyID
	 *            the property id
	 */
	public PropertyColumnLabelProvider(
			IPropertySourceProvider propertySourceProvider, Object propertyID) {
		this.propertySourceProvider = propertySourceProvider;
		this.propertyID = propertyID;
	}

	@Override
	public String getText(Object object) {
		IPropertySource propertySource = propertySourceProvider
				.getPropertySource(object);
		IPropertyDescriptor[] propertyDescriptors = propertySource
				.getPropertyDescriptors();
		for (IPropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (propertyID.equals(propertyDescriptor.getId())) {
				return propertyDescriptor.getLabelProvider().getText(
						propertySource.getPropertyValue(propertyID));
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image getImage(Object object) {
		IPropertySource propertySource = propertySourceProvider
				.getPropertySource(object);
		IPropertyDescriptor[] propertyDescriptors = propertySource
				.getPropertyDescriptors();
		for (IPropertyDescriptor propertyDescriptor : propertyDescriptors) {
			if (propertyID.equals(propertyDescriptor.getId())) {
				return propertyDescriptor.getLabelProvider().getImage(
						propertySource.getPropertyValue(propertyID));
			}
		}
		return null;
	}
}
