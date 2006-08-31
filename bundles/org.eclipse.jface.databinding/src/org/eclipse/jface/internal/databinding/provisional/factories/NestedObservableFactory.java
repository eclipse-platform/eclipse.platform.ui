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

package org.eclipse.jface.internal.databinding.provisional.factories;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.StringTokenizer;

import org.eclipse.jface.internal.databinding.internal.observable.NestedObservableList;
import org.eclipse.jface.internal.databinding.internal.observable.NestedObservableValue;
import org.eclipse.jface.internal.databinding.provisional.BindingException;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.NestedProperty;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;

/**
 * 
 * TODO Javadoc
 * 
 * @since 1.0
 * @deprecated no longer part of the API
 * 
 */
public class NestedObservableFactory implements IObservableFactory {

	private final DataBindingContext dataBindingContext;

	/**
	 * @param dataBindingContext
	 *            TODO
	 * 
	 */
	public NestedObservableFactory(DataBindingContext dataBindingContext) {
		this.dataBindingContext = dataBindingContext;
	}

	public IObservable createObservable(Object description) {
		if (description instanceof NestedProperty) {
			return createNestedObservable((NestedProperty) description,
					dataBindingContext);
		} else if (description instanceof Property) {
			Property propertyDescription = (Property) description;
			Object o = propertyDescription.getObject();
			if (o instanceof IObservableValue) {
				IObservableValue observableValue = (IObservableValue) o;
				Class propertyType = propertyDescription.getPropertyType();
				if (propertyType == null) {
					throw new BindingException(
							"Missing required property type for binding to a property of an IObservableValue."); //$NON-NLS-1$
				}
				Boolean isCollectionProperty = propertyDescription
						.isCollectionProperty();
				if (isCollectionProperty == null) {
					throw new BindingException(
							"Missing required property collection information for binding to a property of an IObservableValue."); //$NON-NLS-1$
				}
				Object propertyID = propertyDescription.getPropertyID();
				if (isCollectionProperty.booleanValue()) {
					return new NestedObservableList(dataBindingContext,
							observableValue, propertyID, propertyType);
				}
				return new NestedObservableValue(dataBindingContext,
						observableValue, propertyID, propertyType);
			}
			// else if (o instanceof List) {
			// return new ListObservableCollection(
			// (List) o,
			// propertyDescription.getPropertyType() == null ? Object.class
			// : propertyDescription.getPropertyType());
			// }
		}
		// else if (description instanceof TreeModelDescription) {
		// TreeModelDescription treeModelDescription = (TreeModelDescription)
		// description;
		// if (treeModelDescription.getRoot() != null) {
		// if (treeModelDescription.getRoot() instanceof IObservable) {
		// if (treeModelDescription.getRoot() instanceof IObservableTree)
		// return (IObservableTree) treeModelDescription
		// .getRoot();
		// // Nest the TreeModelDescription's root
		// return new NestedObservableTree(
		// bindingContext,
		// treeModelDescription);
		// } else if (treeModelDescription.getRoot() instanceof Property) {
		// // Create an Observable for the
		// // TreeModelDescription's root first
		// TreeModelDescription newDescription = new TreeModelDescription(
		// bindingContext
		// .createObservable(treeModelDescription
		// .getRoot()));
		// Class[] types = treeModelDescription.getTypes();
		// for (int i = 0; i < types.length; i++) {
		// String[] props = treeModelDescription
		// .getChildrenProperties(types[i]);
		// for (int j = 0; j < props.length; j++)
		// newDescription.addChildrenProperty(
		// types[i], props[j]);
		// }
		// return bindingContext
		// .createObservable(newDescription);
		// }
		// }
		// return null;
		// } else if (description instanceof TableModelDescription) {
		// TableModelDescription tableModelDescription = (TableModelDescription)
		// description;
		// Object master =
		// tableModelDescription.getCollectionProperty().getObject();
		// if(master instanceof IObservableValue) {
		// return new
		// NestedObservableCellProvider(bindingContext,(IObservableValue)
		// master,tableModelDescription);
		// }
		// }
		return null;
	}

	private IObservable createNestedObservable(NestedProperty nestedProperty,
			DataBindingContext bindingContext) {
		IObservable lastChildObservable = null;
		Object targetObject = nestedProperty.getObject();
		if (nestedProperty.getPrototypeClass() != null) {
			Class targetClazz = nestedProperty.getPrototypeClass();
			StringTokenizer tokenizer = new StringTokenizer(
					(String) nestedProperty.getPropertyID(), "."); //$NON-NLS-1$
			while (tokenizer.hasMoreElements()) {
				String nextDesc = (String) tokenizer.nextElement();
				try {
					BeanInfo beanInfo = Introspector.getBeanInfo(targetClazz);
					PropertyDescriptor[] propertyDescriptors = beanInfo
							.getPropertyDescriptors();
					Class discoveredClazz = null;
					for (int i = 0; i < propertyDescriptors.length; i++) {
						PropertyDescriptor descriptor = propertyDescriptors[i];
						if (descriptor.getName().equals(nextDesc)) {
							discoveredClazz = descriptor.getPropertyType();
							break;
						}
					}
					if (discoveredClazz != null) {
						targetClazz = discoveredClazz;
					} else {
						throw new BindingException(
								"Error using prototype class to determine binding types."); //$NON-NLS-1$
					}
				} catch (BindingException be) {
					throw be;
				} catch (Exception e) {
					e.printStackTrace();
					throw new BindingException(
							"Exeception using prototype class to determine binding types.", e); //$NON-NLS-1$
				}
				lastChildObservable = bindingContext
						.createObservable(new Property(targetObject, nextDesc,
								targetClazz, Boolean.FALSE));
				targetObject = lastChildObservable;
			}

		} else {
			String[] properties = (String[]) nestedProperty.getPropertyID();
			for (int i = 0; i < properties.length; i++) {
				String nextDesc = properties[i];
				Class clazz = nestedProperty.getTypes()[i];
				lastChildObservable = bindingContext
						.createObservable(new Property(targetObject, nextDesc,
								clazz, Boolean.FALSE));
				targetObject = lastChildObservable;
			}
		}
		return lastChildObservable;
	}
}