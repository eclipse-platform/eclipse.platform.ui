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

import java.util.ResourceBundle;

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
/*public*/ final class GestureKey extends NaturalKey {

	private final static String EAST_NAME = "EAST"; //$NON-NLS-1$
	private final static String NORTH_NAME = "NORTH"; //$NON-NLS-1$
	private final static String SOUTH_NAME = "SOUTH"; //$NON-NLS-1$
	private final static String WEST_NAME = "WEST"; //$NON-NLS-1$	
	
	/*public*/ final static GestureKey EAST = new GestureKey(EAST_NAME); 
	/*public*/ final static GestureKey NORTH = new GestureKey(NORTH_NAME);
	/*public*/ final static GestureKey SOUTH = new GestureKey(SOUTH_NAME); 
	/*public*/ final static GestureKey WEST = new GestureKey(WEST_NAME); 
	
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(GestureKey.class.getName());
	
	private GestureKey(String name) {
		super(name);
	}

	public String format() {		
		return Util.translateString(RESOURCE_BUNDLE, name, name, false, false);
	}
}
