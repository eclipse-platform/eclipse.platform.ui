package org.eclipse.ui.activities;

import java.util.Set;

public interface IWorkbenchActivitySupport {

	/**
	 * Returns the activity manager for the workbench. 
	 * 
	 * @return the activity manager for the workbench. Guaranteed not to be 
	 * 		   <code>null</code>.
	 * @since 3.0
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
}
