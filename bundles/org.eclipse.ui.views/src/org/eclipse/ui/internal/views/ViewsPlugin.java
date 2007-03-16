/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the views UI plug-in (id <code>"org.eclipse.ui.views"</code>).
 * <p>
 * This class provides static methods and fields only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @since 2.1
 */
public final class ViewsPlugin extends AbstractUIPlugin {
    /**
     * Views UI plug-in id (value <code>"org.eclipse.ui.views"</code>).
     */
    public static final String PLUGIN_ID = "org.eclipse.ui.views"; //$NON-NLS-1$
	
	private final static String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$

    private static ViewsPlugin instance;

    /**
     * Returns the singleton instance.
     * 
     * @return the singleton instance.
     */
    public static ViewsPlugin getDefault() {
        return instance;
    }

    /**
     * Creates a new instance of the receiver.
     * 
     * @see org.eclipse.core.runtime.Plugin#Plugin()
     */
    public ViewsPlugin() {
        super();
        instance = this;
    }
	
	/**
	 * Get the workbench image with the given path relative to
	 * ICON_PATH.
	 * @param relativePath
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor getViewImageDescriptor(String relativePath){
		return imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH + relativePath);
	}
    
    /**
     * If it is possible to adapt the given object to the given type, this
     * returns the adapter. Performs the following checks:
     * 
     * <ol>
     * <li>Returns <code>sourceObject</code> if it is an instance of the
     * adapter type.</li>
     * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
     * <li>If sourceObject is not an instance of PlatformObject (which would have
     * already done so), the adapter manager is queried for adapters</li>
     * </ol>
     * 
     * Otherwise returns null.
     * 
     * @param sourceObject
     *            object to adapt, or null
     * @param adapter
     *            type to adapt to
     * @param activatePlugins 
     *            true if IAdapterManager.loadAdapter should be used (may trigger plugin activation)
     * @return a representation of sourceObject that is assignable to the
     *         adapter type, or null if no such representation exists
     */
    public static Object getAdapter(Object sourceObject, Class adapter, boolean activatePlugins) {
    	Assert.isNotNull(adapter);
        if (sourceObject == null) {
            return null;
        }
        if (adapter.isInstance(sourceObject)) {
            return sourceObject;
        }

        if (sourceObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) sourceObject;

            Object result = adaptable.getAdapter(adapter);
            if (result != null) {
                // Sanity-check
                Assert.isTrue(adapter.isInstance(result));
                return result;
            }
        } 
        
        if (!(sourceObject instanceof PlatformObject)) {
        	Object result;
        	if (activatePlugins) {
        		result = Platform.getAdapterManager().loadAdapter(sourceObject, adapter.getName());
        	} else {
        		result = Platform.getAdapterManager().getAdapter(sourceObject, adapter);
        	}
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}
