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
package org.eclipse.ui.internal;

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
     * <code>Class.forName("org.eclipse.ui.IContributorResourceAdapter")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.0
     */
    private static Class icontributorResourceAdapterClass = null;

    /**
     * Cached value of
     * <code>Class.forName("org.eclipse.ui.internal.ide.DefaultContributorResourceAdapter")</code>;
     * <code>null</code> if not initialized or not present.
     * @since 3.0
     */
    private static Class defaultContributorResourceAdapterClass = null;

    /**
     * Indicates whether the resources plug-in is even around.
     * Without the resources plug-in, adapting to resource is moot.
     */
    private static boolean resourcesPossible = true;

    /**
     * Indicates whether the IDE plug-in (which supplies the
     * resource contribution adapters) is even around.
     */
    private static boolean resourceAdapterPossible = true;

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
        if (!resourcesPossible) {
            // tried before and failed
            return null;
        }

        // resource plug-in is not on prereq chain of generic wb plug-in
        // hence: IResource.class won't compile
        // and Class.forName("org.eclipse.core.resources.IResource") won't find it
        // need to be trickier...
        Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
        if (bundle == null) {
            // resources plug-in is not around
            // assume that it will never be around
            resourcesPossible = false;
            return null;
        }
        // resources plug-in is around
        // it's not our job to activate the plug-in
        if (!BundleUtility.isActivated(bundle)) {
            // assume it might come alive later
            resourcesPossible = true;
            return null;
        }
        try {
            Class c = bundle.loadClass("org.eclipse.core.resources.IResource"); //$NON-NLS-1$
            // remember for next time
            iresourceClass = c;
            return iresourceClass;
        } catch (ClassNotFoundException e) {
            // unable to load IResource - sounds pretty serious
            // treat as if resources plug-in were unavailable
            resourcesPossible = false;
            return null;
        }
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
        if (!resourceAdapterPossible) {
            // tried before and failed
            return null;
        }

        // IDE plug-in is not on prereq chain of generic wb plug-in
        // hence: IContributorResourceAdapter.class won't compile
        // and Class.forName("org.eclipse.ui.IContributorResourceAdapter") won't find it
        // need to be trickier...
        Bundle bundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
        if (bundle == null) {
            // IDE plug-in is not around
            // assume that it will never be around
            resourceAdapterPossible = false;
            return null;
        }
        // IDE plug-in is around
        // it's not our job to activate the plug-in
        if (!BundleUtility.isActivated(bundle)) {
            // assume it might come alive later
            resourceAdapterPossible = true;
            return null;
        }
        try {
            Class c = bundle
                    .loadClass("org.eclipse.ui.IContributorResourceAdapter"); //$NON-NLS-1$
            // remember for next time
            icontributorResourceAdapterClass = c;
            return icontributorResourceAdapterClass;
        } catch (ClassNotFoundException e) {
            // unable to load IContributorResourceAdapter - sounds pretty serious
            // treat as if IDE plug-in were unavailable
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
        if (!resourceAdapterPossible) {
            // tried before and failed
            return null;
        }

        // IDE plug-in is not on prereq chain of generic wb plug-in
        // hence: DefaultContributorResourceAdapter.class won't compile
        // and Class.forName("org.eclipse.ui.internal.ide.DefaultContributorResourceAdapter") won't find it
        // need to be trickier...
        Bundle bundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
        if (bundle == null) {
            // IDE plug-in is not around
            // assume that it will never be around
            resourceAdapterPossible = false;
            return null;
        }
        // IDE plug-in is around
        // it's not our job to activate the plug-in
        if (!BundleUtility.isActivated(bundle)) {
            // assume it might come alive later
            resourceAdapterPossible = true;
            return null;
        }
        try {
            Class c = bundle
                    .loadClass("org.eclipse.ui.internal.ide.DefaultContributorResourceAdapter"); //$NON-NLS-1$
            // remember for next time
            defaultContributorResourceAdapterClass = c;
            return defaultContributorResourceAdapterClass;
        } catch (ClassNotFoundException e) {
            // unable to load DefaultContributorResourceAdapter - sounds pretty serious
            // treat as if IDE plug-in were unavailable
            resourceAdapterPossible = false;
            return null;
        }
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
     * Returns the class search order starting with <code>extensibleClass</code>.
     * The search order is defined in this class' comment.
     * 
     * @since 3.1
     */
    private static boolean isInstanceOf(Class clazz, String type) {
		if (clazz.getName().equals(type))
			return true;
		Class superClass= clazz.getSuperclass();
		if (superClass != null && isInstanceOf(superClass, type))
			return true;
		Class[] interfaces= clazz.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			if (isInstanceOf(interfaces[i], type))
				return true;
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
				return null;
			}
			Object resourceAdapter = adaptable.getAdapter(contributorResourceAdapterClass);
			if (resourceAdapter == null) {
				// reflective equivalent of
				//    resourceAdapter = DefaultContributorResourceAdapter.getDefault();
				try {
					Class c = LegacyResourceSupport.getDefaultContributorResourceAdapterClass();
					Method m = c.getDeclaredMethod("getDefault", new Class[0]); //$NON-NLS-1$
					resourceAdapter = m.invoke(null, new Object[0]);
				} catch (Exception e) {
					// shouldn't happen - but play it safe
					return null;
				}
			}
			Object result;
			// reflective equivalent of
			//    result = ((IContributorResourceAdapter) resourceAdapter).getAdaptedResource(adaptable);
			try {
				Method m = contributorResourceAdapterClass.getDeclaredMethod("getAdaptedResource", new Class[]{IAdaptable.class}); //$NON-NLS-1$
				result = m.invoke(resourceAdapter, new Object[]{adaptable});
			} catch (Exception e) {
				// shouldn't happen - but play it safe
				return null;
			}
			return result;
		}
		return null;
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
			if (adaptedElement != null)
				newSelection.add(adaptedElement);
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
     * Prevents construction
     */
    private LegacyResourceSupport() {
        // do nothing
    }

}