/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Set;

/**
 * @since 1.1
 * 
 */
public abstract class MapDiff {

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
