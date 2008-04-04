/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An instance of this interface provides support for managing 
 * <code>IWorkbench</code> activities.  An instance of this interface may be
 * obtained via {@link org.eclipse.ui.IWorkbench#getActivitySupport()}. 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *  
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchActivitySupport {

    /**
     * Returns the activity manager for the workbench.
     * 
     * @return the activity manager for the workbench. Guaranteed not to be
     *         <code>null</code>.
     */
    IActivityManager getActivityManager();

    /**
     * Sets the set of identifiers to enabled activities.
     * 
     * @param enabledActivityIds
     *            the set of identifiers to enabled activities. This set may be
     *            empty, but it must not be <code>null</code>. If this set
     *            is not empty, it must only contain instances of <code>String</code>.
     */
    void setEnabledActivityIds(Set enabledActivityIds);
    
    /**
     * Return the image associated with this activity.
     * 
     * @param activity the activity
     * @return the image associated with this activity.  Never <code>null</code>.
     * @since 3.1
     */
    ImageDescriptor getImageDescriptor(IActivity activity);

    /**
     * Return the image associated with this category.
     * 
     * @param category the category
     * @return the image associated with this category.  Never <code>null</code>.
     * @since 3.1
     */
    ImageDescriptor getImageDescriptor(ICategory category);
	
	/**
	 * Return the trigger point manager for this instance.
	 * 
	 * @return the trigger point manager.  Never <code>null</code>.
	 * @since 3.1
	 */
	ITriggerPointManager getTriggerPointManager();
    
    /**
     * Return a copy of the current activity set. This copy will contain all
     * activity and category definitions that the workbench activity manager
     * contains, and will have the same enabled state. It will not have the same
     * listeners. This is useful for user interfaces that wish to modify the
     * activity enablement state (such as preference pages).
     * 
     * @return a copy of the current activity set
     * @since 3.1
     */
    IMutableActivityManager createWorkingCopy();
}
