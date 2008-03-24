/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.ui.views.markers.FilterConfigurationArea;

/**
 * GroupFilterConfigurationArea is the FilterConfigurationArea for the special case
 * group level settings for a {@link MarkerFieldFilterGroup}
 * @since 3.4
 *
 */
abstract class GroupFilterConfigurationArea extends FilterConfigurationArea {
	
	/**
	 * Apply to the group
	 * @param group
	 */
	public abstract void applyToGroup(MarkerFieldFilterGroup group);
	
	/**
	 * Initialise from the group
	 * @param group
	 */
	public abstract void initializeFromGroup(MarkerFieldFilterGroup group);
}
