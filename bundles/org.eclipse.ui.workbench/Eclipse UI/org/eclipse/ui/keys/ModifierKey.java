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

import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.swt.SWT;

/**
 * <p>
 * Instances of <code>ModifierKey</code> represent the four keys on the
 * keyboard recognized by convention as 'modifier keys', those keys typically
 * pressed in combination with themselves and/or a
 * {@link org.eclipse.ui.keys.NaturalKey}.
 * </p>
 * <p>
 * <code>ModifierKey</code> objects are immutable. Clients are not permitted
 * to extend this class.
 * </p>
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyStroke and
 *             org.eclipse.jface.bindings.keys.KeyLookupFactory
 * @since 3.0
 * @see org.eclipse.ui.keys.NaturalKey
 */
public final class ModifierKey extends Key {

	/**
	 * An internal map used to lookup instances of <code>ModifierKey</code>
	 * given the formal string representation of a modifier key.
	 */
	static SortedMap modifierKeysByName = new TreeMap();

	/**
	 * The single static instance of <code>ModifierKey</code> which represents
	 * the 'Alt' key.
	 */
	public final static ModifierKey ALT;

	/**
	 * The single static instance of <code>ModifierKey</code> which represents
	 * the 'Command' key.
	 */
	public final static ModifierKey COMMAND;

	/**
	 * The single static instance of <code>ModifierKey</code> which represents
	 * the 'Ctrl' key.
	 */
	public final static ModifierKey CTRL;

	/**
	 * The name of the 'M1' key.
	 */
	private final static String M1_NAME = "M1"; //$NON-NLS-1$	

	/**
	 * The name of the 'M2' key.
	 */
	private final static String M2_NAME = "M2"; //$NON-NLS-1$

	/**
	 * The name of the 'M3' key.
	 */
	private final static String M3_NAME = "M3"; //$NON-NLS-1$

	/**
	 * The name of the 'M4' key.
	 */
	private final static String M4_NAME = "M4"; //$NON-NLS-1$	

	/**
	 * The single static instance of <code>ModifierKey</code> which represents
	 * the 'Shift' key.
	 */
	public final static ModifierKey SHIFT;

	static {
		final IKeyLookup lookup = KeyLookupFactory.getDefault();
		ALT = new ModifierKey(lookup.getAlt());
		COMMAND = new ModifierKey(lookup.getCommand());
		CTRL = new ModifierKey(lookup.getCtrl());
		SHIFT = new ModifierKey(lookup.getShift());
		
		modifierKeysByName.put(ModifierKey.ALT.toString(), ModifierKey.ALT);
		modifierKeysByName.put(ModifierKey.COMMAND.toString(),
				ModifierKey.COMMAND);
		modifierKeysByName.put(ModifierKey.CTRL.toString(), ModifierKey.CTRL);
		modifierKeysByName.put(ModifierKey.SHIFT.toString(), ModifierKey.SHIFT);
		modifierKeysByName
				.put(
						M1_NAME,
						"carbon".equals(SWT.getPlatform()) ? ModifierKey.COMMAND : ModifierKey.CTRL); //$NON-NLS-1$
		modifierKeysByName.put(M2_NAME, ModifierKey.SHIFT);
		modifierKeysByName.put(M3_NAME, ModifierKey.ALT);
		modifierKeysByName
				.put(
						M4_NAME,
						"carbon".equals(SWT.getPlatform()) ? ModifierKey.CTRL : ModifierKey.COMMAND); //$NON-NLS-1$
	}

	/**
	 * Constructs an instance of <code>ModifierKey</code> given a name.
	 * 
	 * @param key
	 *            The key which this key wraps.
	 */
	private ModifierKey(final int key) {
		super(key);
	}
}
