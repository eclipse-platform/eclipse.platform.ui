/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IReadableList;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.ITree;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.TreeModelDescription;
import org.eclipse.jface.databinding.updatables.ListToSetAdapter;
import org.eclipse.jface.internal.databinding.beans.JavaBeanTree;
import org.eclipse.jface.internal.databinding.beans.JavaBeanUpdatableCollection;
import org.eclipse.jface.internal.databinding.beans.JavaBeanUpdatableTree;
import org.eclipse.jface.internal.databinding.beans.JavaBeanUpdatableValue;
import org.eclipse.jface.internal.databinding.beans.JavaBeansUpdatableCellProvider;

/**
 * A factory for creating updatable objects for properties of plain Java objects
 * with JavaBeans-style notification.
 * 
 * This factory supports the following description objects:
 * <ul>
 * <li>org.eclipse.jface.databinding.PropertyDescription:
 * <ul>
 * <li>Returns an updatable value representing the specified value property of
 * the given object, if {@link Property#isCollectionProperty()}
 * returns false</li>
 * <li>Returns an updatable collection representing the specified collection
 * property of the given object, if
 * {@link Property#isCollectionProperty()} returns false</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @since 3.2
 * 
 */
final public class BeanUpdatableFactory implements IUpdatableFactory {

	public IUpdatable createUpdatable(Map properties, Object description,
			IDataBindingContext bindingContext) {
		if (description instanceof Property) {
			Property propertyDescription = (Property) description;
			if (propertyDescription.getObject() != null) {
				Object object = propertyDescription.getObject();
				BeanInfo beanInfo;
				try {
					beanInfo = Introspector.getBeanInfo(object.getClass());
				} catch (IntrospectionException e) {
					// cannot introspect, give up
					return null;
				}
				PropertyDescriptor[] propertyDescriptors = beanInfo
						.getPropertyDescriptors();
				for (int i = 0; i < propertyDescriptors.length; i++) {
					PropertyDescriptor descriptor = propertyDescriptors[i];
					if (descriptor.getName().equals(
							propertyDescription.getPropertyID())) {
						if (descriptor.getPropertyType().isArray() || Collection.class.isAssignableFrom(descriptor.getPropertyType())){
							// If we are a collection them the type must be explicitly specified in order to be edited.
							// There is no way to derive it by name matching (because of different ways of plurals being made, 
							// e.g. getFlies() and addFly(Fly aFly) or getHooves() and addHoof(Hoof aHoof)
							Class elementType = descriptor.getPropertyType().isArray() ?
									descriptor.getPropertyType().getComponentType() :
									propertyDescription.getPropertyType();
							if (elementType == null) {
								// If we don't know the element type, use the polymorphic converter
								// This should usually get us object to string, but won't get us string
								// back to the object.  At least this handles the read-only case for
								// tables and handles Combos and Lists.
								elementType = Object.class;
							}
							/*
							 * FIXME: CopyOfJavaBeanUpdatableCollection is the new IUpdatableCollection
							 * that fixes bug #119930.  It seems to work now.  Unfortunately, there is
							 * a bunch of code that depends on the IUpdatableCollections themselves
							 * listening to the generic change event on each of the collections' elements.
							 * 
							 * Examples of this include StructuredViewerUpdatableValue and the table
							 * implementations.  These need to be fixed to use IUpdatableValues
							 * pointing to the specific properties being displayed/edited rather than
							 * expecting the IUpdatableCollection to be registered to the generic 
							 * property change event.
							 */
							return new JavaBeanUpdatableCollection(object,
									descriptor, elementType);
//							return new CopyOfJavaBeanUpdatableCollection(object, descriptor, elementType);
						}						
						return new JavaBeanUpdatableValue(object, descriptor);
					}
				}
			}
		}
		else if (description instanceof ITree)
			return new JavaBeanUpdatableTree((ITree)description);
		else if (description instanceof TreeModelDescription) {
			Object root = ((TreeModelDescription)description).getRoot();			
			if (root==null || ! (root instanceof IUpdatable) && !(root instanceof Property)) // TODO workaround until the context's factory is driven first		     			
			    return new JavaBeanUpdatableTree(new JavaBeanTree((TreeModelDescription)description));						 							
		} else if (description instanceof TableModelDescription) {
			TableModelDescription tableModelDescription = (TableModelDescription) description;
			IUpdatable collectionUpdatable = bindingContext.createUpdatable(tableModelDescription.getCollectionProperty());
			if (collectionUpdatable == null) {
				return null;
			}
			IReadableSet readableSet;
			if (collectionUpdatable instanceof IReadableSet) {
				readableSet = (IReadableSet) collectionUpdatable;
			} else if (collectionUpdatable instanceof IReadableList) {
				readableSet = new ListToSetAdapter((IReadableList) collectionUpdatable);
			} else {
				throw new BindingException("collection inside a TableModelDescription needs to be IReadableSet or IReadableList"); //$NON-NLS-1$
			}
			Object[] columnIDs = tableModelDescription.getColumnIDs();
			String[] propertyNames = new String[columnIDs.length];
			for (int i = 0; i < propertyNames.length; i++) {
				propertyNames[i] = (String) columnIDs[i];
			}
			return new JavaBeansUpdatableCellProvider(readableSet, propertyNames);
		}
		return null;
	}
}
