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

/**
 * Represents the corresponding parent document range of a fragment of a <code>ProjectionDocument</code>.<p>
 * This calss is for internal use only.
 * 
 * @since 2.1
 */
public class ProjectionPosition extends Position {
	
	/** The projection document. */
	private IDocument fProjectionDocument;
	/** The corresponding fragment. */
	private Fragment fFragment;
	
	/**
	 * Creates a new position representing the corresponding range of a fragment.
	 * 
	 * @param projectionDocument the projection document
	 * @param offset the offset of the range of the parent document
	 * @param length the length of the range of the parent document
	 */
	public ProjectionPosition(IDocument projectionDocument, int offset, int length) {
		super(offset, length);
		fProjectionDocument= projectionDocument;
	}
	
	/**
	 * Sets the corresponding fragment.
	 * 
	 * @param fragment the corresponding fragment
	 */
	public void setFragment(Fragment fragment) {
		fFragment= fragment;
	}
	
	/**
	 * Returns the corresponding fragment.
	 * @return the corresponding fragment
	 */
	public Fragment getFragment() {
		return fFragment;
	}
	
//	/**
//	 * Changed to be compatible to the position updater behavior
//	 * @see Position#overlapsWith(int, int)
//	 */
//	public boolean overlapsWith(int offset, int length) {
//		boolean append= (offset == this.offset + this.length) && length == 0;
//		return append || super.overlapsWith(offset, length);
//	}
}
