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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This manager is used to populate a popup menu manager with actions
 * for a given type.
 */
public class ObjectActionContributorManager extends ObjectContributorManager {
	private static ObjectActionContributorManager sharedInstance;
	
	/**
	 * PopupMenuManager constructor.
	 */
	public ObjectActionContributorManager() {
		loadContributors();
	}
	
	/**
	 * Returns the class search order starting with <code>extensibleClass</code>.
	 * The search order is defined in this class' comment.
	 */
	private List computeCombinedOrder(Class inputClass) {
		List result = new ArrayList(4);
		Class clazz = inputClass;
		while (clazz != null) {
			// add the class
			result.add(clazz);
			// add all the interfaces it implements
			Class[] interfaces = clazz.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				result.add(interfaces[i]);
			}
			// get the superclass
			clazz = clazz.getSuperclass();
		}
		return result;
	}
	
	/**
	 * Contributes submenus and/or actions applicable to the selection in the
	 * provided viewer into the provided popup menu.
	 */
	public boolean contributeObjectActions(IWorkbenchPart part, IMenuManager popupMenu, ISelectionProvider selProv) {
		// Get a selection.	
		ISelection selection = selProv.getSelection();
		if (selection == null)
			return false;

		// Convert the selection into an element vector.
		// According to the dictionary, a selection is "one that
		// is selected", or "a collection of selected things".  
		// In reflection of this, we deal with one or a collection.
		List elements = null;
		if (selection instanceof IStructuredSelection) {
			elements = ((IStructuredSelection) selection).toList();
		} else {
			elements = new ArrayList(1);
			elements.add(selection);
		}

		// Calculate the common class and interfaces.
		List commonClasses = getCommonClasses(elements);
		if (commonClasses == null || commonClasses.isEmpty())
			return false;

		// Get the resource class. It will be null if any of the
		// elements are resources themselves or do not adapt to
		// IResource.
		Class resourceClass = getCommonResourceClass(elements);

		// Get the contributors.	
		// If there is a resource class add it in
		List contributors = null;
		if (resourceClass == null) {
			if (commonClasses.size() == 1) {
				contributors = getContributors((Class)commonClasses.get(0));
			} else {
				contributors = new ArrayList();
				for (int i = 0; i < commonClasses.size(); i++) {
					List results = getContributors((Class)commonClasses.get(i));
					if (results != null)
						contributors.addAll(results);
				}
			}
		} else {
			contributors = getContributors((Class)commonClasses.get(0), resourceClass);
			for (int i = 1; i < commonClasses.size(); i++) {
				List results = getContributors((Class)commonClasses.get(i));
				if (results != null)
					contributors.addAll(results);
			}
		}
		
		if (contributors == null || contributors.isEmpty())
			return false;

		// Do the contributions.  Add menus first, then actions
		boolean actualContributions = false;
		ArrayList overrides = new ArrayList(4);
		for (int i = 0; i < contributors.size(); i++) {
			IObjectActionContributor contributor = (IObjectActionContributor) contributors.get(i);
			if (!isApplicableTo(elements, contributor))
				continue;
			if (contributor.contributeObjectMenus(popupMenu, selProv))
				actualContributions = true;
			contributor.contributeObjectActionIdOverrides(overrides);
		}
		for (int i = 0; i < contributors.size(); i++) {
			IObjectActionContributor contributor = (IObjectActionContributor) contributors.get(i);
			if (!isApplicableTo(elements, contributor))
				continue;
			if (contributor.contributeObjectActions(part, popupMenu, selProv, overrides))
				actualContributions = true;
		}
		return actualContributions;
	}

	/**
	 * Returns the common denominator class for
	 * two input classes.
	 */
	private Class getCommonClass(Class class1, Class class2) {
		List list1 = computeCombinedOrder(class1);
		List list2 = computeCombinedOrder(class2);
		for (int i = 0; i < list1.size(); i++) {
			for (int j = 0; j < list2.size(); j++) {
				Class candidate1 = (Class) list1.get(i);
				Class candidate2 = (Class) list2.get(j);
				if (candidate1.equals(candidate2))
					return candidate1;
			}
		}
		// no common class
		return null;
	}
	
	/**
	 * Returns the common denominator class for the given
	 * collection of objects.
	 */
	private Class getCommonClass(List objects) {
		if (objects == null || objects.size() == 0)
			return null;
		Class commonClass = objects.get(0).getClass();
		// try easy
		if (objects.size() == 1)
			return commonClass;
		// try harder

		for (int i = 1; i < objects.size(); i++) {
			Object object = objects.get(i);
			Class newClass = object.getClass();
			// try the short cut
			if (newClass.equals(commonClass))
				continue;
			// compute common class
			commonClass = getCommonClass(commonClass, newClass);
			// give up
			if (commonClass == null)
				return null;
		}
		return commonClass;
	}

	/**
	 * Returns the common denominator class and interfaces for the given
	 * collection of objects.
	 */
	private List getCommonClasses(List objects) {
		if (objects == null || objects.size() == 0)
			return null;

		// Quickly handle the easy case...
		if (objects.size() == 1) {
			List results = new ArrayList(1);
			results.add(objects.get(0).getClass());
			return results;
		}

		// Compute all the super classes for the first element
		// and then all of the interfaces for the first element
		// and it's super classes.
		List classes = computeClassOrder(objects.get(0).getClass());
		List interfaces = computeInterfaceOrder(classes);
		boolean classesEmpty = classes.isEmpty();
		boolean interfacesEmpty = interfaces.isEmpty();
		
		for (int i = 1; i < objects.size(); i++) {
			// Compute all the super classes for the current element
			List results = computeClassOrder(objects.get(i).getClass());
			if (!classesEmpty) {
				classesEmpty = true;
				if (results.isEmpty()) {
					// When no super classes, then it is obvious there
					// are no common super classes with the first element
					// so clear its list.
					classes.clear();
				} else {
					// Remove any super classes of the first element that 
					// are not in the current element's super classes list.
					for (int j = 0; j < classes.size(); j++) {
						if (classes.get(j) != null) {
							classesEmpty = false;
							if (!results.contains(classes.get(j))) {
								classes.set(j, null);							
							}
						}
					}
				}
			}
			
			if (!interfacesEmpty) {
				// Compute all the interfaces for the current element
				// and all of its super classes.
				results = computeInterfaceOrder(results);
				interfacesEmpty = true;
				if (results.isEmpty()) {
					// When no interfaces, the it is obvious there are
					// no common interfaces between this current element
					// and the first element, so clear its list.
					interfaces.clear();
				} else {
					// Remove any interfaces of the first element that
					// are not in the current element's interfaces list.
					for (int j = 0; j < interfaces.size(); j++) {
						if (interfaces.get(j) != null) {
							interfacesEmpty = false;
							if (!results.contains(interfaces.get(j))) {
								interfaces.set(j, null);
							}
						}
					}
				}
			}

			if (interfacesEmpty && classesEmpty) {
				// As soon as we detect nothing in common, just exit.
				return null;
			}
		}
		
		ArrayList results = new ArrayList(4);
		ArrayList superClasses = new ArrayList(4);
		if (!classesEmpty) {
			for (int j = 0; j < classes.size(); j++) {
				if (classes.get(j) != null) {
					superClasses.add(classes.get(j));
				}
			}
			// Just keep the first super class
			if (!superClasses.isEmpty()) {
				results.add(superClasses.get(0));
			}
		}

		if (!interfacesEmpty) {
			// Do no include the interfaces belonging to the common
			// super classes as these will be calculated again later
			// in addContributors method.
			List dropInterfaces = null;
			if (!superClasses.isEmpty()) {
				dropInterfaces = computeInterfaceOrder(superClasses);
			}
			
			for (int j = 0; j < interfaces.size(); j++) {
				if (interfaces.get(j) != null) {
					if (dropInterfaces != null && !dropInterfaces.contains(interfaces.get(j))) {
						results.add(interfaces.get(j));
					}
				}
			}
		}
		
		return results;
	}

	/**
	 * Returns the shared instance of this manager.
	 */
	public static ObjectActionContributorManager getManager() {
		if (sharedInstance == null) {
			sharedInstance = new ObjectActionContributorManager();
		}
		return sharedInstance;
	}
	
	/**
	 * Loads the contributors from the workbench's registry.
	 */
	private void loadContributors() {
		ObjectActionContributorReader reader = new ObjectActionContributorReader();
		reader.readPopupContributors(this);
	}
	
	/**
	 * Returns the common denominator resource class for the given
	 * collection of objects.
	 * Do not return a resource class if the objects are resources
	 * themselves so as to prevent double registration of actions.
	 */
	private Class getCommonResourceClass(List objects) {
		if (objects == null || objects.size() == 0)
			return null;

		List testList = new ArrayList();

		for (int i = 0; i < objects.size(); i++) {
			Object object = objects.get(i);

			if (object instanceof IAdaptable) {
				if (object instanceof IResource)
					continue;

				IResource resource = getAdaptedResource((IAdaptable) object);

				if (resource == null)
					//Not a resource and does not adapt. No common resource class
					return null;
				testList.add(resource);
			} else
				return null;
		}

		return getCommonClass(testList);
	}
}
