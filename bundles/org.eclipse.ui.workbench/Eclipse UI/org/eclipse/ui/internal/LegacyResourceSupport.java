/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * Provides access to resource-specific classes, needed to provide
 * backwards compatibility for resource-specific functions which
 * could not be moved up from the generic workbench layer to the
 * IDE layer.
 */
public final class LegacyResourceSupport {

	private static String[] resourceClassNames = {
        "org.eclipse.core.resources.IResource", //$NON-NLS-1$
        "org.eclipse.core.resources.IContainer", //$NON-NLS-1$
        "org.eclipse.core.resources.IFolder", //$NON-NLS-1$
        "org.eclipse.core.resources.IProject", //$NON-NLS-1$
        "org.eclipse.core.resources.IFile", //$NON-NLS-1$
	};
	
    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.core.resources.IResource")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.0
     */
    private static Class iresourceClass = null;

    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.core.resources.IFile")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.1
     */
    private static Class ifileClass;
    
    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.ui.IContributorResourceAdapter")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.0
     */
    private static Class icontributorResourceAdapterClass = null;

    /**
     * Cached value of </code> org.eclipse.ui.IContributorResourceAdapter.getAdaptedResource(IAdaptable) </code>
     * <code>null</code> if not initialized or not present.
     * 
     * @since 3.3
     */
    private static Method getAdaptedResourceMethod = null;
    
    /**
     * Cached value of </code> org.eclipse.ui.IContributorResourceAdapter2.getAdaptedResourceMapping(IAdaptable) </code>
     * <code>null</code> if not initialized or not present.
     * 
     * @since 3.3
     */
    private static Method getAdaptedResourceMappingMethod = null;
    
    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.ui.ide.IContributorResourceAdapter2")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.1
     */
    private static Class icontributorResourceAdapter2Class = null;
    
    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.ui.internal.ide.DefaultContributorResourceAdapter")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.0
     */
    private static Class defaultContributorResourceAdapterClass = null;

    /**
     * Cached value for reflective result of <code>DefaultContributorRessourceAdapter.getDefault()</code>.
     * <code>null</code> if not initialized or not present.
     * 
     * @since 3.3
     */
    private static Object defaultContributorResourceAdapter = null;
    
    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.core.resources.mapping.ResourceMappingr")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.0
     */
    private static Class resourceMappingClass = null;

    /**
     * Indicates whether the IDE plug-in (which supplies the
     * resource contribution adapters) is even around.
     */
    private static boolean resourceAdapterPossible = true;


    /**
     * Returns <code>IFile.class</code> or <code>null</code> if the
     * class is not available.
     * <p>
     * This method exists to avoid explicit references from the generic
     * workbench to the resources plug-in.
     * </p>
     * 
     * @return <code>IFile.class</code> or <code>null</code> if class
     * not available
     * @since 3.1
     */
    public static Class getFileClass() {
        if (ifileClass != null) {
            // tried before and succeeded
            return ifileClass;
        }
        Class c = loadClass("org.eclipse.core.resources", "org.eclipse.core.resources.IFile"); //$NON-NLS-1$ //$NON-NLS-2$
        if (c != null) {
            // The class was found so record it
            ifileClass = c;
        }
        return c;
    }

    /**
     * Returns <code>IResource.class</code> or <code>null</code> if the
     * class is not available.
     * <p>
     * This method exists to avoid explicit references from the generic
     * workbench to the resources plug-in.
     * </p>
     * 
     * @return <code>IResource.class</code> or <code>null</code> if class
     * not available
     * @since 3.0
     */
    public static Class getResourceClass() {
        if (iresourceClass != null) {
            // tried before and succeeded
            return iresourceClass;
        }
        Class c = loadClass("org.eclipse.core.resources", "org.eclipse.core.resources.IResource"); //$NON-NLS-1$ //$NON-NLS-2$
        if (c != null) {
            // The class was found so record it
            iresourceClass = c;
        }
        return c;
    }

