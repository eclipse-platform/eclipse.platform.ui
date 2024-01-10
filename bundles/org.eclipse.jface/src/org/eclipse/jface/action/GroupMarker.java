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
package org.eclipse.jface.action;

import org.eclipse.pde.api.tools.annotations.NoExtend;

/**
 * A group marker is a special kind of contribution item denoting
 * the beginning of a group. These groups are used to structure
 * the list of items. Unlike regular contribution items and
 * separators, group markers have no visual representation.
 * The name of the group is synonymous with the contribution item id.
 * <p>
 * This class may be instantiated; it is not intended to be
 * subclassed outside the framework.
 * </p>
 */
@NoExtend
public class GroupMarker extends AbstractGroupMarker {
	/**
	 * Create a new group marker with the given name.
	 * The group name must not be <code>null</code> or the empty string.
	 * The group name is also used as the item id.
	 *
	 * @param groupName the name of the group
	 */
	public GroupMarker(String groupName) {
		super(groupName);
	}

	/**
	 * The <code>GroupMarker</code> implementation of this method
	 * returns <code>false</code> since group markers are always invisible.
	 */
	@Override
	public boolean isVisible() {
		return false;
	}
}
