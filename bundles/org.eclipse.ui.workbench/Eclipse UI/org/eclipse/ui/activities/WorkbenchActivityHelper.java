/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchPreferences;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * A utility class that contains helpful methods for interacting with the
 * activities API.  The methods in this class are experimental and may change.
 * 
 * @since 3.0
 */
public final class WorkbenchActivityHelper {

	/**
	 * Utility method to create a <code>String</code> containing the plugin
	 * and local ids of a contribution.
	 * 
	 * @param contribution
	 *            the contribution to use.
	 * @return the unified id.
	 */
	public static final String createUnifiedId(IPluginContribution contribution) {
		if (contribution.getPluginId() != null)
			return contribution.getPluginId() + '/' + contribution.getLocalId();
		return contribution.getLocalId();
	}
	
    /**
     * @return the <code>RGB</code> that should be used to highlight filtered 
     * contributions.
     */
    public static RGB getFilterRGB() {
    	return JFaceResources.getColorRegistry().getRGB(IWorkbenchConstants.COLOR_ACTIVITY_HIGHLIGHT);
    }
	

    /**
     * @return the <code>Color</code> that should be used to highlight filtered 
     * contributions.
     */
    public static Color getFilterColor() {
        return JFaceResources.getColorRegistry().get(IWorkbenchConstants.COLOR_ACTIVITY_HIGHLIGHT);
    }	
	
	/**
	 * Answers whether the provided object should be filtered from the UI based
	 * on activity state. Returns false except when the object is an instance
	 * of <code>IPluginContribution</code> whos unified id matches an <code>IIdentifier</code>
	 * that is currently disabled.
	 * 
	 * @param object
	 *            the object to test.
	 * @return whether the object should be filtered.
	 * @see createUnifiedId(IPluginContribution)
	 */
	public static final boolean filterItem(Object object) {
		if (object instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) object;
			if (contribution.getPluginId() != null) {
				IWorkbenchActivitySupport workbenchActivitySupport =
				PlatformUI.getWorkbench().getActivitySupport();
				IIdentifier identifier =
					workbenchActivitySupport
						.getActivityManager()
						.getIdentifier(
						createUnifiedId(contribution));
				if (!identifier.isEnabled())
					return true;

			}
		}
		return false;
	}

	/**
	 * @return whether the UI is set up to filter contributions (has defined
	 *         activity categories).
	 */
	public static final boolean isFiltering() {
		return !PlatformUI.getWorkbench().getActivitySupport().getActivityManager().getDefinedCategoryIds().isEmpty();
	}
	
	/** 
	 * @return whether the UI will provide a "show all" option when applicable.
	 */
	public static final boolean showAll() {
		return isFiltering() && PlatformUI.getWorkbench().getPreferenceStore().getBoolean(IWorkbenchPreferences.SHOULD_ALLOW_SHOW_ALL);
	}

	/**
	 * Not intended to be instantiated.
	 */
	private WorkbenchActivityHelper() {
	    // no-op
	}
}
