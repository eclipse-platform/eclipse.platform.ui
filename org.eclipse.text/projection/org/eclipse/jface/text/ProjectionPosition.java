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
 * ProjectionPosition.java
 */
public class ProjectionPosition extends Position {
	
	private IDocument fProjectionDocument;
	private Fragment fFragment;
	
	public ProjectionPosition(IDocument projectionDocument, int offset, int length) {
		super(offset, length);
		fProjectionDocument= projectionDocument;
	}
	
	public void setFragment(Fragment fragment) {
		fFragment= fragment;
	}
	
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
