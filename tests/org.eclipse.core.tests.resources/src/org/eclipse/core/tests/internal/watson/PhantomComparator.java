/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.watson;

import org.eclipse.core.internal.watson.IElementComparator;

/**
 * Used in conjunction with PluggableDeltaLogicTests
 */
public class PhantomComparator extends TestElementComparator implements IElementComparator {
	private static PhantomComparator fSingleton;

	/**
	 * Force clients to use the singleton
	 */
	protected PhantomComparator() {
		super();
	}

	/**
	 * Compare based on name and phantom status.
	 */
	public int compare(Object old, Object newt) {
		if (old == null && newt == null) {
			return K_NO_CHANGE;
		}

		PhantomElementData oldInfo = null, newInfo = null;
		if (old == null) {
			/* ignore added phantoms */
			newInfo = (PhantomElementData) newt;
			return newInfo.isPhantom ? K_NO_CHANGE : CHANGED;
		}
		if (newt == null) {
			/* ignore deleted phantoms */
			oldInfo = (PhantomElementData) old;
			return oldInfo.isPhantom ? K_NO_CHANGE : CHANGED;
		}

		try {
			oldInfo = (PhantomElementData) old;
			newInfo = (PhantomElementData) newt;
		} catch (ClassCastException e) {
		}

		if (oldInfo.isPhantom) {
			if (newInfo.isPhantom) {
				/* ignore changes to phantoms */
				return K_NO_CHANGE;
			} else {
				/* phantom -> real is an addition */
				return ADDED;
			}
		} else {
			if (newInfo.isPhantom) {
				/* real -> phantom == deletion */
				return REMOVED;
			} else {
				/* not a phantom */
				if (oldInfo.name == null && newInfo.name == null) {
					return K_NO_CHANGE;
				}
				if (oldInfo.name == null || newInfo.name == null)
					return CHANGED;
				if (oldInfo.name.equals(newInfo.name)) {
					return K_NO_CHANGE;
				} else {
					return CHANGED;
				}
			}
		}
	}

	/**
	 * Returns the singleton instance
	 */
	public static IElementComparator getComparator() {
		if (fSingleton == null) {
			fSingleton = new PhantomComparator();
		}
		return fSingleton;
	}
}
