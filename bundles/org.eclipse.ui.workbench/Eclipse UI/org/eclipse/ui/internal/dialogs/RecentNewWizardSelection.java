/*******************************************************************************
 * Copyright (c) 2023 ETAS GmbH and others, all rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ETAS GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A <code>RecentNewWizardSelection</code> is used to manage the five most
 * recent new wizards used or created from the "Other" shortcut, which is part
 * of the menu manager with New Wizard actions. The recently used new wizards
 * will appear before the "Other" shortcut.
 */
public class RecentNewWizardSelection {
	private Set<String> menuIds = new LinkedHashSet<>();
	private static RecentNewWizardSelection instance;
	private static final int MAX_MENU_SIZE = 5;

	public static RecentNewWizardSelection getInstance() {
		synchronized (RecentNewWizardSelection.class) {
			if (instance == null) {
				instance = new RecentNewWizardSelection();
			}
			return instance;
		}
	}

	/**
	 * Adds the new wizard menu shortcut ID to the set and removes the oldest one if
	 * the number of recently used new wizard menu shortcuts exceeds MAX_MENU_SIZE.
	 *
	 * @param shortcut the new wizard menu shortcut ID
	 */
	public void addItem(String shortcut) {
		menuIds.add(shortcut);
		if (menuIds.size() > MAX_MENU_SIZE) {
			Iterator<String> iterator = menuIds.iterator();
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * Returns the set of recently used new wizard menu shortcut IDs.
	 *
	 * @return the set of recently used new wizard menu shortcut IDs
	 */

	public Set<String> getMenuIds() {
		return Collections.unmodifiableSet(menuIds);
	}

}
