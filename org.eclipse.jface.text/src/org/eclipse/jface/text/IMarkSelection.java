/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.jface.viewers.ISelection;


/**
 * A mark selection. Can be returned by text viewers implementing the
 * <code>IMarkRegionTarget</code> interface.
 * 
 * @since 2.0
 */
public interface IMarkSelection extends ISelection {

	/**
	 * Returns the marked document.
	 * 
	 * @return the marked document
	 */
	IDocument getDocument();
	
	/**
	 * Returns the mark position. The offset may be <code>-1</code> if there's no marked region.
	 * 
	 * @return the mark position or <code>-1</code> if there is no marked region
	 */
	int getOffset();
	
	/**
	 * Returns the length of the mark selection. The length may be negative, if the caret
	 * is before the mark position. The length has no meaning if <code>getOffset()</code> 
	 * returns <code>-1</code>.
	 * 
	 * @return the length of the mark selection. Result is undefined for <code>getOffset == -1</code>
	 */
	int getLength();
}
