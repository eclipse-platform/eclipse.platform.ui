/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.views.properties.tabbed.Activator;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.views.DynamicTestsView;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

/**
 * An element for the dynamic tests view. We build an element for one of the
 * icons. For example, "icons/blue_circle.gif" becomes a "Blue Circle" element.
 * 
 * @author Anthony Hunter
 */
public class DynamicTestsElement implements
		ITabbedPropertySheetPageContributor, IPropertySource {

	public static String ADVANCED_CATEGORY = "Advanced"; //$NON-NLS-1$

	public static String DEFAULT_CATEGORY = "Default"; //$NON-NLS-1$

	public static String ID_COLOR = "Color"; //$NON-NLS-1$

	public static String ID_IMAGE = "Image"; //$NON-NLS-1$

	public static String ID_NAME = "Name"; //$NON-NLS-1$

	public static String ID_SHAPE = "Shape"; //$NON-NLS-1$

	private DynamicTestsColor dynamicTestsColor;

	private Image image;

	private String name;

	private String path;

	private List propertyDescriptors = new ArrayList();

	private DynamicTestsShape shape;

	private DynamicTestsView view;

	public DynamicTestsElement(DynamicTestsView aView, String aPath) {
		super();
		this.view = aView;
		this.path = aPath;
		int slash = path.indexOf('/');
		Assert.isTrue(slash > 0);
		int underscore = path.indexOf('_');
		Assert.isTrue(underscore > 0);
		int dot = path.indexOf('.');
		Assert.isTrue(dot > 0);
		this.shape = DynamicTestsShape.getShape(path.substring(underscore + 1,
				dot));
		this.dynamicTestsColor = DynamicTestsColor.getColor(path.substring(
				slash + 1, underscore));
		this.image = Activator.getImageDescriptor(path).createImage();
		StringBuffer nameBuffer = new StringBuffer(path);
		nameBuffer.replace(slash + 1, slash + 2, path.substring(slash + 1,
				slash + 2).toUpperCase());
		nameBuffer.replace(underscore + 1, underscore + 2, path.substring(
				underscore + 1, underscore + 2).toUpperCase());
		nameBuffer.replace(underscore, underscore + 1, " ");//$NON-NLS-1$
		name = nameBuffer.substring(slash + 1, dot).toString();
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(ID_NAME,
				ID_NAME);
		propertyDescriptor.setCategory(DEFAULT_CATEGORY);
		propertyDescriptors.add(propertyDescriptor);
		propertyDescriptor = new PropertyDescriptor(ID_COLOR, ID_COLOR);
		propertyDescriptor.setCategory(DEFAULT_CATEGORY);
		propertyDescriptors.add(propertyDescriptor);
		propertyDescriptor = new PropertyDescriptor(ID_SHAPE, ID_SHAPE);
		propertyDescriptor.setCategory(DEFAULT_CATEGORY);
		propertyDescriptors.add(propertyDescriptor);
		propertyDescriptor = new PropertyDescriptor(ID_IMAGE, ID_IMAGE);
		propertyDescriptor.setCategory(ADVANCED_CATEGORY);
		propertyDescriptors.add(propertyDescriptor);
	}

	public String getContributorId() {
		return view.getContributorId();
	}

	public Object getEditableValue() {
		return this;
	}

	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return (IPropertyDescriptor[]) propertyDescriptors
				.toArray(new IPropertyDescriptor[0]);
	}

	public Object getPropertyValue(Object id) {
		if (id.equals(ID_COLOR)) {
			return dynamicTestsColor;
		} else if (id.equals(ID_SHAPE)) {
			return shape;
		} else if (id.equals(ID_NAME)) {
			return name;
		} else if (id.equals(ID_IMAGE)) {
			return path;
		}
		return null;
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public void resetPropertyValue(Object id) {
		// Not implemented, read only properties
	}

	public void setPropertyValue(Object id, Object value) {
		// Not implemented, read only properties
	}
}
