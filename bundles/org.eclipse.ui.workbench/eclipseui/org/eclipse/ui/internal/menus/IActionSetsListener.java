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

package org.eclipse.ui.internal.menus;

import org.eclipse.ui.internal.ActionSetsEvent;

/**
 * <p>
 * A listener to changes in the list of active action sets.
 * </p>
 * <p>
 * This class is only intended for internal use within
 * <code>org.eclipse.ui.workbench</code>.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 *
 * @since 3.2
 */
public interface IActionSetsListener {

	/**
	 * Indicates that the list of active action sets has changed.
	 *
	 * @param event The event carrying information about the new state of the action
	 *              sets; never <code>null</code>.
	 */
	void actionSetsChanged(ActionSetsEvent event);

}
