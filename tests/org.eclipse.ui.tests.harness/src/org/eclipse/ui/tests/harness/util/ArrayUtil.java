/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.util.Random;

/**
 * <code>ArrayUtil</code> contains methods for array
 * examination.
 */
public class ArrayUtil {
	private static Random randomBox = new Random();

	/**
	 * Returns a random object chosen from an array.
	 *
	 * @param array the input array
	 * @return a random object in the array
	 */
	public static Object pickRandom(Object[] array) {
		int num = randomBox.nextInt(array.length);
		return array[num];
	}

	/**
	 * Returns whether an array is not null and
	 * each object in the array is not null.
	 *
	 * @param array the input array
	 * @return <code>true or false</code>
	 */
	public static boolean checkNotNull(Object[] array) {
		if (array == null)
			return false;
		for (Object a : array) {
			if (a == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether an array contains a given object.
	 *
	 * @param array the input array
	 * @param element the test object
	 * @return <code>true</code> if the array contains the object,
	 * 		<code>false</code> otherwise.
	 */
	public static boolean contains(Object[] array, Object element) {
		if (array == null || element == null)
			return false;
		for (Object a : array) {
			if (a == element) {
				return true;
			}
		}
		return false;
	}

}
