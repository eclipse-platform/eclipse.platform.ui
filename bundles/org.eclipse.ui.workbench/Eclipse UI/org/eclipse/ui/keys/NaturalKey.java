/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

/**
 * </p>
 * Instances of <code>NaturalKey</code> represent all keys on the keyboard not
 * known by convention as 'modifier keys'. These can either be keys that belong
 * to a natural language of some kind(e.g., "A", "1"), any Unicode character
 * (e.g., "backspace"), or they can be special controls keys used by computers
 * (e.g., "F10", "PageUp").
 * </p>
 * <p>
 * <code>NaturalKey</code> objects are immutable. Clients are not permitted to
 * extend this class.
 * </p>
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyStroke and
 *             org.eclipse.jface.bindings.keys.KeyLookupFactory
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class NaturalKey extends Key {

	/**
	 * Constructs an instance of <code>NaturalKey</code> given a name.
	 * 
	 * @param key
	 *            The key to be wrapped.
	 */
	NaturalKey(final int key) {
		super(key);
	}
}
