/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.themes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @since 3.0
 */
public class CascadingMap {

	private Map base, override;

	/**
	 * @param base     the base (default) map
	 * @param override the override map
	 */
	public CascadingMap(Map base, Map override) {
		this.base = base;
		this.override = override;
	}

	/**
	 * Return the union of the parent and child key sets.
	 *
	 * @return the union. This set is read only.
	 */
	public Set keySet() {
		Set keySet = new HashSet(base.keySet());
		keySet.addAll(override.keySet());
		return Collections.unmodifiableSet(keySet);
	}

	/**
	 * Get the value. Preference will be given to entries in the override map.
	 *
	 * @param key the key
	 * @return the value
	 */
	public Object get(Object key) {
		if (override.containsKey(key)) {
			return override.get(key);
		}
		return base.get(key);
	}
}
