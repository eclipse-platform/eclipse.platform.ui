/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

import java.util.SortedMap;
import java.util.TreeMap;

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
     * The name of the 'Alt' key.
     */
    private final static String ALT_NAME = "ALT"; //$NON-NLS-1$

    /**
     * The single static instance of <code>ModifierKey</code> which
     * represents the 'Alt' key.
     */
    public final static ModifierKey ALT = new ModifierKey(ALT_NAME);

    /**
     * The name of the 'Command' key.
     */
    private final static String COMMAND_NAME = "COMMAND"; //$NON-NLS-1$

    /**
     * The single static instance of <code>ModifierKey</code> which
     * represents the 'Command' key.
     */
    public final static ModifierKey COMMAND = new ModifierKey(COMMAND_NAME);

    /**
     * The name of the 'Ctrl' key.
     */
    private final static String CTRL_NAME = "CTRL"; //$NON-NLS-1$

    /**
     * The single static instance of <code>ModifierKey</code> which
     * represents the 'Ctrl' key.
     */
    public final static ModifierKey CTRL = new ModifierKey(CTRL_NAME);

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
     * The name of the 'Shift' key.
     */
    private final static String SHIFT_NAME = "SHIFT"; //$NON-NLS-1$	

    /**
     * The single static instance of <code>ModifierKey</code> which
     * represents the 'Shift' key.
     */
    public final static ModifierKey SHIFT = new ModifierKey(SHIFT_NAME);

    static {
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
     * @param name
     *            The name of the key, must not be null.
     */
    private ModifierKey(String name) {
        super(name);
    }
}