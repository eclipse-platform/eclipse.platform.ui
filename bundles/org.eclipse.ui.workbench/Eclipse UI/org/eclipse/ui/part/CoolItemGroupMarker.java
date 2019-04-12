/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.jface.action.GroupMarker;

/**
 * A group marker used by EditorActionBars to delineate CoolItem groups. Use
 * this marker when contributing to the ToolBar for the EditorActionBar.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CoolItemGroupMarker extends GroupMarker {
	/**
	 * Create a new group marker with the given name. The group name must not be
	 * <code>null</code> or the empty string. The group name is also used as the
	 * item id.
	 *
	 * Note that CoolItemGroupMarkers must have a group name and the name must be
	 * unique.
	 *
	 * @param groupName the name of the group
	 */
	public CoolItemGroupMarker(String groupName) {
		super(groupName);
	}
}
