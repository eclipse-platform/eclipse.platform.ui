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
package org.eclipse.jface.text;

import org.eclipse.jface.text.Position;

/**
 * 
 * This class is for internal use only.
 * @since 2.1
 */
public class Fragment extends Position {
		
	private Position fOrigin;

	public Fragment(int offset, int length, Position origin) {
		super(offset, length);
		fOrigin= origin;
	}
	
	/**
	 * Returns the fOrigin.
	 * @return Position
	 */
	public Position getOrigin() {
		return fOrigin;
	}
	
	public void setOrigin(Position origin) {
		fOrigin= origin;
	}
}
