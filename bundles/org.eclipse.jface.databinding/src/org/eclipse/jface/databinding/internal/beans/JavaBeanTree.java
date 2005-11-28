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

package org.eclipse.jface.databinding.internal.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ITree;
import org.eclipse.jface.databinding.TreeModelDescription;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 *
 */
public class JavaBeanTree implements ITree {

	private TreeModelDescription modelDescription;
	
	private HashMap descriptors = new HashMap();
	
	/**
	 * @param modelDescripton
	 */
	public JavaBeanTree(TreeModelDescription modelDescripton) {
		this.modelDescription = modelDescripton;
	}
	
	private Class[] addType(Class instanceType, Class[] list) {
		if (list==null)
			return new Class[] { instanceType } ;
		
		for (int i = 0; i < list.length; i++) 
			if (list[i]==instanceType) return list;			
		
		Class[] newList = new Class[list.length+1];
		System.arraycopy(list,0, newList, 0, list.length);
		newList[list.length]=instanceType;
		return newList;
	}
	
	private Class[] getRelevantTypes(Class type) {
		Class[] list = modelDescription.getTypes();
		Class[] result = null;
		for (int i = 0; i < list.length; i++)
			if (list[i].isAssignableFrom(type))
				result = addType(list[i], result);
		return result;
	}
	
	private PropertyDescriptor[] addDescriptor(PropertyDescriptor desc, PropertyDescriptor[] list) {
		if (list==null)
			return new PropertyDescriptor[] { desc } ;
		for (int i = 0; i < list.length; i++) 
			if (list[i]==desc) return list;
		
		PropertyDescriptor[] newList = new PropertyDescriptor[list.length+1];
		System.arraycopy(list,0, newList, 0, list.length);
		newList[list.length]=desc;
		return newList;		
	}
		

	private PropertyDescriptor[]  getChildrenProperty(Class type, int paramCount) {
		String[] childrenProperties = modelDescription
				.getChildrenProperties(type);
		if (childrenProperties == null || childrenProperties.length == 0)
			return null;
		
		PropertyDescriptor[] desc = (PropertyDescriptor[]) descriptors.get(type);

		if (desc == null) {
			try {												
				BeanInfo info = Introspector.getBeanInfo(type);
				PropertyDescriptor[] list = info.getPropertyDescriptors();
				// Walk by properties to preserve the tree order
				for (int j = 0; j < childrenProperties.length; j++) {
					for (int k = 0; k < list.length; k++) {
						if (list[k].getName().equals(childrenProperties[j])) {
							desc = addDescriptor(list[k], desc);
							Class parameters[] = list[k].getReadMethod()
									.getParameterTypes();
							if (paramCount == 0)
								Assert.isTrue(parameters == null
										|| parameters.length == 0);
							else
								Assert.isTrue(parameters.length == paramCount);
						}
					}
				}
				if (desc!=null)
					descriptors.put(type, desc);

			} catch (IntrospectionException e) {
				return null;
			}
		}
		return desc;
	}
	
	// TODO deal with nested
	private Method[] getReadMethods(Object element) {
		PropertyDescriptor[] desc = getChildrenProperty(element.getClass(), 0);
		if (desc == null)
			return null;
		Method[] methods = new Method[desc.length];
		for (int i = 0; i < methods.length; i++) 
			methods[i] = desc[i]!=null ? desc[i].getReadMethod() : null;			
		return methods;
	}
	
	
	private Method[] getWriteMethods (Object element) {
		PropertyDescriptor[] desc = getChildrenProperty(element.getClass(), 1);
		Method[] methods = new Method[desc.length];
		for (int i = 0; i < methods.length; i++) 
		  methods[i] = desc[i]!=null ? desc[i].getWriteMethod() : null;
		  return methods;
	}
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement==null)
			return modelDescription.getRootObjects();
		
		Method[] getters = getReadMethods(parentElement);
		if (getters==null)
			return Collections.EMPTY_LIST.toArray();
		
		List children = new ArrayList();
		
		try {
			for (int i = 0; i < getters.length; i++) {
				if (getters[i].getReturnType().isArray())
					children.addAll(Arrays.asList((Object[]) getters[i].invoke(parentElement, new Object[0])));
				else
				    children.addAll((Collection) getters[i].invoke(parentElement, new Object[0]));
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block		
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block		
		} 
		return children.toArray();
	}

	public void setChildren(Object parentElement, Object[] children) {
		if (parentElement==null)
			modelDescription.setRootObjects(children);
		
		Method[] setters = getWriteMethods(parentElement);
		// TODO we can try to figure out (by type) which methods to invoke on which object
		if (setters==null || setters.length!=1)
			throw new BindingException("Can not determine children set method for: "+parentElement.getClass().getName()); //$NON-NLS-1$
		
		Method setter = setters[0];
		Class[] parameters = setter.getParameterTypes();
		Object arg;
		if (parameters[0].isArray())
			arg = children;
		else
			arg = Arrays.asList(children);
		
		try {
			setter.invoke(parentElement, new Object[] { arg } );
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block			
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
		}
	}

	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return (children!=null && children.length>0);
	}

	public Class[] getTypes() {		
		return modelDescription.getTypes();
	}

}
