/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.beans;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.IUpdatableTree;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.NestedProperty;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.TreeModelDescription;
import org.eclipse.jface.internal.databinding.ListUpdatableCollection;
import org.eclipse.jface.internal.databinding.NestedUpdatableCellProvider;
import org.eclipse.jface.internal.databinding.NestedUpdatableCollection;
import org.eclipse.jface.internal.databinding.NestedUpdatableTree;
import org.eclipse.jface.internal.databinding.NestedUpdatableValue;

public class NestedUpdatableFactory implements IUpdatableFactory {
	public IUpdatable createUpdatable(Map properties,
			Object description, IDataBindingContext bindingContext) {
		if (description instanceof NestedProperty) {
			return createNestedUpdatable((NestedProperty)description, bindingContext);
		} else if (description instanceof Property) {
			Property propertyDescription = (Property) description;
			Object o = propertyDescription.getObject();
			if (o instanceof IUpdatableValue) {
				IUpdatableValue updatableValue = (IUpdatableValue) o;
				Class propertyType = propertyDescription
						.getPropertyType();
				if (propertyType == null) {
					throw new BindingException(
							"Missing required property type for binding to a property of an IUpdatableValue."); //$NON-NLS-1$
				}
				Boolean isCollectionProperty = propertyDescription
						.isCollectionProperty();
				if (isCollectionProperty == null) {
					throw new BindingException(
							"Missing required property collection information for binding to a property of an IUpdatableValue."); //$NON-NLS-1$
				}
				Object propertyID = propertyDescription.getPropertyID();
				if (isCollectionProperty.booleanValue()) {
					return new NestedUpdatableCollection(
							bindingContext, updatableValue,
							propertyID, propertyType);
				}
				return new NestedUpdatableValue(
						bindingContext, updatableValue,
						propertyID, propertyType);
			} else if (o instanceof List) {
				return new ListUpdatableCollection(
						(List) o,
						propertyDescription.getPropertyType() == null ? Object.class
								: propertyDescription.getPropertyType());
			}
		} else if (description instanceof TreeModelDescription) {
			TreeModelDescription treeModelDescription = (TreeModelDescription) description;
			if (treeModelDescription.getRoot() != null) {
				if (treeModelDescription.getRoot() instanceof IUpdatable) {
					if (treeModelDescription.getRoot() instanceof IUpdatableTree)
						return (IUpdatableTree) treeModelDescription
								.getRoot();
					// Nest the TreeModelDescription's root
					return new NestedUpdatableTree(
							bindingContext,
							treeModelDescription);
				} else if (treeModelDescription.getRoot() instanceof Property) {
					// Create an Updatable for the
					// TreeModelDescription's root first
					TreeModelDescription newDescription = new TreeModelDescription(
							bindingContext
									.createUpdatable(treeModelDescription
											.getRoot()));
					Class[] types = treeModelDescription.getTypes();
					for (int i = 0; i < types.length; i++) {
						String[] props = treeModelDescription
								.getChildrenProperties(types[i]);
						for (int j = 0; j < props.length; j++)
							newDescription.addChildrenProperty(
									types[i], props[j]);
					}
					return bindingContext
							.createUpdatable(newDescription);
				}
			}
			return null;
		} else if (description instanceof TableModelDescription) {
			TableModelDescription tableModelDescription = (TableModelDescription) description;
			Object master = tableModelDescription.getCollectionProperty().getObject();
			if(master instanceof IUpdatableValue) {
				return new NestedUpdatableCellProvider(bindingContext,(IUpdatableValue) master,tableModelDescription);
			}
		}
		return null;
	}
	
	private IUpdatable createNestedUpdatable(NestedProperty nestedProperty, IDataBindingContext bindingContext) {
		IUpdatable lastChildUpdatable = null;
		Object targetObject = nestedProperty.getObject();
		if (nestedProperty.getPrototypeClass() != null) {
			Class targetClazz = nestedProperty.getPrototypeClass();
			StringTokenizer tokenizer = new StringTokenizer((String) nestedProperty.getPropertyID(), "."); //$NON-NLS-1$
			while (tokenizer.hasMoreElements()) {
				String nextDesc = (String) tokenizer.nextElement();
				try {
					BeanInfo beanInfo = Introspector.getBeanInfo(targetClazz);
					PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
					Class discoveredClazz = null;
					for (int i = 0; i < propertyDescriptors.length; i++) {
						PropertyDescriptor descriptor = propertyDescriptors[i];
						if (descriptor.getName().equals(
								nextDesc)) {
							discoveredClazz = descriptor.getPropertyType();
							break;
						}
					}
					if (discoveredClazz != null) {
						targetClazz = discoveredClazz;
					} else {
						throw new BindingException("Error using prototype class to determine binding types."); //$NON-NLS-1$
					}
				} catch (BindingException be) {
					throw be;
				} catch (Exception e) {
					e.printStackTrace();
					throw new BindingException("Exeception using prototype class to determine binding types.", e); //$NON-NLS-1$
				}
				lastChildUpdatable = bindingContext.createUpdatable(new Property(targetObject,
						nextDesc, targetClazz, new Boolean(false)));
				targetObject = lastChildUpdatable;
			}

		} else {
			String[] properties = (String[]) nestedProperty.getPropertyID();
			for (int i = 0; i < properties.length; i++) {
				String nextDesc = properties[i];
				Class clazz = nestedProperty.getTypes()[i];
				lastChildUpdatable = bindingContext.createUpdatable(new Property(targetObject,
						nextDesc, clazz, new Boolean(false)));
				targetObject = lastChildUpdatable;
			}
		}
		return lastChildUpdatable;
	}
}