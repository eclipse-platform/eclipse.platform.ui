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
 * 
 * <em>EXPERIMENTAL</em>
 * @since 3.1
 */
public interface ITriggerPointAdvisor {

	/**
	 * Answer whether the activities bound to the contribtion should be enabled
	 * when triggered by the provided trigger point.
	 * 
	 * @param triggerPoint
	 * @param identifier
	 * @return the set of activities to enable. If empty the caller may proceed
	 *         with their usage of the object represented by the identifier. If
	 *         <code>null</code> the caller should abort the action.
	 */
	Set allow(ITriggerPoint triggerPoint, IIdentifier identifier);
}
