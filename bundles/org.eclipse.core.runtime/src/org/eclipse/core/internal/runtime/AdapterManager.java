/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.core.internal.runtime;

import java.util.*;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * This class is a default implementation of <code>IExtenderManager</code>.
 * It provides fast lookup of property values with the following semantics:
 * <ul>
 * <li> At most one extender will be invoked per property lookup
 * <li> If multiple installed extenders provide the same property, only 
 *		the first found in the search order is said to <i>define</i> the property 
 *		as it is the only extender which will be invoked..
 * <li> The search order from a class with the definition<br>
 *			<code>class X extends Y implements A, B</code><br>
 *		is as follows:
 * 		<il>
 *			<li>the target's class: X
 *			<li>X's superclasses in order to <code>Object</code>
 *			<li>a depth-first traversal of the target class's interaces in the order 
 *				returned by <code>getInterfaces</code> (in the example, A and 
 *				its superinterfaces then B and its superinterfaces)
 *		</il>
 * </ul>
 *
 * @see IAdapter
 * @see IAdapterManager
 */
public final class AdapterManager implements IAdapterManager {

	/** Table of factories, keyed by <code>Class</code>. */
	protected Hashtable factories;

	/** Cache of adapter search paths; <code>null</code> if none. */
	protected Hashtable lookup;
/** Constructs a new extender manager.
 */
public AdapterManager() {
	factories = new Hashtable(5);
	lookup = null;
}
/**
 * Given a list of types, add all of the property entries for the installed
 * extenders into the lookupTable.  Each entry will be keyed by the property
 * identifier (supplied in IExtender.getPropertyList) and the extender
 * supplying that property.
 */
private void addFactoriesFor(Vector types, Hashtable lookupTable) {
	for (Enumeration classes = types.elements(); classes.hasMoreElements();) {
		Class clazz = (Class) classes.nextElement();
		Vector factoryList = (Vector) factories.get(clazz);
		if (factoryList == null)
			continue;
		for (Enumeration list = factoryList.elements(); list.hasMoreElements();) {
			IAdapterFactory factory = (IAdapterFactory) list.nextElement();
			Object[] adapters = factory.getAdapterList();
			for (int i = 0; i < adapters.length; i++) {
				Object adapter = adapters[i];
				if (lookupTable.get(adapter) == null)
					lookupTable.put(adapter, factory);
			}
		}
	}
}
/**
 * Returns the class search order starting with <code>extensibleClass</code>.
 * The search order is defined in this class' comment.
 */
private Vector computeClassOrder(Class extensibleClass) {
	Vector result = new Vector(4);
	Class clazz = extensibleClass;
	while (clazz != null) {
		result.addElement(clazz);
		clazz = clazz.getSuperclass();
	}
	return result;
}
/**
 * Returns the interface search order for the class hierarchy described
 * by <code>classList</code>.
 * The search order is defined in this class' comment.
 */
private Vector computeInterfaceOrder(Vector classList) {
	Vector result = new Vector(4);
	Hashtable seen = new Hashtable(4);
	for (Enumeration list = classList.elements(); list.hasMoreElements();) {
		Class[] interfaces = ((Class) list.nextElement()).getInterfaces();
		internalComputeInterfaceOrder(interfaces, result, seen);
	}
	return result;
}
/**
 * Flushes the cache of extender search paths.  This is generally required
 * whenever an extender is added or removed.  
 * <p>
 * It is likely easier to just toss the whole cache rather than trying to be
 * smart and remove only those entries affected.
 * </p>
 */
public void flushLookup() {
	lookup = null;
}
/*
 * @see IAdapterManager#getAdapter
 */
public Object getAdapter(Object object, Class target) {
	IAdapterFactory factory = getFactory(object.getClass(), target);
	Object result = null;
	if (factory != null)
		result = factory.getAdapter(object, target);
	if (result == null && target.isInstance(object))
		return object;
	return result;
}
/**
 * Gets the extender installed for objects of class <code>extensibleClass</code>
 * which defines the property identified by <code>key</code>.  If no such
 * extender exists, returns null.
 */
private IAdapterFactory getFactory(Class extensibleClass, Class adapter) {
	Hashtable table;
	// check the cache first.
	if (lookup != null) {
		table = (Hashtable) lookup.get(extensibleClass);
		if (table != null)
			return (IAdapterFactory) table.get(adapter);
	}
	// Its not in the cache so we have to build the extender table for this class.
	// The table is keyed by property identifier.  The 
	// value is the <b>sole<b> extender which defines that property.  Note that if
	// if multiple extenders technically define the same property, only the first found
	// in the search order is considered.
	table = new Hashtable(4);
	// get the list of all superclasses and add the extenders installed for each 
	// of those classes to the table.  
	Vector classList = computeClassOrder(extensibleClass);
	addFactoriesFor(classList, table);
	// get the ordered set of all interfaces for the extensible class and its 
	// superclasses and add the extenders installed for each 
	// of those interfaces to the table.  
	classList = computeInterfaceOrder(classList);
	addFactoriesFor(classList, table);
	//cache the table and do the lookup again.
	if (lookup == null)
		lookup = new Hashtable(5);
	lookup.put(extensibleClass, table);
	return (IAdapterFactory) table.get(adapter);
}
private void internalComputeInterfaceOrder(Class[] interfaces, Vector result, Hashtable seen) {
	Vector newInterfaces = new Vector(seen.size());
	for (int i = 0; i < interfaces.length; i++) {
		Class interfac = interfaces[i];
		if (seen.get(interfac) == null) {
			result.addElement(interfac);
			seen.put(interfac, interfac);
			newInterfaces.addElement(interfac);
		}
	}
	for (Enumeration newList = newInterfaces.elements(); newList.hasMoreElements();)
		internalComputeInterfaceOrder(((Class) newList.nextElement()).getInterfaces(), result, seen);
}
/*
 * @see IAdapterManager#registerAdapters
 */
public void registerAdapters(IAdapterFactory factory, Class extensibleType) {
	Vector list = (Vector) factories.get(extensibleType);
	if (list == null) {
		list = new Vector(5);
		factories.put(extensibleType, list);
	}
	list.addElement(factory);
	flushLookup();
}
/*
 * @see IAdapterManager#unregisterAdapters
 */
public void unregisterAdapters(IAdapterFactory factory) {
	for (Enumeration enum = factories.elements(); enum.hasMoreElements();) {
		Vector list = (Vector) enum.nextElement();
		list.removeElement(factory);
	}
	flushLookup();
}
/*
 * @see IAdapterManager#unregisterAdapters
 */
public void unregisterAdapters(IAdapterFactory factory, Class extensibleType) {
	Vector factoryList = (Vector) factories.get(extensibleType);
	if (factoryList == null)
		return;
	factoryList.removeElement(factory);
	flushLookup();
}
/*
 * @see IAdapterManager#unregisterAllAdapters
 */
public void unregisterAllAdapters() {
	factories = new Hashtable(5);
	flushLookup();
}
}
