/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IContributorResourceAdapter;

/**
 * This class is a default implementation of <code>IObjectContributorManager</code>.
 * It provides fast merging of contributions with the following semantics:
 * <ul>
 * <li> All of the matching contributors will be invoked per property lookup
 * <li> The search order from a class with the definition<br>
 *			<code>class X extends Y implements A, B</code><br>
 *		is as follows:
 * 		<il>
 *			<li>the target's class: X
 *			<li>X's superclasses in order to <code>Object</code>
 *			<li>a depth-first traversal of the target class's interaces in the order 
 *				returned by <code>getInterfaces()</code> (in the example, A and 
 *				its superinterfaces then B and its superinterfaces)
 *		</il>
 * </ul>
 *
 * @see IObjectContributor
 * @see IObjectContributorManager
 */
public abstract class ObjectContributorManager {
	// Empty list that is immutable
	private static final List EMPTY_LIST = Arrays.asList(new Object[0]);

	/** Table of contributors. */
	protected Map contributors;

	/** Cache of object class contributor search paths; <code>null</code> if none. */
	protected Map objectLookup;

	/** Cache of resource adapter class contributor search paths; <code>null</code> if none. */
	protected Map adapterLookup;

	/** 
	 * Constructs a new contributor manager.
	 */
	public ObjectContributorManager() {
		contributors = new Hashtable(5);
		objectLookup = null;
		adapterLookup = null;
	}
	/**
	 * Adds contributors for the given types to the result list.
	 */
	private void addContributorsFor(List types, List result) {
		for (Iterator classes = types.iterator(); classes.hasNext();) {
			Class clazz = (Class) classes.next();
			List contributorList = (List) contributors.get(clazz.getName());
			if (contributorList != null)
				result.addAll(contributorList);
		}
	}
	/**
	 * Returns the class search order starting with <code>extensibleClass</code>.
	 * The search order is defined in this class' comment.
	 */
	protected final List computeClassOrder(Class extensibleClass) {
		ArrayList result = new ArrayList(4);
		Class clazz = extensibleClass;
		while (clazz != null) {
			result.add(clazz);
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	/**
	 * Returns the interface search order for the class hierarchy described
	 * by <code>classList</code>.
	 * The search order is defined in this class' comment.
	 */
	protected final List computeInterfaceOrder(List classList) {
		ArrayList result = new ArrayList(4);
		Map seen = new HashMap(4);
		for (Iterator list = classList.iterator(); list.hasNext();) {
			Class[] interfaces = ((Class) list.next()).getInterfaces();
			internalComputeInterfaceOrder(interfaces, result, seen);
		}
		return result;
	}

	/**
	 * Flushes the cache of contributor search paths.  This is generally required
	 * whenever a contributor is added or removed.  
	 * <p>
	 * It is likely easier to just toss the whole cache rather than trying to be
	 * smart and remove only those entries affected.
	 */
	public void flushLookup() {
		objectLookup = null;
		adapterLookup = null;
	}
	/**
	 * Cache the resource adapter class contributor search path.
	 */
	private void cacheAdapterLookup(Class adapterClass, List results) {
		if (adapterLookup == null)
			adapterLookup = new HashMap();
		adapterLookup.put(adapterClass, results);
	}
	/**
	 * Cache the object class contributor search path.
	 */
	private void cacheObjectLookup(Class objectClass, List results) {
		if (objectLookup == null)
			objectLookup = new HashMap();
		objectLookup.put(objectClass, results);
	}
	/**
	 * Returns all the contributors registered against
	 * the given object class.
	 */
	protected List getContributors(Class objectClass) {

		List objectList = null;

		// Lookup the results in the cache first
		if (objectLookup != null) {
			objectList = (List) objectLookup.get(objectClass);
		}

		// If not in cache, build it
		if (objectList == null) {
			objectList = addContributorsFor(objectClass);
			if (objectList.size() == 0)
				objectList = EMPTY_LIST;

			// Store the contribution list into the cache.
			cacheObjectLookup(objectClass, objectList);
		}

		return objectList;
	}

	/**
	 * Return the list of contributors for the supplied class.
	 */
	protected List addContributorsFor(Class objectClass) {

		List classList = computeClassOrder(objectClass);
		List result = new ArrayList();
		addContributorsFor(classList, result);
		classList = computeInterfaceOrder(classList); // interfaces
		addContributorsFor(classList, result);
		return result;
	}

	/**
	 * Get the contributors for object including those it adapts
	 * to.
	 * 
	 * @return The list of contributors, empty if none.
	 */
	protected List getContributors(Object object) {

		Class objectClass = object.getClass();
		IResource adapted = getAdaptedResource(object);

		if (adapted == null)
			return getContributors(objectClass);
		else
			return getContributors(objectClass, adapted.getClass());
	}

	/**
	 * Returns true if contributors exist in the manager for
	 * this object.
	 */
	public boolean hasContributorsFor(Object object) {

		List contributors = getContributors(object);
		return contributors.size() > 0;
	}
	/**
	 * Add interface Class objects to the result list based
	 * on the class hierarchy. Interfaces will be searched
	 * based on their position in the result list.
	 */
	private void internalComputeInterfaceOrder(Class[] interfaces, List result, Map seen) {
		List newInterfaces = new ArrayList(seen.size());
		for (int i = 0; i < interfaces.length; i++) {
			Class interfac = interfaces[i];
			if (seen.get(interfac) == null) {
				result.add(interfac);
				seen.put(interfac, interfac);
				newInterfaces.add(interfac);
			}
		}
		for (Iterator newList = newInterfaces.iterator(); newList.hasNext();)
			internalComputeInterfaceOrder(((Class) newList.next()).getInterfaces(), result, seen);
	}
	/**
	 *
	 */
	public boolean isApplicableTo(IStructuredSelection selection, IObjectContributor contributor) {
		Iterator elements = selection.iterator();
		while (elements.hasNext()) {
			if (contributor.isApplicableTo(elements.next()) == false)
				return false;
		}
		return true;
	}
	/**
	 *
	 */
	public boolean isApplicableTo(List list, IObjectContributor contributor) {
		Iterator elements = list.iterator();
		while (elements.hasNext()) {
			if (contributor.isApplicableTo(elements.next()) == false)
				return false;
		}
		return true;
	}
	/**
	 * @see IContributorManager#registerContributor
	 */
	public void registerContributor(IObjectContributor contributor, String targetType) {
		Vector contributorList = (Vector) contributors.get(targetType);
		if (contributorList == null) {
			contributorList = new Vector(5);
			contributors.put(targetType, contributorList);
		}
		contributorList.addElement(contributor);
		flushLookup();
	}
	/**
	 * @see IContributorManager#unregisterAllContributors
	 */
	public void unregisterAllContributors() {
		contributors = new Hashtable(5);
		flushLookup();
	}
	/**
	 * @see IContributorManager#unregisterContributor
	 */
	public void unregisterContributor(IObjectContributor contributor, String targetType) {
		Vector contributorList = (Vector) contributors.get(targetType);
		if (contributorList == null)
			return;
		contributorList.removeElement(contributor);
		flushLookup();
	}
	/**
	 * @see IContributorManager#unregisterContributors
	 */
	public void unregisterContributors(String targetType) {
		contributors.remove(targetType);
		flushLookup();
	}

	/**
	 * Returns all the contributors registered against
	 * the given object class and the resource class that
	 * it has an Adaptable for.
	 */
	protected List getContributors(Class objectClass, Class resourceClass) {

		List objectList = null;
		List resourceList = null;

		// Lookup the results in the cache first
		if (objectLookup != null) {
			objectList = (List) objectLookup.get(objectClass);
		}
		if (adapterLookup != null) {
			resourceList = (List) adapterLookup.get(resourceClass);
		}

		if (objectList == null) {
			objectList = addContributorsFor(objectClass);
			if (objectList.size() == 0)
				objectList = EMPTY_LIST;
			cacheObjectLookup(objectClass, objectList);
		}
		if (resourceList == null) {
			List contributors = addContributorsFor(resourceClass);
			resourceList = new ArrayList(contributors.size());
			Iterator enum = contributors.iterator();
			while (enum.hasNext()) {
				IObjectContributor contributor = (IObjectContributor) enum.next();
				if (contributor.canAdapt())
					resourceList.add(contributor);
			}
			if (resourceList.size() == 0)
				resourceList = EMPTY_LIST;
			cacheAdapterLookup(resourceClass, resourceList);
		}

		// Collect the contribution lists into one result
		ArrayList results = new ArrayList(objectList.size() + resourceList.size());
		results.addAll(objectList);
		results.addAll(resourceList);
		return results;
	}

	/**
	 * Get the adapted resource for the supplied object. If the
	 * object is an instance of IResource or is not an instance
	 * of IAdaptable return null. Otherwise see if it adapts
	 * to IResource via IContributorResourceAdapter.
	 * @return IResource or null
	 * @param object Object 
	 */
	protected IResource getAdaptedResource(Object object) {

		if (object instanceof IResource)
			return null;

		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;

			Object resourceAdapter = adaptable.getAdapter(IContributorResourceAdapter.class);
			if (resourceAdapter == null)
				resourceAdapter = DefaultContributorResourceAdapter.getDefault();

			return ((IContributorResourceAdapter) resourceAdapter).getAdaptedResource(adaptable);
		}
		return null;
	}
}
