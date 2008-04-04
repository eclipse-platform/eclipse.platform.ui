/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

/**
 * A trigger point represents a place within the Workbench that has the
 * potential to enable activities. Instances of this class may be obtained from
 * {@link org.eclipse.ui.activities.ITriggerPointManager#getTriggerPoint(String)}.
 * Instances of this interface are passed as a parameter to
 * {@link org.eclipse.ui.activities.ITriggerPointAdvisor#allow(ITriggerPoint, IIdentifier)}
 * and may be used by the advisor to determine policy.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.activities.ITriggerPointAdvisor
 * @see org.eclipse.ui.activities.ITriggerPointManager
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITriggerPoint {

	/**
	 * The interactive hint key.  Value <code>"interactive"</code>.
	 */
	public static final String HINT_INTERACTIVE = "interactive"; //$NON-NLS-1$
	
	/**
	 * A hint key for activities that are enabled based on core expressions.
	 * 
	 * @since 3.4
	 */
	public static final String HINT_PRE_UI = "pre_UI"; //$NON-NLS-1$
	
	/**
	 * Return the id of this trigger point.
	 * 
	 * @return the id
	 */
	String getId();

	/**
	 * Return the hint with the given key defined on this trigger point.
	 * 
	 * @param key the hint key
	 * @return the hint
	 */
	String getStringHint(String key);
	
	
	/**
     * Return the hint with the given key defined on this trigger point as
     * interpreted as a <code>boolean</code>.
     * 
     * @param key the hint key
     * @return the hint
     * @see Boolean#valueOf(java.lang.String)
     */	
	boolean getBooleanHint(String key);
}
