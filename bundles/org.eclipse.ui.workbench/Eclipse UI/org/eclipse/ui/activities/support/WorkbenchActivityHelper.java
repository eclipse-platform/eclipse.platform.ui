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
package org.eclipse.ui.activities.support;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;

/**
 * A utility class that contains helpful methods for interacting with the
 * activities API.
 * 
 * @since 3.0
 */
public final class WorkbenchActivityHelper {

	/**
	 * Not intended to be instantiated.
	 */
	private WorkbenchActivityHelper() {
	}

	/**
	 * Utility method to create a <code>String</code> containing the plugin
	 * and local ids of a contribution.
	 * 
	 * @param contribution
	 *            the contribution to use.
	 * @return the unified id.
	 */
	public static final String createUnifiedId(IPluginContribution contribution) {
		if (contribution.fromPlugin())
			return contribution.getPluginId() + '/' + contribution.getLocalId();
		return contribution.getLocalId();
	}

	/**
	 * Answers whether the provided object should be filtered from the UI based
	 * on activity state. Returns false except when the object is an instance
	 * of <code>IPluginContribution</code> whos unified id matches an 
     * <code>IIdentifier</code> that is currently disabled.
	 * 
	 * @param object
	 *            the object to test.
	 * @return whether the object should be filtered.
	 * @see createUnifiedId(IPluginContribution)
	 */
	public static final boolean filterItem(Object object) {
		if (object instanceof IPluginContribution) {
			IPluginContribution contribution = (IPluginContribution) object;
			if (contribution.fromPlugin()) {
				IIdentifier identifier =
					PlatformUI
						.getWorkbench()
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
		return !PlatformUI
			.getWorkbench()
			.getActivityManager()
			.getDefinedCategoryIds()
			.isEmpty();
	}
}
