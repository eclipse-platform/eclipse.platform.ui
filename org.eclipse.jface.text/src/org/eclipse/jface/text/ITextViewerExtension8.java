/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.swt.custom.StyledTextPrintOptions;


/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}. Adds the
 * ability to print and set whether to allow moving the mouse into a hover.
 * 
 * @since 3.4
 */
public interface ITextViewerExtension8 {

	/**
	 * Print the text viewer contents using the given options.
	 * 
	 * @param options the print options
	 */
	void print(StyledTextPrintOptions options);
	
	/**
	 * Sets whether this viewer allows to move
	 * the mouse into a hover i.e. don't close
	 * it automatically.
	 * 
	 * @param state <code>true</code> to enable, <code>false</code> otherwise 
	 */
	void setAllowMoveIntoHover(boolean state);

}