    /**
     * Returns <code>ResourceMapping.class</code> or <code>null</code> if the
     * class is not available.
     * <p>
     * This method exists to avoid explicit references from the generic
     * workbench to the resources plug-in.
     * </p>
     * 
     * @return <code>ResourceMapping.class</code> or <code>null</code> if class
     * not available
     * @since 3.1
     */
    public static Class getResourceMappingClass() {
        if (resourceMappingClass != null) {
            // tried before and succeeded
            return resourceMappingClass;
        }
        Class c = loadClass("org.eclipse.core.resources", "org.eclipse.core.resources.mapping.ResourceMapping"); //$NON-NLS-1$ //$NON-NLS-2$
        if (c != null) {
            // The class was found so record it
            resourceMappingClass = c;
        }
        return c;
    }
    
    /**
     * Returns <code>IContributorResourceAdapter.class</code> or
     * <code>null</code> if the class is not available.
     * <p>
     * This method exists to avoid explicit references from the generic
     * workbench to the IDE plug-in.
     * </p>
     * 
     * @return <code>IContributorResourceAdapter.class</code> or
     * <code>null</code> if class not available
     * @since 3.0
     */
    public static Class getIContributorResourceAdapterClass() {
        if (icontributorResourceAdapterClass != null) {
            // tried before and succeeded
            return icontributorResourceAdapterClass;
        }
        Class c = loadClass("org.eclipse.ui.ide", "org.eclipse.ui.IContributorResourceAdapter"); //$NON-NLS-1$ //$NON-NLS-2$
        if (c != null) {
            // The class was found so record it
            icontributorResourceAdapterClass = c;
        }
        return c;
    }

    /**
     * Returns <code>IContributorResourceAdapter2.class</code> or
     * <code>null</code> if the class is not available.
     * <p>
     * This method exists to avoid explicit references from the generic
     * workbench to the IDE plug-in.
     * </p>
     * 
     * @return <code>IContributorResourceAdapter.class</code> or
     * <code>null</code> if class not available
     * @since 3.1
     */
    public static Class getIContributorResourceAdapter2Class() {
        if (icontributorResourceAdapter2Class != null) {
            // tried before and succeeded
            return icontributorResourceAdapter2Class;
        }
        Class c = loadClass("org.eclipse.ui.ide", "org.eclipse.ui.ide.IContributorResourceAdapter2"); //$NON-NLS-1$ //$NON-NLS-2$
        if (c != null) {
            // The class was found so record it
            icontributorResourceAdapter2Class = c;
        }
        return c;
    }
    
