/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * <p>
 * An event indicating changes to the action sets in a particular workbench
 * window.
 * </p>
 * <p>
 * This class is only intended for internal use within
 * <code>org.eclipse.ui.workbench</code>.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is eventually intended to exist in
 * <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 */
public final class ActionSetsEvent {

	/**
	 * The array of action sets that are now active. This value may be
	 * <code>null</code>.
	 */
	private final IActionSetDescriptor[] newActionSets;

	/**
	 * Constructs a new instance of {@link ActionSetsEvent}.
	 * 
	 * @param newActionSets
	 *            The action sets that are now active; may be <code>null</code>.
	 */
	ActionSetsEvent(final IActionSetDescriptor[] newActionSets) {
		this.newActionSets = newActionSets;
	}

	/**
	 * Returns the currently active action sets.
	 * 
	 * @return The action sets that are now active; may be <code>null</code>.
	 */
	public final IActionSetDescriptor[] getNewActionSets() {
		return newActionSets;
	}
}

