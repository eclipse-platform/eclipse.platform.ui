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
 * Represents a fragment of a <code>ProjectionDocument</code>.<p>
 * This class is for internal use only.
 * 
 * @since 2.1
 */
public class Fragment extends Position {
	
	/** Position representing the corresponding range in the parent document. */
	private Position fOrigin;
	
	/**
	 * Creates new position representing a fragment.
	 * 
	 * @param offset the offset of the fragment
	 * @param length the length of the fragment
	 * @param origin the cooresponding range in the parent document
	 */
	public Fragment(int offset, int length, Position origin) {
		super(offset, length);
		fOrigin= origin;
	}
	
	/**
	 * Returns the corresponding range in the parent document.
	 * @return the corresponding range in the parent document
	 */
	public Position getOrigin() {
		return fOrigin;
	}
	
	/**
	 * Sets the corresponding range in the parent document.
	 * @param origin the cooresponding range in the parent document
	 */
	public void setOrigin(Position origin) {
		fOrigin= origin;
	}
}
