/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui;

/**
 * Objects implementing this interface are capable of saving their state in an
 * {@link IMemento}.
 *
 * @since 3.1
 */
public interface IPersistable {
	/**
	 * Saves the state of the object in the given memento.
	 *
	 * @param memento the storage area for object's state
	 */
	void saveState(IMemento memento);
}
