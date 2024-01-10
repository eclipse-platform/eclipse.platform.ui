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
 *     Robert Roth <robert.roth.off@gmail.com> - bug 33184
 *******************************************************************************/
package org.eclipse.jface.action;

import org.eclipse.core.runtime.Assert;
import org.eclipse.pde.api.tools.annotations.NoExtend;

/**
 * Abstract superclass for group marker classes.
 * <p>
 * This class is not intended to be subclassed outside the framework.
 * </p>
 */
@NoExtend
public abstract class AbstractGroupMarker extends ContributionItem {
	/**
	 * Constructor for use by subclasses.
	 */
	protected AbstractGroupMarker() {
	}

	/**
	 * Create a new group marker with the given name.
	 * The group name must not be <code>null</code> or the empty string.
	 * The group name is also used as the item id.
	 *
	 * @param groupName the name of the group
	 */
	protected AbstractGroupMarker(String groupName) {
		super(groupName);
		Assert.isTrue(groupName != null && groupName.length() > 0);
	}

	/**
	 * Returns the group name.
	 *
	 * @return the group name
	 */
	public String getGroupName() {
		return getId();
	}

	/**
	 * Always return <code>false</code> as group markers (including separators)
	 * are only there for visual separation, not meant to be actionable.
	 */
	@Override
	public boolean isEnabled() {
		return false;
	}

	/**
	 * The <code>AbstractGroupMarker</code> implementation of this
	 * <code>IContributionItem</code> method returns <code>true</code> iff the
	 * id is not <code>null</code>. Subclasses may override.
	 */
	@Override
	public boolean isGroupMarker() {
		return getId() != null;
	}
}
