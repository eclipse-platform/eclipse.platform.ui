/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.keys;

import java.util.SortedMap;
import java.util.TreeMap;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * Instances of <code>ModifierKey</code> represent the four keys on the keyboard
 * recognized by convention as 'modifier keys', those keys typically pressed in
 * combination with themselves and/or a {@link org.eclipse.ui.keys.NaturalKey}.
 * </p>
 * <p>
 * <code>ModifierKey</code> objects are immutable. Clients are not permitted to
 * extend this class.
 * </p>
 *
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyStroke and
 *             org.eclipse.jface.bindings.keys.KeyLookupFactory
 * @since 3.0
 * @see org.eclipse.ui.keys.NaturalKey
 */
@Deprecated
public final class ModifierKey extends Key {

	/**
	 * An internal map used to lookup instances of <code>ModifierKey</code> given
	 * the formal string representation of a modifier key.
	 */
	static SortedMap<String, ModifierKey> modifierKeysByName = new TreeMap<>();

	/**
	 * The single static instance of <code>ModifierKey</code> which represents the
	 * 'Alt' key.
	 */
	public static final ModifierKey ALT;

	/**
	 * The single static instance of <code>ModifierKey</code> which represents the
	 * 'Command' key.
	 */
	public static final ModifierKey COMMAND;

	/**
	 * The single static instance of <code>ModifierKey</code> which represents the
	 * 'Ctrl' key.
	 */
	public static final ModifierKey CTRL;

	/**
	 * The name of the 'M1' key.
	 */
	private static final String M1_NAME = "M1"; //$NON-NLS-1$

	/**
	 * The name of the 'M2' key.
	 */
	private static final String M2_NAME = "M2"; //$NON-NLS-1$

	/**
	 * The name of the 'M3' key.
	 */
	private static final String M3_NAME = "M3"; //$NON-NLS-1$

	/**
	 * The name of the 'M4' key.
	 */
	private static final String M4_NAME = "M4"; //$NON-NLS-1$

	/**
	 * The single static instance of <code>ModifierKey</code> which represents the
	 * 'Shift' key.
	 */
	public static final ModifierKey SHIFT;

	static {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		ALT = new ModifierKey(lookup.getAlt());
		COMMAND = new ModifierKey(lookup.getCommand());
		CTRL = new ModifierKey(lookup.getCtrl());
		SHIFT = new ModifierKey(lookup.getShift());

		modifierKeysByName.put(ModifierKey.ALT.toString(), ModifierKey.ALT);
		modifierKeysByName.put(ModifierKey.COMMAND.toString(), ModifierKey.COMMAND);
		modifierKeysByName.put(ModifierKey.CTRL.toString(), ModifierKey.CTRL);
		modifierKeysByName.put(ModifierKey.SHIFT.toString(), ModifierKey.SHIFT);
		modifierKeysByName.put(M1_NAME, Util.isMac() ? ModifierKey.COMMAND : ModifierKey.CTRL);
		modifierKeysByName.put(M2_NAME, ModifierKey.SHIFT);
		modifierKeysByName.put(M3_NAME, ModifierKey.ALT);
		modifierKeysByName.put(M4_NAME, Util.isMac() ? ModifierKey.CTRL : ModifierKey.COMMAND);
	}

	/**
	 * Constructs an instance of <code>ModifierKey</code> given a name.
	 *
	 * @param key The key which this key wraps.
	 */
	private ModifierKey(final int key) {
		super(key);
	}
}
