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

/**
 * </p> 
 * Instances of <code>NaturalKey</code> represent all keys on the keyboard not 
 * known by convention as 'modifier keys'.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public abstract class NaturalKey extends Key {

	/**
	 * Constructs an instance of <code>NaturalKey</code> given a name.
	 * 
	 * @param name The name of the key, must not be null.
	 */	
	NaturalKey(String name) {
		super(name);
	}
}
