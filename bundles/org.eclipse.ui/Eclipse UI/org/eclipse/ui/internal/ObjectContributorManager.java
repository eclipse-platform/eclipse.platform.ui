package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.*;
import java.util.*;

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

	/** Table of contributors. */
	protected Map contributors;

	/** Cache of contributor search paths; <code>null</code> if none. */
	protected Map lookup;
/** Constructs a new contributor manager.
 */
public ObjectContributorManager() {
	contributors = new Hashtable(5);
	lookup = null;
}
/**
 * Adds contributors for the given types to the result list.
 */
private void addContributorsFor(List types, List result) {
	for (Iterator classes = types.iterator(); classes.hasNext();) {
		Class clazz = (Class) classes.next();
		List contributorList = (List) contributors.get(clazz.getName());
		if (contributorList == null)
			continue;
		for (Iterator list = contributorList.iterator(); list.hasNext();) {
			Object contributor = list.next();
			result.add(contributor);
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
private List computeInterfaceOrder(List classList) {
	List result = new ArrayList(4);
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
	lookup = null;
}
/**
 * Returns all the contributors registered against
 * the given object class.
 */
protected List getContributors(Class objectClass) {
	List result=null;
	
	// If there's a cache look for the object class.
	if (lookup!=null) {
		result = (ArrayList) lookup.get(objectClass);
		if (result != null)
		   return result;
	}
	
	// Class not found.  Build the result set for classes and interfaces.
	result = new ArrayList();
	List classList = computeClassOrder(objectClass);	// classes
	addContributorsFor(classList, result);
	classList = computeInterfaceOrder(classList);	// interfaces
	addContributorsFor(classList, result);
	if (result.size()==0) 
		return null;

	// Store the result set in the cache.
	if (lookup==null)
	   lookup = new HashMap();
	lookup.put(objectClass, result);
	
	return result;
}
/**
 * Returns true if contributors exist in the manager for
 * this object.
 */
public boolean hasContributorsFor(Object object) {
	List contributors = getContributors(object.getClass());
	return (contributors!=null && contributors.size()>0);
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
	for (Iterator elements=selection.iterator(); elements.hasNext();) {
		if (contributor.isApplicableTo(elements.next())==false) return false;
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
}
