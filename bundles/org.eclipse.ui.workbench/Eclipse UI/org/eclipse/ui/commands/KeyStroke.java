/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands;

import java.util.Collections;
import java.util.Set;

import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class KeyStroke {

	/**
	 * JAVADOC
	 * 
	 * @param modifierKeys
	 * @param nonModifierKey
	 * @return
	 */		
	public static KeyStroke create(Set modifierKeys, NonModifierKey nonModifierKey) {
		return new KeyStroke(modifierKeys, nonModifierKey);
	}

	private Set modifierKeys;
	private NonModifierKey nonModifierKey;
	
	private KeyStroke(Set modifierKeys, NonModifierKey nonModifierKey) {
		super();
		if (nonModifierKey == null)
			throw new IllegalArgumentException();

		this.modifierKeys = Util.safeCopy(modifierKeys, ModifierKey.class);
		this.nonModifierKey = nonModifierKey;		
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public Set getModifierKeys() {
		return Collections.unmodifiableSet(modifierKeys);
	}

	/**
	 * JAVADOC
	 * 
	 * @return
	 */
	public NonModifierKey getNonModifierKey() {
		return nonModifierKey;
	}
}
