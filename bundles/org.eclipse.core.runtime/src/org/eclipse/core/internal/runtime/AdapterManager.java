/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;
import java.util.*;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
/**
 * This class is a default implementation of <code>IAdapterManager</code>. It provides
 * fast lookup of property values with the following semantics:
 * <ul>
 * <li>At most one factory will be invoked per property lookup
 * <li>If multiple installed factories provide the same adapter, only the first found in
 * the search order will be invoked.
 * <li>The search order from a class with the definition <br>
 * <code>class X extends Y implements A, B</code><br>
 * is as follows: <il>
 * <li>the target's class: X
 * <li>X's superclasses in order to <code>Object</code>
 * <li>a depth-first traversal of the target class's interfaces in the order returned by
 * <code>getInterfaces</code> (in the example, A and its superinterfaces then B and its
 * superinterfaces) </il>
 * </ul>
 * 
 * @see IAdapter
 * @see IAdapterManager
 */
public final class AdapterManager implements IAdapterManager {
	/**
	 * Map of factories, keyed by <code>String</code>, fully qualified class name of
	 * the adaptable class that the factory provides adapters for. Value is a <code>List</code>
	 * of <code>IAdapterFactory</code>.
	 */
	protected final HashMap factories;
	/**
	 * An adapter factory that searches in the set of unloaded plug-ins to
	 * see if they have an adapter of the required type.
	 */
	protected ExtensionAdapterFactory extensionFactory;
	/** 
	 * Cache of adapter search paths; <code>null</code> if none. 
	 */
	protected HashMap lookup;
	/**
	 * Constructs a new adapter manager.
	 */
	public AdapterManager() {
		factories = new HashMap(5);
		lookup = null;
		
	}
	/**
	 * Given a list of types, add all of the factories that respond to those types into
	 * the lookupTable. Each entry will be keyed by the adapter class name (supplied in
	 * IAdapterFactory.getAdapterList).
	 */
	private void addFactoriesFor(List types, Map lookupTable) {
		for (Iterator classes = types.iterator(); classes.hasNext();) {
			Class clazz = (Class) classes.next();
			List factoryList = (List) factories.get(clazz);
			if (factoryList == null)
				continue;
			for (Iterator list = factoryList.iterator(); list.hasNext();) {
				IAdapterFactory factory = (IAdapterFactory) list.next();
				Class[] adapters = factory.getAdapterList();
				for (int i = 0; i < adapters.length; i++) 
					if (lookupTable.get(adapters[i]) == null)
						lookupTable.put(adapters[i], factory);
			}
		}
	}
	/**
	 * Returns the class search order starting with <code>extensibleClass</code>. The
	 * search order is defined in this class' comment.
	 */
	private List computeClassOrder(Class extensibleClass) {
		List result = new ArrayList(4);
		Class clazz = extensibleClass;
		while (clazz != null) {
			result.add(clazz);
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	/**
	 * Returns the interface search order for the class hierarchy described by <code>classList</code>.
	 * The search order is defined in this class' comment.
	 */
	private List computeInterfaceOrder(List classList) {
		List result = new ArrayList(4);
		Set seen = new HashSet(4);
		for (Iterator list = classList.iterator(); list.hasNext();) {
			Class[] interfaces = ((Class) list.next()).getInterfaces();
			internalComputeInterfaceOrder(interfaces, result, seen);
		}
		return result;
	}
	/**
	 * Flushes the cache of adapter search paths. This is generally required whenever an
	 * adapter is added or removed.
	 * <p>
	 * It is likely easier to just toss the whole cache rather than trying to be smart
	 * and remove only those entries affected.
	 * </p>
	 */
	public synchronized void flushLookup() {
		lookup = null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterManager#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public synchronized Object getAdapter(Object adaptable, Class adapterType) {
		IAdapterFactory factory = getFactory(adaptable.getClass(), adapterType);
		Object result = null;
		if (factory != null)
			result = factory.getAdapter(adaptable, adapterType);
		if (result == null && adapterType.isInstance(adaptable))
			return adaptable;
		return result;
	}
	/**
	 * Returns the class with the given fully qualified name, or null
	 * if that class does not exist or belongs to a plug-in that has not
	 * yet been loaded.
	 */
	private Class classForName(String typeName) {
		try {
			return Class.forName(typeName);
		} catch (ClassNotFoundException e) {
			//class not yet loaded
			return null;
		}
	}
	/**
	 * Gets the adapter factory installed for objects of class <code>extensibleClass</code>
	 * which defines adapters of type <code>adapter</code>. If no such factories
	 * exists, returns null.
	 */
	private IAdapterFactory getFactory(Class extensibleClass, Class adapter) {
		Map table;
		// check the cache first.
		if (lookup != null) {
			table = (Map) lookup.get(extensibleClass);
			if (table != null)
				return (IAdapterFactory) table.get(adapter);
		}
		// Its not in the cache so we have to build the adapter table for this
		// class.
		// The table is keyed by adapter class name. The
		// value is the <b>sole<b> factory that defines that adapter. Note that
		// if
		// if multiple adapters technically define the same property, only the
		// first found
		// in the search order is considered.
		table = new HashMap(4);
		// get the list of all superclasses and add the adapter factories
		// installed for each
		// of those classes to the table.
		List classList = computeClassOrder(extensibleClass);
		addFactoriesFor(classList, table);
		// get the ordered set of all interfaces for the extensible class and
		// its
		// superclasses and add the adapter factories installed for each
		// of those interfaces to the table.
		classList = computeInterfaceOrder(classList);
		addFactoriesFor(classList, table);
		//cache the table and do the lookup again.
		if (lookup == null)
			lookup = new HashMap(5);
		lookup.put(extensibleClass, table);
		return (IAdapterFactory) table.get(adapter);
	}
	public boolean hasAdapter(Object adaptable, String adapterTypeName) {
		//ask the extension adapter factory
		 return getExtensionFactory().hasAdapter(adaptable, adapterTypeName);
	}
	private ExtensionAdapterFactory getExtensionFactory() {
		if (extensionFactory == null)
			extensionFactory = new ExtensionAdapterFactory();
		return extensionFactory;
	}
	private void internalComputeInterfaceOrder(Class[] interfaces, List result, Set seen) {
		List newInterfaces = new ArrayList(interfaces.length);
		for (int i = 0; i < interfaces.length; i++) {
			Class interfac = interfaces[i];
			if (seen.add(interfac)) {
				result.add(interfac);
				newInterfaces.add(interfac);
			}
		}
		for (Iterator it = newInterfaces.iterator(); it.hasNext();)
			internalComputeInterfaceOrder(((Class) it.next()).getInterfaces(), result, seen);
	}
	/*
	 * @see IAdapterManager#registerAdapters
	 */
	public synchronized void registerAdapters(IAdapterFactory factory, Class adaptable) {
		List list = (List) factories.get(adaptable);
		if (list == null) {
			list = new ArrayList(5);
			factories.put(adaptable, list);
		}
		list.add(factory);
		extensionFactory.registerAdapters(factory, adaptable);
		flushLookup();
	}
	/*
	 * @see IAdapterManager#unregisterAdapters
	 */
	public synchronized void unregisterAdapters(IAdapterFactory factory) {
		for (Iterator it = factories.values().iterator(); it.hasNext();)
			((List) it.next()).remove(factory);
		flushLookup();
	}
	/*
	 * @see IAdapterManager#unregisterAdapters
	 */
	public synchronized void unregisterAdapters(IAdapterFactory factory, Class adaptable) {
		Vector factoryList = (Vector) factories.get(adaptable);
		if (factoryList == null)
			return;
		factoryList.removeElement(factory);
		flushLookup();
	}
	/*
	 * @see IAdapterManager#unregisterAllAdapters
	 */
	public synchronized void unregisterAllAdapters() {
		factories.clear();
		flushLookup();
	}
}
