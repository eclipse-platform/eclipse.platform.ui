/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

/**
 * Contains a collection of known trigger points. An instance of this class may
 * be obtained from
 * {@link org.eclipse.ui.activities.IWorkbenchActivitySupport#getTriggerPointManager()}.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @see org.eclipse.ui.activities.ITriggerPoint
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITriggerPointManager {
	
	/**
     * Constant representing the id of an unknown trigger point. Used by clients
     * of {@link WorkbenchActivityHelper#allowUseOf(Object)} for trigger point
     * determination logic.
     */
	public static final String UNKNOWN_TRIGGER_POINT_ID = "org.eclipse.ui.internal.UnknownTriggerPoint"; //$NON-NLS-1$
	
	/**
	 * Return the trigger point with the given id.
	 * 
	 * @param id the trigger point id
	 * @return the trigger point or <code>null</code>
	 */
	ITriggerPoint getTriggerPoint(String id);
	
	/**
	 * Return the set of defined trigger point ids.
	 * 
	 * @return the defined ids.  Never <code>null</code> but may be empty.
	 */
	Set getDefinedTriggerPointIds();
}
