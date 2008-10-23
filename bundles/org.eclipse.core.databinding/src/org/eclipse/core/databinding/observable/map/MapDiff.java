/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 251884
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @since 1.1
 * 
 */
public abstract class MapDiff {
	/**
	 * Returns true if the diff has no added, removed or changed entries.
	 * 
	 * @return true if the diff has no added, removed or changed entries.
	 * @since 1.2
	 */
	public boolean isEmpty() {
		return getAddedKeys().isEmpty() && getRemovedKeys().isEmpty()
				&& getChangedKeys().isEmpty();
	}

	/**
	 * Applies the changes in this diff to the given map
	 * 
	 * @param map
	 *            the map to which the diff will be applied
	 * @since 1.2
	 */
	public void applyTo(Map map) {
		for (Iterator it = getAddedKeys().iterator(); it.hasNext();) {
			Object key = it.next();
			map.put(key, getNewValue(key));
		}
		for (Iterator it = getChangedKeys().iterator(); it.hasNext();) {
			Object key = it.next();
			map.put(key, getNewValue(key));
		}
		for (Iterator it = getRemovedKeys().iterator(); it.hasNext();) {
			map.remove(it.next());
		}
	}

	/**
	 * @return the set of keys which were added
	 */
	public abstract Set getAddedKeys();

	/**
	 * @return the set of keys which were removed
	 */
	public abstract Set getRemovedKeys();

	/**
	 * @return the set of keys for which the value has changed
	 */
	public abstract Set getChangedKeys();

	/**
	 * Returns the old value for the given key, which must be an element of
	 * {@link #getRemovedKeys()} or {@link #getChangedKeys()}.
	 * 
	 * @param key
	 * @return the old value for the given key.
	 */
	public abstract Object getOldValue(Object key);

	/**
	 * Returns the new value for the given key, which must be an element of
	 * {@link #getChangedKeys()} or {@link #getAddedKeys()}.
	 * 
	 * @param key
	 * @return the new value for the given key.
	 */
	public abstract Object getNewValue(Object key);
}
