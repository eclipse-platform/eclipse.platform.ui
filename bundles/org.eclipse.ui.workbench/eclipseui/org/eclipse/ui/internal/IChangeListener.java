/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
 * Interface that can receive change notifiecations from a Model object
 */
public interface IChangeListener {
	/**
	 * Called with false when the listener is first attached to the model, and
	 * called with true every time the model's state changes.
	 */
	void update(boolean changed);
}
