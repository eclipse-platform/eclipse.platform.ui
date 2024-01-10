/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 487940
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.Collection;

import org.eclipse.pde.api.tools.annotations.NoExtend;

/**
 * This implementation of <code>IStructuredContentProvider</code> handles
 * the case where the viewer input is an unchanging array or collection of elements.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework.
 * </p>
 *
 * @since 2.1
 */
@NoExtend
public class ArrayContentProvider implements IStructuredContentProvider {

	private static ArrayContentProvider instance;

	/**
	 * Returns an instance of ArrayContentProvider. Since instances of this
	 * class do not maintain any state, they can be shared between multiple
	 * clients.
	 *
	 * @return an instance of ArrayContentProvider
	 *
	 * @since 3.5
	 */
	public static ArrayContentProvider getInstance() {
		synchronized(ArrayContentProvider.class) {
			if (instance == null) {
				instance = new ArrayContentProvider();
			}
			return instance;
		}
	}
	/**
	 * Returns the elements in the input, which must be either an array or a
	 * <code>Collection</code>.
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		if (inputElement instanceof Collection) {
			return ((Collection) inputElement).toArray();
		}
		return new Object[0];
	}
}
