/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelection;

/**
 * A mark selection.
 */
public interface IMarkSelection extends ISelection {

	/**
	 * Returns the marked document.
	 */
	IDocument getDocument();
	
	/**
	 * The mark position. The offset may be <code>-1</code> if there's no marked region.
	 */
	int getOffset();
	
	/**
	 * The length of the mark selection. The length may be negative, if the caret
	 * is before the mark position. The length has no meaning if getOffset() returns <code>-1</code>.
	 */
	int getLength();

}
