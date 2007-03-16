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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * <p>
 * A listener to changes in the showing menus. This is used to activate handlers
 * just for the span of a menu being shown. This is needed for full support for
 * legacy action-based extension points.
 * </p>
 * <p>
 * This class is only intended for internal use within
 * <code>org.eclipse.ui.workbench</code>.
 * </p>
 * 
 * @since 3.2
 */
public final class MenuSourceProvider extends AbstractSourceProvider {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] {
			ISources.ACTIVE_MENU_NAME, ISources.ACTIVE_MENU_SELECTION_NAME,
			ISources.ACTIVE_MENU_EDITOR_INPUT_NAME };

	/**
	 * The menu ids that are currently showing, as known by this source
	 * provider. This value may be <code>null</code>.
	 */
	private Set menuIds = new HashSet();

	/**
	 * Adds all of the given menu identifiers as being shown.
	 * 
	 * @param menuIds
	 *            The ids of the menu that is now showing; must not be
	 *            <code>null</code>.
	 */
	public final void addShowingMenus(final Set menuIds,
			final ISelection localSelection, final ISelection localEditorInput) {
		this.menuIds.addAll(menuIds);
		if (DEBUG) {
			logDebuggingInfo("Menu ids changed to " + this.menuIds); //$NON-NLS-1$
		}
		Map m = new HashMap();
		m.put(ISources.ACTIVE_MENU_NAME, this.menuIds);
		if (selection != localSelection) {
			selection = localSelection;
			m.put(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
		}
		if (input != localEditorInput) {
			input = localEditorInput;
			m.put(ISources.ACTIVE_MENU_EDITOR_INPUT_NAME, input);
		}

		fireSourceChanged(ISources.ACTIVE_MENU, m);
	}

	public final void dispose() {
		menuIds.clear();
		selection = null;
		input = null;
	}

	public final Map getCurrentState() {
		final Map state = new HashMap();
		state.put(ISources.ACTIVE_MENU_NAME, menuIds);
		state.put(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
		state.put(ISources.ACTIVE_MENU_EDITOR_INPUT_NAME, input);
		return state;
	}

	public final String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	/**
	 * Removes all of the given menu identifiers as being shown.
	 * 
	 * @param menuIds
	 *            The ids of the menu that is no longer shown; must not be
	 *            <code>null</code>.
	 */
	public final void removeShowingMenus(final Set menuIds,
			final ISelection localSelection, final ISelection localEditorInput) {
		this.menuIds.removeAll(menuIds);
		if (DEBUG) {
			logDebuggingInfo("Menu ids changed to " + this.menuIds); //$NON-NLS-1$
		}
		Map m = new HashMap();
		m.put(ISources.ACTIVE_MENU_NAME, this.menuIds);
		if (selection != localSelection) {
			selection = localSelection;
			m.put(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
		}
		if (input != localEditorInput) {
			input = localEditorInput;
			m.put(ISources.ACTIVE_MENU_EDITOR_INPUT_NAME, input);
		}
		fireSourceChanged(ISources.ACTIVE_MENU, m);
	}

	private ISelection selection = null;
	private ISelection input = null;
}
