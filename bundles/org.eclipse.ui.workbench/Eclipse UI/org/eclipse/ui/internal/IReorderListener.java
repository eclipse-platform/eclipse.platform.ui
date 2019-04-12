/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal;

/**
 * Simple interface for informing clients of reordering of an object in an
 * ordered list.
 *
 */
interface IReorderListener {

	/**
	 * An object has been moved, clients might need to react.
	 *
	 * @param obj
	 * @param newIndex
	 *
	 */
	void reorder(Object obj, int newIndex);
}
