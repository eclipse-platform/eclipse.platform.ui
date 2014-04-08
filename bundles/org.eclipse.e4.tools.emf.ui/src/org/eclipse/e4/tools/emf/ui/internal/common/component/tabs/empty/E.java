/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty;

import java.util.Collection;

/**
 * E is for empty. Very useful functions for determining empty objects. Avoids
 * null checking and content checking, and makes code clearer. <br />
 * An object is empty if it is null, an empty string, or an empty collection
 *
 * @author Steven Spungin
 *
 */
public class E {

	public static boolean notEmpty(String object) {
		return !isEmpty(object);
	}

	public static boolean notEmpty(Collection<?> object) {
		return !isEmpty(object);
	}

	public static boolean notEmpty(Object object) {
		return !isEmpty(object);
	}

	public static boolean isEmpty(String object) {
		return (object == null || object.isEmpty());
	}

	public static boolean isEmpty(Collection<?> object) {
		return (object == null || object.isEmpty());
	}

	public static boolean isEmpty(Object object) {
		if (object == null) {
			return true;
		} else if (object instanceof String && ((String) object).isEmpty()) {
			return true;
		} else if (object instanceof Collection<?> && ((Collection<?>) object).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param obj1
	 * @param obj2
	 * @return true if (both objects are null) or (both objects are not null and
	 *         equal)
	 */
	static public boolean equals(Object obj1, Object obj2) {
		if (obj1 == null) {
			if (obj2 == null) {
				return true;
			} else {
				return false;
			}
		} else if (obj2 == null) {
			return false;
		} else {
			return obj1.equals(obj2);
		}
	}

	/**
	 * Compares 2 objects.
	 *
	 * @param obj1
	 * @param obj2
	 * @return If both objects are null, returns 0. If only 1 object is null, it
	 *         will return 1 or -1. Otherwise call compareTo on the first
	 *         object.
	 */
	static public <T> int compareTo(Comparable<T> obj1, T obj2) {
		if (obj1 == null) {
			if (obj2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (obj2 == null) {
			return 1;
		} else {
			return obj1.compareTo(obj2);
		}

	}
}
