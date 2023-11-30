/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.navigator;

/**
 * A descriptive construct to relay information about a menu insertion point;
 * including the name of the insertion point and whether the item should be
 * rendered as a separator or group marker.
 *
 * @since 3.2
 */
public final class MenuInsertionPoint {
	private String name;

	private boolean isSeparator;

	private String toString;

	/**
	 *
	 * @param aName
	 *            The name that clients will refer to when inserting items into
	 *            the menu at this point.
	 * @param toMakeASeparator
	 *            A value of true will cause the point to be rendered as a
	 *            org.eclipse.jface.action.Separator (a "bar" in the menu);
	 *            false will cause the point to be rendered as a
	 *            org.eclipse.jface.action.GroupMarker.
	 */
	public MenuInsertionPoint(String aName, boolean toMakeASeparator) {
		name = aName;
		isSeparator = toMakeASeparator;
	}

	/**
	 *
	 * @return True if the point should be rendered as a
	 *         org.eclipse.jface.action.Separator (a "bar" in the menu); or
	 *         false if the point should be rendered as a
	 *         org.eclipse.jface.action.GroupMarker.
	 */
	public boolean isSeparator() {
		return isSeparator;
	}

	/**
	 *
	 * @return The name that clients will refer to when inserting items into the
	 *         menu at this point.
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		if(toString == null) {
			toString = "MenuInsertionPoint[name=\""+name+"\", isSeparator="+isSeparator+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return toString;
	}
}