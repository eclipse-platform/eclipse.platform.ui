/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;



/**
 * Provides the information to be displayed in a hover popup window which
 * appears over the presentation area of annotations.
 * <p>
 * In order to provide backward compatibility for clients of
 * <code>IAnnotationHover</code>, extension interfaces are used as a means of
 * evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.source.IAnnotationHoverExtension} since
 *     version 3.0 allowing a text hover to provide a creator for the hover control.
 *     This allows for sophisticated hovers in a way that information computed by
 *     the hover can be displayed in the best possible form.</li>
 * <li>{@link org.eclipse.jface.text.source.IAnnotationHoverExtension2} since
 *     version 3.2 allowing a text hover to specify whether it handles mouse-wheel
 *     events itself.</li>
 * </ul></p>
 * <p>
 * Clients may implement this interface.</p>
 *
 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension
 * @see org.eclipse.jface.text.source.IAnnotationHoverExtension2
 */
public interface IAnnotationHover {

	/**
	 * Returns the text which should be presented in the a
	 * hover popup window. This information is requested based on
	 * the specified line number.
	 *
	 * @param sourceViewer the source viewer this hover is registered with
	 * @param lineNumber the line number for which information is requested
	 * @return the requested information or <code>null</code> if no such information exists
	 */
	String getHoverInfo(ISourceViewer sourceViewer, int lineNumber);
}