    private static Class loadClass(String bundleName, String className) {
        if (!resourceAdapterPossible) {
            // tried before and failed
            return null;
        }
        Bundle bundle = Platform.getBundle(bundleName);
        if (bundle == null) {
            // Required plug-in is not around
            // assume that it will never be around
            resourceAdapterPossible = false;
            return null;
        }
        // Required plug-in is around
        // it's not our job to activate the plug-in
        if (!BundleUtility.isActivated(bundle)) {
            // assume it might come alive later
            resourceAdapterPossible = true;
            return null;
        }
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            // unable to load the class - sounds pretty serious
            // treat as if the plug-in were unavailable
            resourceAdapterPossible = false;
            return null;
        }
    }
    
    /**
     * Returns <code>DefaultContributorResourceAdapter.class</code> or
     * <code>null</code> if the class is not available.
     * <p>
     * This method exists to avoid explicit references from the generic
     * workbench to the IDE plug-in.
     * </p>
     * 
     * @return <code>DefaultContributorResourceAdapter.class</code> or
     * <code>null</code> if class not available
     * @since 3.0
     */
    public static Class getDefaultContributorResourceAdapterClass() {
        if (defaultContributorResourceAdapterClass != null) {
            // tried before and succeeded
            return defaultContributorResourceAdapterClass;
        }
        Class c = loadClass("org.eclipse.ui.ide", "org.eclipse.ui.internal.ide.DefaultContributorResourceAdapter"); //$NON-NLS-1$ //$NON-NLS-2$
        if (c != null) {
            // The class was found so record it
            defaultContributorResourceAdapterClass = c;
        }
        return c;
    }
    
    private static Object getDefaultContributorResourceAdapter() {
        if (defaultContributorResourceAdapter != null) {
            return defaultContributorResourceAdapter;
        }
        
		// reflective equivalent of
		//    resourceAdapter = DefaultContributorResourceAdapter.getDefault();
		
			Class c = LegacyResourceSupport.getDefaultContributorResourceAdapterClass();
			if (c != null) {    			
				try {
					Method m  = c.getDeclaredMethod("getDefault", new Class[0]);//$NON-NLS-1$
					defaultContributorResourceAdapter = m.invoke(null, new Object[0]);
					return defaultContributorResourceAdapter;
				} catch (SecurityException e) {
					// shouldn't happen - but play it safe
				} catch (NoSuchMethodException e) {
					// shouldn't happen - but play it safe
				} catch (IllegalArgumentException e) {
					// shouldn't happen - but play it safe
				} catch (IllegalAccessException e) {
					// shouldn't happen - but play it safe
				} catch (InvocationTargetException e) {
					// shouldn't happen - but play it safe
				} 
    			
    			
			}
		
		return null;

    }
    
    /**
     * Returns <code>true</code> if the provided type name is an
     * <code>IResource</code>, and <code>false</code> otherwise.
     * @param objectClassName
     * @return <code>true</code> if the provided type name is an
     * <code>IResource</code>, and <code>false</code> otherwise.
     * 
     * @since 3.1
     */
    public static boolean isResourceType(String objectClassName) {
        for (int i = 0; i < resourceClassNames.length; i++) {
            if (resourceClassNames[i].equals(objectClassName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns <code>true</code> if the provided type name is an
     * <code>"org.eclipse.core.resources.mapping.ResourceMapping"</code>, and <code>false</code> otherwise.
     * @param objectClassName
     * @return <code>true</code> if the provided type name is an
     * <code>"org.eclipse.core.resources.mapping.ResourceMapping"</code>, and <code>false</code> otherwise.
     * 
     * @since 3.1
     */
    public static boolean isResourceMappingType(String objectClassName) {
        return objectClassName.equals("org.eclipse.core.resources.mapping.ResourceMapping"); //$NON-NLS-1$
    }
    
    /**
     * Returns the class search order starting with <code>extensibleClass</code>.
     * The search order is defined in this class' comment.
     * 
     * @since 3.1
     */
    private static boolean isInstanceOf(Class clazz, String type) {
		if (clazz.getName().equals(type)) {
			return true;
		}
		Class superClass= clazz.getSuperclass();
		if (superClass != null && isInstanceOf(superClass, type)) {
			return true;
		}
		Class[] interfaces= clazz.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			if (isInstanceOf(interfaces[i], type)) {
				return true;
			}
		} 
		return false;
	}
    
    /**
     * Returns the adapted resource using the <code>IContributorResourceAdapter</code>
     * registered for the given object. If the Resources plug-in is not loaded
     * the object can not be adapted.
     * 
     * @param object the object to adapt to <code>IResource</code>.
     * @return returns the adapted resource using the <code>IContributorResourceAdapter</code>
     * or <code>null</code> if the Resources plug-in is not loaded.
     * 
     * @since 3.1
     */
    public static Object getAdaptedContributorResource(Object object) {
		Class resourceClass = LegacyResourceSupport.getResourceClass();
		if (resourceClass == null) {
			return null;
		}
		if (resourceClass.isInstance(object)) {
			return null;
		}
		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			Class contributorResourceAdapterClass = LegacyResourceSupport.getIContributorResourceAdapterClass();
			if (contributorResourceAdapterClass == null) {
				return adaptable.getAdapter(resourceClass);
			}
			Object resourceAdapter = adaptable.getAdapter(contributorResourceAdapterClass);
			if (resourceAdapter == null) {
			    resourceAdapter = LegacyResourceSupport.getDefaultContributorResourceAdapter();
			    if (resourceAdapter == null) {
					return null;
				}
			}
			// reflective equivalent of
			//    result = ((IContributorResourceAdapter) resourceAdapter).getAdaptedResource(adaptable);
			
				Method m = getContributorResourceAdapterGetAdaptedResourceMethod();
				if (m != null) {
					try {
						return m.invoke(resourceAdapter, new Object[]{adaptable});
					} catch (IllegalArgumentException e) {
						// shouldn't happen - but play it safe
					} catch (IllegalAccessException e) {
						// shouldn't happen - but play it safe
					} catch (InvocationTargetException e) {
						// shouldn't happen - but play it safe
					}
				}
			
		}
		return null;
	}
    
    private static Method getContributorResourceAdapterGetAdaptedResourceMethod() {
        if (getAdaptedResourceMethod != null) {
            return getAdaptedResourceMethod;
        }
      
        Class c = getIContributorResourceAdapterClass();
        if (c != null) {
            try {
				getAdaptedResourceMethod = c.getDeclaredMethod("getAdaptedResource", new Class[]{IAdaptable.class}); //$NON-NLS-1$
				return getAdaptedResourceMethod;
			} catch (SecurityException e) {
				// shouldn't happen - but play it safe
			} catch (NoSuchMethodException e) {
				// shouldn't happen - but play it safe
			} 
            	
        }
    	
    	return null;
	}


    private static Method getContributorResourceAdapter2GetAdaptedResourceMappingMethod() {
        if (getAdaptedResourceMappingMethod != null) {
            return getAdaptedResourceMappingMethod;
        }
       
        Class c = getIContributorResourceAdapter2Class();
        if (c != null) {
            try {
				getAdaptedResourceMappingMethod = c.getDeclaredMethod("getAdaptedResourceMapping", new Class[]{IAdaptable.class}); //$NON-NLS-1$
				return getAdaptedResourceMappingMethod;
			} catch (SecurityException e) {
				// do nothing - play it safe
			} catch (NoSuchMethodException e) {
				// do nothing - play it safe
			} 
            	
        }
    	
    	return null;
	}

	/**
     * Returns the adapted resource mapping using the <code>IContributorResourceAdapter2</code>
     * registered for the given object. If the Resources plug-in is not loaded
     * the object can not be adapted.
     * 
     * @param object the object to adapt to <code>ResourceMapping</code>.
     * @return returns the adapted resource using the <code>IContributorResourceAdapter2</code>
     * or <code>null</code> if the Resources plug-in is not loaded.
     * 
     * @since 3.1
     */
    public static Object getAdaptedContributorResourceMapping(Object object) {
        Class resourceMappingClass = LegacyResourceSupport.getResourceMappingClass();
        if (resourceMappingClass == null) {
            return null;
        }
        if (resourceMappingClass.isInstance(object)) {
            return null;
        }
        if (object instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) object;
            Class contributorResourceAdapterClass = LegacyResourceSupport.getIContributorResourceAdapterClass();
            if (contributorResourceAdapterClass == null) {
                return adaptable.getAdapter(resourceMappingClass);
            }
            Class contributorResourceAdapter2Class = LegacyResourceSupport.getIContributorResourceAdapter2Class();
            if (contributorResourceAdapter2Class == null) {
                return adaptable.getAdapter(resourceMappingClass);
            }
            Object resourceAdapter = adaptable.getAdapter(contributorResourceAdapterClass);
            Object resourceMappingAdapter;
			if (resourceAdapter != null && contributorResourceAdapter2Class.isInstance(resourceAdapter)) {
            	// The registered adapter also handles resource mappings
            	resourceMappingAdapter = resourceAdapter;
            } else {
            	// Either there is no registered adapter or it doesn't handle resource mappings.
            	// In this case, we will use the default contribution adapter
                resourceMappingAdapter = getDefaultContributorResourceAdapter();
                if (resourceMappingAdapter == null) {
                    return null;
                }
            }
            
           
	            // reflective equivalent of
	            //    result = ((IContributorResourceAdapter2) resourceAdapter).getAdaptedResource(adaptable);

	         Method m = getContributorResourceAdapter2GetAdaptedResourceMappingMethod();
	         if (m != null) {
	            	
				try {
					Object result  = m.invoke(resourceMappingAdapter, new Object[]{adaptable});
					if (result != null) {
		           		return result;
		           	}
				} catch (IllegalArgumentException e) {
					 // shouldn't happen - but play it safe
				} catch (IllegalAccessException e) {
					 // shouldn't happen - but play it safe
				} catch (InvocationTargetException e) {
					 // shouldn't happen - but play it safe
				}
	            	
	         }
            
            
            // If we get here, that means that the object in question doesn't adapt to resource mapping
            // and it's contributed adapter doesn't do the adaptation either.
            // Before we fail, we will attempt to adapt the object to IResource and then to ResourceMapping
            Object r = getAdaptedContributorResource(object);
            if (r != null) {
            	return Platform.getAdapterManager().getAdapter(r, resourceMappingClass);
            }
            
            // we've exhausted every avenue so just return null
            return null;
        }
        // Fallback to querying the adapter manager directly when the object isn't an IAdaptable
        return Platform.getAdapterManager().getAdapter(object, resourceMappingClass);
    }
    
    /**
     * Adapts a selection to the given objectClass considering the Legacy resource 
     * support. Non resource objectClasses are adapted using the <code>IAdapterManager</code>
     * and this may load the plug-in that contributes the adapter factory.
     * <p>
     * The returned selection will only contain elements successfully adapted.
     * </p>
     * @param selection the selection to adapt
     * @param objectClass the class name to adapt the selection to
     * @return an adapted selection
     * 
     * @since 3.1
     */
    public static IStructuredSelection adaptSelection(IStructuredSelection selection, String objectClass) {
		List newSelection = new ArrayList(10);
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object element = it.next();
			Object adaptedElement = getAdapter(element, objectClass);		
			if (adaptedElement != null) {
				newSelection.add(adaptedElement);
			}
		}
		return new StructuredSelection(newSelection);
	}
    
    /**
     * Adapts an object to a specified objectClass considering the Legacy resource 
     * support. Non resource objectClasses are adapted using the <code>IAdapterManager</code>
     * and this may load the plug-in that contributes the adapter factory.
     * <p>
     * The returned selection will be of the same size as the original, and elements that could
     * not be adapted are added to the returned selection as is.
     * </p>
     * @param element the element to adapt
     * @param objectClass the class name to adapt the selection to
     * @return an adapted element or <code>null</code> if the 
     * element could not be adapted.
     * 
     * @since 3.1
     */    
    public static Object getAdapter(Object element, String objectClass) {
		Object adaptedElement = null;
		if (isInstanceOf(element.getClass(), objectClass)) {
			adaptedElement = element;
		} else {		
			// Handle IResource
			if (LegacyResourceSupport.isResourceType(objectClass)) {
				adaptedElement = getAdaptedResource(element);
            } else if (LegacyResourceSupport.isResourceMappingType(objectClass)) {
                adaptedElement = getAdaptedResourceMapping(element);
                if (adaptedElement == null) {
                    // The object doesn't adapt directly so check if it adapts transitively
                    Object resource = getAdaptedResource(element);
                    if (resource != null) {
                        adaptedElement =( (IAdaptable)resource).getAdapter(LegacyResourceSupport.getResourceMappingClass());
                    }
                }
			} else {
				// Handle all other types by using the adapter factory.
				adaptedElement = Platform.getAdapterManager().loadAdapter(element, objectClass);
			}
		}
		return adaptedElement;
	}

	/**
     * Adapt the given element to an <code>IResource</code> using the following 
     * search order:
     * <ol>
     * <li> using the IContributorResourceAdapter registered for the given element, or
     * <li> directly asking the element if it adapts.
     * </ol>
     * 
     * @param element the element to adapt
     * @return an <code>IResource</code> instance if the element could be adapted or <code>null</code>
     * otherwise.
     * @since 3.1
     */
    public static Object getAdaptedResource(Object element) {
		Class resourceClass = LegacyResourceSupport.getResourceClass();
		Object adaptedValue = null;
		if (resourceClass != null) {
			if (resourceClass.isInstance(element)) {
				adaptedValue = element;
			} else {
				adaptedValue = LegacyResourceSupport.getAdaptedContributorResource(element);
			}
		}
		return adaptedValue;
	}

    /**
     * Adapt the given element to an <code>ResourceMapping</code> using the following 
     * search order:
     * <ol>
     * <li> using the IContributorResourceAdapter2 registered for the given element, or
     * <li> directly asking the element if it adapts.
     * </ol>
     * 
     * @param element the element to adapt
     * @return an <code>ResourceMapping</code> instance if the element could be adapted or <code>null</code>
     * otherwise.
     * @since 3.1
     */
    public static Object getAdaptedResourceMapping(Object element) {
        Class resourceMappingClass = LegacyResourceSupport.getResourceMappingClass();
        Object adaptedValue = null;
        if (resourceMappingClass != null) {
            if (resourceMappingClass.isInstance(element)) {
                adaptedValue = element;
            } else {
                adaptedValue = LegacyResourceSupport.getAdaptedContributorResourceMapping(element);
            }
        }
        return adaptedValue;
    }
    
    /**
     * Prevents construction
     */
    private LegacyResourceSupport() {
        // do nothing
    }

}
