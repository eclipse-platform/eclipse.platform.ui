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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @since 3.5
 *
 */
public class DynamicMenuSelection {
	private Set<String> selectedFromOther = new LinkedHashSet<>();
	private static DynamicMenuSelection instance;
	private static final int MAX_MENU_SIZE = 5;

	public static DynamicMenuSelection getInstance() {
		synchronized (DynamicMenuSelection.class) {
			if (instance == null) {
				instance = new DynamicMenuSelection();
			}
			return instance;
		}
	}

	public void addItems(String shortcuts) {
		selectedFromOther.add(shortcuts);
		if (selectedFromOther.size() > MAX_MENU_SIZE) {
			Iterator<String> iterator = selectedFromOther.iterator();
			iterator.next();
			iterator.remove();
		}
	}

	/**
	 * @return Returns the selectedFromOther.
	 */
	public Set<String> getSelectedFromOther() {
		return selectedFromOther;
	}

}
