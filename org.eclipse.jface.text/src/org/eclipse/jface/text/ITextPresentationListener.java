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

/**
 * Text presentation listeners registered with a text viewer are informed 
 * when a text region is about to be drawn in order to get the text
 * presentation information.
 * 
 * @since 3.0
 */
public interface ITextPresentationListener {
	
	/**
	 * This method is called when a region is about to be
	 * drawn in order to get the text presentation information.
	 * Even though the given text presentation may cover a wider
	 * region than the given one clients should not modify text
	 * presentation outside the given region since this might be
	 * ignored.
	 *
	 * @param textPresentation the current text presentation	
	 */
	public void applyTextPresentation(TextPresentation textPresentation);
}
