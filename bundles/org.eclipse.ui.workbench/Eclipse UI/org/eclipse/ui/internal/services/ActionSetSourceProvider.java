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
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.internal.ActionSetsEvent;
import org.eclipse.ui.internal.menus.IActionSetsListener;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

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
public final class ActionSetSourceProvider extends AbstractSourceProvider implements IActionSetsListener {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ISources.ACTIVE_ACTION_SETS_NAME };

	/**
	 * The action sets last seen as active by this source provider. This value may
	 * be <code>null</code>.
	 */
	private IActionSetDescriptor[] activeActionSets;

	public ActionSetSourceProvider() {
		super();
	}

	@Override
	public void actionSetsChanged(final ActionSetsEvent event) {
		final IActionSetDescriptor[] newActionSets = event.getNewActionSets();
		if (!Arrays.equals(newActionSets, activeActionSets)) {
			if (DEBUG) {
				final StringBuilder message = new StringBuilder();
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
			fireSourceChanged(ISources.ACTIVE_ACTION_SETS, ISources.ACTIVE_ACTION_SETS_NAME, activeActionSets);

		}
	}

	@Override
	public void dispose() {
		activeActionSets = null;
	}

	@Override
	public Map getCurrentState() {
		final Map currentState = new HashMap();
		currentState.put(ISources.ACTIVE_ACTION_SETS_NAME, activeActionSets);
		return currentState;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}
}