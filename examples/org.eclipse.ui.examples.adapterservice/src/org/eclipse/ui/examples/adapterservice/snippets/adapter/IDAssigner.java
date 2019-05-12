/*******************************************************************************
 * Copyright (c) 2014 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.adapterservice.snippets.adapter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;

public class IDAssigner implements IAdapterFactory {
	int currentId;
	Map<Object, String> assignedIds = new HashMap<Object, String>(); // Object->its
																		// id

	public IDAssigner() {
		currentId = 1000;
	}
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Object adaptableObject, Class adapterType) {
		if (adapterType.equals(ThingWithId.class)) {
			if (!assignedIds.containsKey(adaptableObject)) {
				String id = Integer.toString(currentId);
				currentId++;
				assignedIds.put(adaptableObject, id);
			}
			return (ThingWithId) () -> assignedIds.get(adaptableObject);
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { ThingWithId.class };
	}

}