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
package org.eclipse.jface.internal.databinding.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.jface.databinding.BindingException;

/**
 * @since 3.2
 *
 */
public class PropertyHelper {
	
	/**
	 * nested property seperator
	 * TODO discuss nesting with .
	 */
	public final static String PROPERTY_NESTING_SEPERATOR = ".";  //$NON-NLS-1$
	
	private final String   propertyString;
	private final Class    rootClass;
	private final String[] propertyList;
	 
	
	private Method[] getterList=null;
	private Method setter=null;
		

	/**
	 * @param property
	 * @param rootClass 
	 */
	public PropertyHelper(String property, Class rootClass) {		
		propertyString=property;
		StringTokenizer stk = new StringTokenizer(property, PROPERTY_NESTING_SEPERATOR);
		propertyList = new String [stk.countTokens()];		
		for(int i=0; stk.hasMoreElements(); i++)
			propertyList[i]=(String)stk.nextElement();
		this.rootClass=rootClass;
	}
		
	private Method getGetterMethod (Class c, String property) throws SecurityException, NoSuchMethodException {
		if (c==null || property==null) 
			return null;
		Method getter=null;
		// Try a vanilla getter
		try {
			getter = c.getMethod(
					"get"+ property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1), new Class[0]); //$NON-NLS-1$			
		} 
		catch (SecurityException e) {} 
		catch (NoSuchMethodException e) {}
		
		if (getter==null) {
			// vanilla boolean;
			try {
				getter = c.getMethod(
						"is"+ property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1), new Class[0]); //$NON-NLS-1$
			} 
			catch (SecurityException e) {} 
			catch (NoSuchMethodException e) {}
			
			if (getter==null)  // allow for method invocation
				getter =  c.getMethod(property, new Class[0]);			
		}
		
		if (getter!=null && !getter.isAccessible())
			getter.setAccessible(true);
			
		return getter;		
	}
	
	private Method getSetterMethod (Class c, String property, Class argType) throws SecurityException, NoSuchMethodException {
		if (c==null || property==null) 
			return null;
		Method setter=null;
		try {
			setter = c.getMethod(
					"set"+ property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1), new Class[] { argType }); //$NON-NLS-1$
		} 
		catch (SecurityException e) {} 
		catch (NoSuchMethodException e) {}
		if (setter==null)  // allow for method invocation
			setter =  c.getMethod(property, new Class[] { argType });
		
		if (setter!=null && !setter.isAccessible())
			setter.setAccessible(true);
				
		return setter;		
	}
	
	private Method[] getGetters(Class root) {
		if (getterList!=null)
			return getterList;
		
		getterList = new Method[propertyList.length];
		Method getter = null;
		for (int i = 0; i < propertyList.length; i++) {		
			try {
				getter = getGetterMethod(root, propertyList[i]);
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
				throw new BindingException("Invalid property: "+propertyString); //$NON-NLS-1$
			}
				
			root = getter.getReturnType();
			getterList[i]=getter;
		}							
		return getterList;
	}
	
	/**
	 * @return setter
	 */
	public Method getSetter() {
		if (setter!=null)
			return setter;
		
		Method[] list = getGetters(rootClass);
		Class target = list.length>1? list[list.length-2].getReturnType(): rootClass;
		try {
			setter = getSetterMethod(target, 
									propertyList[propertyList.length-1],
					                list[list.length-1].getReturnType());
		} catch (SecurityException e) {			
		} catch (NoSuchMethodException e) {					
		}
		return setter;
		
	}

	
	/**
	 * @param root
	 * @return value for the get property
	 */
	public Object get(Object root) {
		return get(root, propertyList.length-1);
	}
	
	private Object get(Object root, int length) {
		if (root == null)
			return null;

		Object result = root;
		Method list[] = getGetters(rootClass);
		try {
			for (int i = 0; i <= length; i++) {
				result = list[i].invoke(result, new Object[0]);
				if (result == null)
					break;
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}

		return result;
	}

	/**
	 * @return setter method
	 */
	public Method getGetter() {
		
		
		Method[] getters = getGetters(rootClass);
		return getters[getters.length-1];
	}
	
	
	/**
	 * @param root
	 * @param singleArg 
	 */
	public void set(Object root, Object singleArg) {
		Method setter = getSetter();
		Object target = getLeafTarget(root);
		if (setter!=null && target!=null)
			try {
				setter.invoke(target, new Object[] {singleArg});
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block				
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
	}
	
	/**
	 * @param root
	 * @return true if a setter is available (nested, and method)
	 */
	public boolean canSet(Object root) {
		return getSetter()!=null && get(root, propertyList.length-2)!=null;
	}
	
	/**
	 * @param root
	 * @return true if getter is available (nested element property may not be available)
	 */
	public boolean canGet(Object root) {
		return getGetter()!=null && get(root, propertyList.length-2)!=null;
	}
	
	/**
	 * @return final nested property
	 */
	public String getLeafProperty() {
		return propertyList[propertyList.length-1];
	}
	
	/**
	 * @param root
	 * @return (nested) target object 
	 */
	public Object getLeafTarget(Object root) {
		int index = propertyList.length-2;
		if (index<0) return root;
		return get(root, index);
	}
	
}
