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

package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0
 *
 */
public class RenamableItem {

	public static interface Listener {
		public void handleChanged(RenamableItem item);
	}

	private String name;
	private List<Listener> listeners = new ArrayList<>();

	public RenamableItem() {
		name = "RenamableItem"; //$NON-NLS-1$
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener toRemove) {
		listeners.remove(toRemove);
	}

	public void setName(String newName) {
		this.name = newName;

		for (Listener listener : listeners) {
			listener.handleChanged(this);
		}
	}

	public String getName() {
		return name;
	}
}
