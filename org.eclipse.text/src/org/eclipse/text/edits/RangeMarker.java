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
package org.eclipse.text.edits;

import org.eclipse.jface.text.IDocument;

/**
 * A range marker can be used to track positions when executing 
 * text edits.
 * 
 * @since 3.0
 */
public final class RangeMarker extends TextEdit {
	
	/**
	 * Creates a new range marker for the given offset and length.
	 * 
	 * @param offset the marker's offset
	 * @param length the marker's length
	 */
	public RangeMarker(int offset, int length) {
		super(offset, length);
	}
	
	/*
	 * Copy constructor
	 */
	private RangeMarker(RangeMarker other) {
		super(other);
	}

	/* non Java-doc
	 * @see TextEdit#perform
	 */	
	/* package */ final void perform(IDocument document)  {
		// do nothing
	}
	
	/* non Java-doc
	 * @see TextEdit#copy
	 */	
	protected TextEdit doCopy() {
		return new RangeMarker(this);
	}	
}
