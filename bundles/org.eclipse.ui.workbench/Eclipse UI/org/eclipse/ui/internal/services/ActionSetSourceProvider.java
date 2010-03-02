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

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.ActionSetsEvent;
import org.eclipse.ui.internal.menus.IActionSetsListener;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * A listener to changes in the action sets.
 * </p>
 * <p>
 * This class is only intended for internal use within
 * <code>org.eclipse.ui.workbench</code>.
 * </p>
 * 
 * @since 3.2
 */
public final class ActionSetSourceProvider extends AbstractSourceProvider
		implements IActionSetsListener {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ISources.ACTIVE_ACTION_SETS_NAME };

	/**
	 * The action sets last seen as active by this source provider. This value
	 * may be <code>null</code>.
	 */
	private IActionSetDescriptor[] activeActionSets;

	public ActionSetSourceProvider() {
		super();
	}

	public final void actionSetsChanged(final ActionSetsEvent event) {
		final IActionSetDescriptor[] newActionSets = event.getNewActionSets();
		if (!Util.equals(newActionSets, activeActionSets)) {
			if (DEBUG) {
				final StringBuffer message = new StringBuffer();
				message.append("Action sets changed to ["); //$NON-NLS-1$
				if (newActionSets != null) {
					for (int i = 0; i < newActionSets.length; i++) {
						message.append(newActionSets[i].getLabel());
						if (i < newActionSets.length - 1) {
							message.append(", "); //$NON-NLS-1$
						}
					}
				}
				message.append(']');
				logDebuggingInfo(message.toString());
			}

			activeActionSets = newActionSets;
			fireSourceChanged(ISources.ACTIVE_ACTION_SETS,
					ISources.ACTIVE_ACTION_SETS_NAME, activeActionSets);

		}
	}

	public final void dispose() {
		activeActionSets = null;
	}

	public final Map getCurrentState() {
		final Map currentState = new HashMap();
		currentState.put(ISources.ACTIVE_ACTION_SETS_NAME, activeActionSets);
		return currentState;
	}

	public final String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
}