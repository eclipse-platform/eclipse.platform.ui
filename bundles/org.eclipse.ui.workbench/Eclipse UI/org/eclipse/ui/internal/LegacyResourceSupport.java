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

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * Provides access to resource-specific classes, needed to provide
 * backwards compatibility for resource-specific functions which
 * could not be moved up from the generic workbench layer to the
 * IDE layer.
 */
public final class LegacyResourceSupport {

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
     * Prevents construction
     */
    private LegacyResourceSupport() {
        // do nothing
    }

}