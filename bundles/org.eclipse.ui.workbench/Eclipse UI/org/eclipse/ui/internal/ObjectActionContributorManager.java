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

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.*;
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
     * Contributes submenus and/or actions applicable to the selection in the
     * provided viewer into the provided popup menu.
     */
    public boolean contributeObjectActions(IWorkbenchPart part,
            IMenuManager popupMenu, ISelectionProvider selProv) {
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

        // Calculate the common class, interfaces, and adapters registered
        // via the IAdapterManager.
        List commonAdapters = new ArrayList();
        List commonClasses = getCommonClasses(elements, commonAdapters);
        // Get the resource class. It will be null if any of the
        // elements are resources themselves or do not adapt to
        // IResource.
        Class resourceClass = getCommonResourceClass(elements);

        // Get the contributors.	
        List contributors = new ArrayList();
		if (resourceClass != null) {
			contributors.addAll(getResourceContributors(resourceClass));
		}
		if (! commonAdapters.isEmpty()) {
			for (Iterator it = commonAdapters.iterator(); it.hasNext();) {
				String adapter = (String) it.next();
				contributors.addAll(getAdaptableContributors(adapter));
			}
		}
		if (commonClasses != null && ! commonClasses.isEmpty()) {
			for (int i = 0; i < commonClasses.size(); i++) {
				List results = getObjectContributors((Class) commonClasses.get(i));
				if (results != null)
					contributors.addAll(results);
			}
		}
       
        if (contributors.isEmpty())
            return false;

        // First pass, add the menus and collect the overrides. Prune from the
        // list any non-applicable contributions.
        boolean actualContributions = false;
        ArrayList overrides = new ArrayList(4);
        for (Iterator it = contributors.iterator(); it.hasNext();) {
			IObjectActionContributor contributor = (IObjectActionContributor) it.next();
            if (!isApplicableTo(elements, contributor)) {
            	it.remove();            
                continue;
            }
            if (contributor.contributeObjectMenus(popupMenu, selProv))
                actualContributions = true;
            contributor.contributeObjectActionIdOverrides(overrides);
        }
        
        // Second pass, add the contributions that are applicable to
        // the selection.
        for (Iterator it = contributors.iterator(); it.hasNext();) {
			IObjectActionContributor contributor = (IObjectActionContributor) it.next();        
            if (contributor.contributeObjectActions(part, popupMenu, selProv,
                    overrides))
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
     * Returns the common denominator class, interfaces, and adapters 
     * for the given collection of objects.
     */
    private List getCommonClasses(List objects, List commonAdapters) {
        if (objects == null || objects.size() == 0)
            return null;

        // Compute all the super classes, interfaces, and adapters 
        // for the first element.
        List classes = computeClassOrder(objects.get(0).getClass());
        List adapters = computeAdapterOrder(classes);
        List interfaces = computeInterfaceOrder(classes);
        
        // Cache of all types found in the selection - this is needed
        // to compute common adapters.
        List lastCommonTypes = new ArrayList();
        
        boolean classesEmpty = classes.isEmpty();
        boolean interfacesEmpty = interfaces.isEmpty();

        // Traverse the selection if there is more than one element selected.
        for (int i = 1; i < objects.size(); i++) {
            // Compute all the super classes for the current element
            List otherClasses = computeClassOrder(objects.get(i).getClass());
            if (!classesEmpty) {
                classesEmpty = extractCommonClasses(classes, otherClasses);
            }
            
            // Compute all the interfaces for the current element
            // and all of its super classes.
            List otherInterfaces = computeInterfaceOrder(otherClasses);
            if (!interfacesEmpty) {
                interfacesEmpty = extractCommonClasses(interfaces, otherInterfaces);
            }
            
            // Compute all the adapters provided for the calculated
            // classes and interfaces for this element.
            List classesAndInterfaces = new ArrayList(otherClasses);
        	if(otherInterfaces != null) 
        		classesAndInterfaces.addAll(otherInterfaces);
			List otherAdapters = computeAdapterOrder(classesAndInterfaces);
            
            // Compute common adapters
            // Note here that an adapter can match a class or interface, that is
            // that an element in the selection may not adapt to a type but instead
            // be of that type.
			// If the selected classes doesn't have adapters, keep
			// adapters that match the given classes types (classes and interfaces).
			if (otherAdapters.isEmpty() && ! adapters.isEmpty()) {
				removeNonCommonAdapters(adapters, classesAndInterfaces);
			} else {
				if (adapters.isEmpty()) {
					removeNonCommonAdapters(otherAdapters, lastCommonTypes);
					if(! otherAdapters.isEmpty())
						adapters.addAll(otherAdapters);
				} else {
					// Remove any adapters of the first element that
					// are not in the current element's adapter list.
					for (Iterator it = adapters.iterator(); it.hasNext();) {
						String adapter = (String) it.next();
						if (! otherAdapters.contains(adapter)) {
							it.remove();
						}
					}
				}
			}

            // Remember the common search order up to now, this is
			// used to match adapters against common classes or interfaces.
			lastCommonTypes.clear();
            lastCommonTypes.addAll(classes);
            lastCommonTypes.addAll(interfaces);
            
            if (interfacesEmpty && classesEmpty && adapters.isEmpty()) {
                // As soon as we detect nothing in common, just exit.
                return null;
            }
        }

        // Once the common classes, interfaces, and adapters are
        // calculated, let's prune the lists to remove duplicates.       
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
           removeCommonInterfaces(superClasses, interfaces, results);
        }
        
        // Remove adapters already included as common classes
        if(! adapters.isEmpty()) {
        	removeCommonAdapters(adapters, results);
        	commonAdapters.addAll(adapters);
        }	
        return results;        
    }
    
    private boolean extractCommonClasses(List classes, List otherClasses) {
		boolean classesEmpty = true;
		if (otherClasses.isEmpty()) {
		    // When no super classes, then it is obvious there
		    // are no common super classes with the first element
		    // so clear its list.
		    classes.clear();
		} else {
		    // Remove any super classes of the first element that 
		    // are not in the current element's super classes list.
		    for (int j = 0; j < classes.size(); j++) {
		        if (classes.get(j) != null) {
		            classesEmpty = false;  		            // TODO: should this only be set if item not nulled out?
		            if (!otherClasses.contains(classes.get(j))) {
		                classes.set(j, null);
		            }
		        }
		    }
		}
		return classesEmpty;
	}

	private void removeNonCommonAdapters(List adapters, List classes) {
    	for (int i = 0; i < classes.size(); i++) {
			Object o = classes.get(i);
			if(o != null) {
				Class clazz = (Class)o;
				String name = clazz.getName();
				if(adapters.contains(name))
					return;
			}			
		}
    	adapters.clear();
    }
    
    private void removeCommonInterfaces(List superClasses, List types, List results) {
		List dropInterfaces = null;
		if (!superClasses.isEmpty()) {
			dropInterfaces = computeInterfaceOrder(superClasses);
		}
		for (int j = 0; j < types.size(); j++) {
			if (types.get(j) != null) {
				if (dropInterfaces != null && !dropInterfaces.contains(types.get(j))) {
					results.add(types.get(j));
				}
			}
		}
	}
    
    private List computeAdapterOrder(List classList) {
    	Set result = new HashSet(4);       
        IAdapterManager adapterMgr = Platform.getAdapterManager();
        for (Iterator list = classList.iterator(); list.hasNext();) {
            Class clazz = ((Class) list.next());
            String[] adapters = adapterMgr.computeAdapterTypes(clazz);
            for (int i = 0; i < adapters.length; i++) {
				String adapter = adapters[i];
				if(! result.contains(adapter)) {
					result.add(adapter);
				}
			}
        }
        return new ArrayList(result);
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
        if (objects == null || objects.size() == 0) {
            return null;
        }
        Class resourceClass = LegacyResourceSupport.getResourceClass();
        if (resourceClass == null) {
            // resources plug-in not loaded - no resources. period.
            return null;
        }

        List testList = new ArrayList();

        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);

            if (object instanceof IAdaptable) {
                if (resourceClass.isInstance(object)) {
                    continue;
                }

                Object resource = LegacyResourceSupport.getAdaptedContributorResource(object);

                if (resource == null) {
                    //Not a resource and does not adapt. No common resource class
                    return null;
                }
                testList.add(resource);
            } else {
                return null;
            }
        }

        return getCommonClass(testList);
    }
}