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
package org.eclipse.jface.text.source;

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.text.IInformationControlCreator;

/**
 * Extension to <code>IAnnotationHover</code> for
 * <ul>
 * <li>providing its own information control creator</li>
 * <li>providing the range of lines for which the hover for a given line is valid
 * </ul>
 * 
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.source.IAnnotationHover
 * @since 3.0
 */
public interface IAnnotationHoverExtension {

	/**
	 * Returns the information control creator of this annotation hover.
	 * 
	 * @return the information control creator
	 */
	IInformationControlCreator getInformationControlCreator();
	
	/**
	 * Returns the range of lines that are covered by this hover for the given
	 * <code>ISourceViewer</code> at model line <code>line</code>.
	 * 
	 * @param viewer the viewer which the hover is queried for
	 * @param line the line which a hover is displayed for
	 * @return the inclusive range of lines containing the given line in which a hover computer for
	 * 	the given line is valid.
	 */
	Point getLineRange(ISourceViewer viewer, int line);
}