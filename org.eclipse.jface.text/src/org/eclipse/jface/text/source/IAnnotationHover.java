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
package org.eclipse.jface.text.source;




/**
 * Provides the information to be displayed in a hover popup window
 * which appears over the presentation area of annotations. Clients
 * may implement this interface.
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
