/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial API and implementation
**********************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.widgets.Control;

/**
 * The ruler info provides interested clients with mapping and
 * interaction information. This covers the mapping between
 * coordinates of the ruler's control and line numbers based 
 * on the connected text viewer's document.
 */
public interface IVerticalRulerInfo {

	/**
	 * Returns the ruler's SWT control.
	 *
	 * @return the ruler's SWT control
	 */
	Control getControl();

	/**
	 * Returns the line number of the last mouse button activity.
	 * Based on the input document of the connected text viewer.
	 *
	 * @return the line number of the last mouse button activity
	 */
	int getLineOfLastMouseButtonActivity();
	
	/**
	 * Translates a y-coordinate of the ruler's SWT control into
	 * the according line number of the document of the connected text viewer.
	 *
	 * @param y_coordinate a y-coordinate of the ruler's SWT control
	 * @return the line number of that coordinate 
	 */
	int toDocumentLineNumber(int y_coordinate);
	
	/**
	 * Returns the width of this ruler's control.
	 *
	 * @return the width of this ruler's control
	 */
	int getWidth();

}
