/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.IInformationControlCreator;

/**
 * Extension interface for {@link org.eclipse.jface.text.source.IAnnotationHover} for
 * <ul>
 * <li>providing its own information control creator</li>
 * <li>providing the range of lines for which the hover for a given line is valid</li>
 * <li>providing whether the information control can interact with the mouse cursor</li>
 * </ul>
 *
 * @see org.eclipse.jface.text.IInformationControlCreator
 * @see org.eclipse.jface.text.source.IAnnotationHover
 * @since 3.0
 */
public interface IAnnotationHoverExtension {

	/**
	 * Returns the hover control creator of this annotation hover.
	 *
	 * @return the hover control creator
	 */
	IInformationControlCreator getHoverControlCreator();

	/**
	 * Returns whether the provided information control can interact with the mouse cursor. I.e. the
	 * hover must implement custom information control management.
	 *
	 * @return <code>true</code> if the mouse cursor can be handled
	 */
	boolean canHandleMouseCursor();

	/**
	 * Returns the object which should be presented in the a
	 * hover popup window. The information is requested based on
	 * the specified line range.
	 *
	 * @param sourceViewer the source viewer this hover is registered with
	 * @param lineRange the line range for which information is requested
	 * @param visibleNumberOfLines the number of visible lines
	 * @return the requested information or <code>null</code> if no such information exists
	 */
	Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines);

	/**
	 * Returns the range of lines that include the given line number for which
	 * the same hover information is valid.
	 *
	 * @param viewer the viewer which the hover is queried for
	 * @param lineNumber the line number of the line for which a hover is displayed for
	 * @return the computed line range or <code>null</code> for no range
	 */
	ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber);
}
