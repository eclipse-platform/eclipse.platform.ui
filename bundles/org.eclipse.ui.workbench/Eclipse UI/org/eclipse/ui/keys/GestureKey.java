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

package org.eclipse.ui.keys;


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
/*public*/ final class GestureKey extends NaturalKey {

	/*public*/ final static GestureKey EAST = new GestureKey("EAST"); 
	/*public*/ final static GestureKey NORTH = new GestureKey("NORTH"); 
	/*public*/ final static GestureKey SOUTH = new GestureKey("SOUTH"); 
	/*public*/ final static GestureKey WEST = new GestureKey("WEST"); 

	private GestureKey(String name) {
		super(name);
	}
}
