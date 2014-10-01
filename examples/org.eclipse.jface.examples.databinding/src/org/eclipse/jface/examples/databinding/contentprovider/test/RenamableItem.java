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

package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.ArrayList;

/**
 * @since 1.0
 *
 */
public class RenamableItem {

	public static interface Listener {
		public void handleChanged(RenamableItem item);
	}

	private String name;
	private ArrayList listeners = new ArrayList();

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

		Listener[] l = (Listener[]) listeners.toArray(new Listener[listeners.size()]);
		for (int i = 0; i < l.length; i++) {
			l[i].handleChanged(this);
		}
	}

	public String getName() {
		return name;
	}
}
