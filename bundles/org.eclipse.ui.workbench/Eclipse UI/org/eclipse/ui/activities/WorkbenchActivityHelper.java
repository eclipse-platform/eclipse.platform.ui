/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.activities.ws.WorkbenchActivitySupport;

/**
 * A utility class that contains helpful methods for interacting with the
 * activities API.
 * 
 * @since 3.0
 */
public final class WorkbenchActivityHelper {
	
	/**
	 * Return the identifier that maps to the given contribution.
	 * 
	 * @param contribution the contribution
	 * @return the identifier
	 * @since 3.1
	 */
	public static IIdentifier getIdentifier(IPluginContribution contribution) {
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
				.getWorkbench().getActivitySupport();
		IIdentifier identifier = workbenchActivitySupport.getActivityManager()
				.getIdentifier(createUnifiedId(contribution));
		return identifier;
	}
	
    /**
	 * Answers whether a given contribution is allowed to be used based on
	 * activity enablement. If it is currently disabled, then a dialog is opened
	 * and the user is prompted to activate the requried activities. If the user
	 * declines their activation then false is returned. In all other cases
	 * <code>true</code> is returned.
	 * 
	 * @param object
	 *            the contribution to test.
	 * @return whether the contribution is allowed to be used based on activity
	 *         enablement.
	 * @deprecated 
	 * @see #allowUseOf(ITriggerPoint, Object)
	 */
    public static boolean allowUseOf(Object object) {
		return allowUseOf(PlatformUI.getWorkbench().getActivitySupport()
				.getTriggerPointManager().getTriggerPoint(
						ITriggerPointManager.UNKNOWN_TRIGGER_POINT_ID), object);
    }

    /**
	 * Answers whether a given contribution is allowed to be used based on
	 * activity enablement. If it is currently disabled, then a dialog is opened
	 * and the user is prompted to activate the requried activities. If the user
	 * declines their activation then false is returned. In all other cases
	 * <code>true</code> is returned.
	 * 
	 * @param triggerPoint 
	 * 			  the trigger point being hit
	 * @param object
	 *            the contribution to test.
	 * @return whether the contribution is allowed to be used based on activity
	 *         enablement.
	 */	
	public static boolean allowUseOf(ITriggerPoint triggerPoint, Object object) {
        if (!isFiltering())
            return true;
		if (triggerPoint == null)
			return true;
        if (object instanceof IPluginContribution) {
            IPluginContribution contribution = (IPluginContribution) object;
            IIdentifier identifier = getIdentifier(contribution);
            return allow(triggerPoint, identifier);
        }
        return true;
	}

    /**
     * Answers whether a given identifier is enabled.  If it is not enabled, 
     * then a dialog is opened and the user is prompted to enable the associated 
     * activities.  
     *  
     * @param triggerPoint thr trigger point to test
     * @param identifier the identifier to test.
     * @return whether the identifier is enabled.
     */
    private static boolean allow(ITriggerPoint triggerPoint, IIdentifier identifier) {
        if (identifier.isEnabled()) {
            return true;
        }
		
		ITriggerPointAdvisor advisor = ((WorkbenchActivitySupport) PlatformUI
				.getWorkbench().getActivitySupport()).getTriggerPointAdvisor();
		Set activitiesToEnable = advisor.allow(triggerPoint, identifier);
		if (activitiesToEnable == null)
			return false;
		
		enableActivities(activitiesToEnable);
		return true;
    }

    /**
     * Utility method to create a <code>String</code> containing the plugin
     * and extension ids of a contribution.  This will have the form 
     * <pre>pluginId/extensionId</pre>.  If the IPluginContribution does not 
     * define a plugin id then the extension id alone is returned.
     * 
     * @param contribution the contribution to use
     * @return the unified id
     */
    public static final String createUnifiedId(IPluginContribution contribution) {
        if (contribution.getPluginId() != null)
            return contribution.getPluginId() + '/' + contribution.getLocalId();
        return contribution.getLocalId();
    }

    /**
     * Enables the set of activities.
     * 
     * @param activities the activities to enable
     */
    private static void enableActivities(Collection activities) {
        IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench()
                .getActivitySupport();
        Set newSet = new HashSet(activitySupport.getActivityManager()
                .getEnabledActivityIds());
        newSet.addAll(activities);
        activitySupport.setEnabledActivityIds(newSet);
    }

    /**
     * Answers whether the provided object should be filtered from the UI based
     * on activity state. Returns <code>false</code> except when the object is an instance
     * of <code>IPluginContribution</code> whos unified id matches an <code>IIdentifier</code>
     * that is currently disabled.
     * 
     * @param object the object to test
     * @return whether the object should be filtered
     * @see #createUnifiedId(IPluginContribution)
     */
    public static final boolean filterItem(Object object) {
        if (object instanceof IPluginContribution) {
            IPluginContribution contribution = (IPluginContribution) object;
            IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
                    .getWorkbench().getActivitySupport();
            IIdentifier identifier = workbenchActivitySupport
                    .getActivityManager().getIdentifier(
                            createUnifiedId(contribution));
            if (!identifier.isEnabled())
                return true;
        }
        return false;
    }

    /**
     * Returns whether the UI is set up to filter contributions.  This is the 
     * case if there are defined activities.
     * 
     * @return whether the UI is set up to filter contributions
     */
    public static final boolean isFiltering() {
        return !PlatformUI.getWorkbench().getActivitySupport()
                .getActivityManager().getDefinedActivityIds().isEmpty();
    }

    /**
     * Not intended to be instantiated.
     */
    private WorkbenchActivityHelper() {
        // no-op
    }
}