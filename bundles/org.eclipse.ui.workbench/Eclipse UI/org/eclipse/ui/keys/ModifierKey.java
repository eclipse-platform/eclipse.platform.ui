/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.keys;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * Instances of <code>ModifierKey</code> represent the four keys on the keyboard 
 * recognized by convention as 'modifier keys', those keys typically pressed in 
 * combination with themselves and/or a 'natural key'.
 * </p>
 * <p>
 * <code>ModifierKey</code> objects are immutable. Clients are not permitted to 
 * extend this class.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class ModifierKey extends Key {
	
	/**
	 * The name of the 'Alt' key.
	 */
	private final static String ALT_NAME = "ALT"; //$NON-NLS-1$
	
	/**
	 * The name of the 'Command' key.
	 */
	private final static String COMMAND_NAME = "COMMAND"; //$NON-NLS-1$
	
	/**
	 * The name of the 'Ctrl' key.
	 */
	private final static String CTRL_NAME = "CTRL"; //$NON-NLS-1$
	
	/**
	 * The name of the 'Shift' key.
	 */
	private final static String SHIFT_NAME = "SHIFT"; //$NON-NLS-1$	
	
	/**
	 * The single static instance of <code>ModifierKey</code> which represents 
	 * the 'Alt' key.
	 */
	public final static ModifierKey ALT = new ModifierKey(ALT_NAME); 
    
	/**
	 * The single static instance of <code>ModifierKey</code> which represents 
	 * the 'Command' key.
	 */
    public final static ModifierKey COMMAND = new ModifierKey(COMMAND_NAME);
	
	/**
	 * The single static instance of <code>ModifierKey</code> which represents 
	 * the 'Ctrl' key.
	 */
	public final static ModifierKey CTRL = new ModifierKey(CTRL_NAME); 
	
	/**
	 * The single static instance of <code>ModifierKey</code> which represents 
	 * the 'Shift' key.
	 */
	public final static ModifierKey SHIFT = new ModifierKey(SHIFT_NAME); 
	
	/**
	 * The resource bundle used by <code>format()</code> to translate key names
	 * by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(ModifierKey.class.getName());		

	/**
	 * Constructs an instance of <code>ModifierKey</code> given a name.
	 * 
	 * @param name The name of the key, must not be null.
	 */	
	private ModifierKey(String name) {
		super(name);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.keys.Key#format()
	 */
	public String format() {
		// TODO consider platform-specific resource bundles.
		if ("carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$    	
			if (ALT_NAME.equals(name))
				return Character.toString('\u2325');
			
			if (COMMAND_NAME.equals(name))
				return Character.toString('\u2318');
			
			if (CTRL_NAME.equals(name))
				return Character.toString('\u2303');
			
			if (SHIFT_NAME.equals(name))
				return Character.toString('\u21E7');
		}
		
		return Util.translateString(RESOURCE_BUNDLE, name, name, false, false);
	}
}
