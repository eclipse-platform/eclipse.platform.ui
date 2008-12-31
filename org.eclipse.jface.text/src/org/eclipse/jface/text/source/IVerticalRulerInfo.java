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


import org.eclipse.swt.widgets.Control;


/**
 * A vertical ruler is a visual component which may serve text viewers as an
 * annotation presentation area. The vertical ruler info provides interested
 * clients with the mapping and interaction aspect of the vertical ruler. This
 * covers the mapping between coordinates of the ruler's control and line
 * numbers based on the connected text viewer's document.
 *
 * In order to provide backward compatibility for clients of
 * <code>IVerticalRulerInfo</code>, extension interfaces are used as a means
 * of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.source.IVerticalRulerInfoExtension} since
 * version 3.0 allowing custom annotation hovers and specific annotation models.
 * </li>
 * </ul>
 *
 * @see org.eclipse.jface.text.source.IVerticalRulerInfoExtension
 * @since 2.0
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
	 * @return the line number of the last mouse button activity or <code>-1</code> if
	 * 			the last mouse activity does not correspond to a valid document line
	 */
	int getLineOfLastMouseButtonActivity();

	/**
	 * Translates a y-coordinate of the ruler's SWT control into
	 * the according line number of the document of the connected text viewer.
	 *
	 * @param y_coordinate a y-coordinate of the ruler's SWT control
	 * @return the line number of that coordinate or <code>-1</code> if that
	 * 			coordinate does not correspond to a valid document line
	 */
	int toDocumentLineNumber(int y_coordinate);

	/**
	 * Returns the width of this ruler's control.
	 *
	 * @return the width of this ruler's control
	 */
	int getWidth();
}
