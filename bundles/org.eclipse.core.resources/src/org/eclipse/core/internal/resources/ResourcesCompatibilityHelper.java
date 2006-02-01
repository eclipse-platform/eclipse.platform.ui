/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.localstore.HistoryStore2;
import org.eclipse.core.internal.localstore.IHistoryStore;
import org.eclipse.core.internal.properties.IPropertyManager;
import org.eclipse.core.internal.properties.PropertyManager2;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * This is mostly a convenience class for accessing the ResourcesCompatibility class from the 
 * compatibility fragment using reflection.
 * 
 * See the ResourcesCompatibility class in the compatibility fragment.
 */
public class ResourcesCompatibilityHelper {
	private static final String COMPATIBILITY_CLASS = "org.eclipse.core.internal.resources.ResourcesCompatibility"; //$NON-NLS-1$
	private static final String CONVERT_HISTORY_STORE = ResourcesPlugin.PI_RESOURCES + ".convertHistory"; //$NON-NLS-1$
	private static final String CONVERT_PROPERTY_STORE = ResourcesPlugin.PI_RESOURCES + ".convertProperties"; //$NON-NLS-1$		
	private static final String ENABLE_NEW_HISTORY_STORE = ResourcesPlugin.PI_RESOURCES + ".newHistory"; //$NON-NLS-1$
	private static final String ENABLE_NEW_PROPERTY_STORE = ResourcesPlugin.PI_RESOURCES + ".newProperties"; //$NON-NLS-1$	

	/**
	 * Creates a history store. Decides which implementation of history store should be chosen, and whether
	 * conversion from the existing state should be performed by looking at some system properties. 
	 */
	public static IHistoryStore createHistoryStore(IPath location, int limit) {
		// the default is to use new implementation
		boolean newImpl = !Boolean.FALSE.toString().equalsIgnoreCase(System.getProperty(ENABLE_NEW_HISTORY_STORE));
		// the default is to convert existing state to the new implementation
		boolean convert = !Boolean.FALSE.toString().equalsIgnoreCase(System.getProperty(CONVERT_HISTORY_STORE));
		try {
			return createHistoryStore(location, limit, newImpl, convert, true);
		} catch (ClassNotFoundException e) {
			// fragment not available
		} catch (NoSuchMethodException e) {
			// unlikely
			if (Workspace.DEBUG)
				e.printStackTrace();
		} catch (IllegalAccessException e) {
			// unlikely
			if (Workspace.DEBUG)
				e.printStackTrace();
		} catch (InvocationTargetException e) {
			// got a runtime exception/error
			Throwable target = e.getTargetException();
			if (target instanceof RuntimeException)
				throw (RuntimeException) target;
			throw (Error) target;
		}
		// default to new version
		IFileStore store = EFS.getLocalFileSystem().getStore(location);
		return new HistoryStore2((Workspace) ResourcesPlugin.getWorkspace(), store, limit);
	}

	public static IHistoryStore createHistoryStore(IPath location, int limit, boolean newImpl, boolean convert, boolean rename) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class clazz = Class.forName(COMPATIBILITY_CLASS);
		Method createMethod = clazz.getDeclaredMethod("createHistoryStore", new Class[] {IPath.class, int.class, boolean.class, boolean.class, boolean.class}); //$NON-NLS-1$
		return (IHistoryStore) createMethod.invoke(null, new Object[] {location, new Integer(limit), Boolean.valueOf(newImpl), Boolean.valueOf(convert), Boolean.valueOf(rename)});
	}

	public static IPropertyManager createPropertyManager(boolean newImpl, boolean convert) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class clazz = Class.forName(COMPATIBILITY_CLASS);
		Method createMethod = clazz.getDeclaredMethod("createPropertyManager", new Class[] {boolean.class, boolean.class}); //$NON-NLS-1$
		return (IPropertyManager) createMethod.invoke(null, new Object[] {Boolean.valueOf(newImpl), Boolean.valueOf(convert)});
	}

	/**
	 * Creates a property manager. Decides which implementation of property manager should be chosen, and whether
	 * conversion from the existing state should be performed by looking at some system properties. 
	 */
	public static IPropertyManager createPropertyManager() {
		// the default is to use new implementation		
		boolean newImpl = !Boolean.FALSE.toString().equalsIgnoreCase(System.getProperty(ENABLE_NEW_PROPERTY_STORE));
		// the default is to convert existing state to the new implementation
		boolean convert = !Boolean.FALSE.toString().equalsIgnoreCase(System.getProperty(CONVERT_PROPERTY_STORE));
		try {
			return createPropertyManager(newImpl, convert);
		} catch (ClassNotFoundException e) {
			// fragment not available
		} catch (NoSuchMethodException e) {
			// unlikely
			if (Workspace.DEBUG)
				e.printStackTrace();
		} catch (IllegalAccessException e) {
			// unlikely
			if (Workspace.DEBUG)
				e.printStackTrace();
		} catch (InvocationTargetException e) {
			// got a runtime exception/error
			Throwable target = e.getTargetException();
			if (target instanceof RuntimeException)
				throw (RuntimeException) target;
			throw (Error) target;
		}
		// default to new version		
		return new PropertyManager2((Workspace) ResourcesPlugin.getWorkspace());
	}
}
