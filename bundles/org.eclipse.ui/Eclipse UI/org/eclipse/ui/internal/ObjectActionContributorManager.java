package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

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
		Class [] interfaces = clazz.getInterfaces();
		for (int i=0; i<interfaces.length; i++) {
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
public boolean contributeObjectActions(IWorkbenchPart part, IMenuManager popupMenu, 
	ISelectionProvider selProv) 
{
	// Get a selection.	
	ISelection selection = selProv.getSelection();
	if (selection == null) 
		return false;
		
	// Convert the selection into an element vector.
	// According to the dictionary, a selection is "one that
	// is selected", or "a collection of selected things".  
	// In reflection of this, we deal with one or a collection.
	List elements = new ArrayList();
	if (selection instanceof IStructuredSelection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		Iterator enum = ssel.iterator();
		while (enum.hasNext()) {
			Object obj = enum.next();
			elements.add(obj);
		}
	} else {
		elements.add(selection);
	}
	
	// Calculate the common class.
	Class commonClass = getCommonClass(elements);
	if (commonClass == null)
		return false;
	
	// Get the resource class. It will be null if any of the
	// elements are resources themselves or do not adapt to
	// IResource.
	Class resourceClass = getCommonResourceClass(elements);

	// Get the contributors.	
	// If there is a resource class add it in
	List contributors = null;
	if (resourceClass == null)
		contributors = getContributors(commonClass);
	else
		contributors = getContributors(commonClass, resourceClass);
	if (contributors == null)
		return false;
		
	// Do the contributions.  Add menus first, then actions
	boolean actualContributions = false;
	for (int i = 0; i < contributors.size(); i++) {
		IObjectActionContributor contributor = (IObjectActionContributor) contributors.get(i);
		if (!isApplicableTo(elements, contributor)) 
			continue;
		if (contributor.contributeObjectMenus(popupMenu, selProv))
			actualContributions = true;
	}
	for (int i = 0; i < contributors.size(); i++) {
		IObjectActionContributor contributor = (IObjectActionContributor) contributors.get(i);
		if (!isApplicableTo(elements, contributor)) 
			continue;
		if (contributor.contributeObjectActions(part, popupMenu, selProv))
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
			Class candidate1 = (Class)list1.get(i);
			Class candidate2 = (Class)list2.get(j);
			if (candidate1.equals(candidate2)) return candidate1;
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
	if (objects == null || objects.size()==0)
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
		if (newClass.equals(commonClass)) continue;
		// compute common class
		commonClass = getCommonClass(commonClass, newClass);
		// give up
		if (commonClass==null) return null;
	}
	return commonClass;
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
	if (objects == null || objects.size()==0)
		return null;
		
	List testList = new ArrayList();
	
	for (int i = 0; i < objects.size(); i++) {
		Object object = objects.get(i);
		
		if(object instanceof IAdaptable){
			if(object instanceof IResource)
				continue;
			//Leave the resources out of this
			if(object instanceof IResource)
				break;
				
			IResource resource = getAdaptedResource((IAdaptable) object);
			
			if(resource == null)
				//Not a resource and does not adapt. No common resource class
				return null;
			testList.add(resource);
		}
		else
			return null;
	}
		
	return getCommonClass(testList);
}


}
