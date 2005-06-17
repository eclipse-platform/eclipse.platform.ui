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


import org.eclipse.swt.graphics.Font;


/**
 * Extension interface for {@link IVerticalRuler}.
 * <p>
 * Allows to set the font of the vertical ruler and to set the location of the
 * last mouse button activity.
 *
 * @since 2.0
 */
public interface IVerticalRulerExtension {

	/**
	 * Sets the font of this vertical ruler.
	 *
	 * @param font the new font of the vertical ruler
	 */
	void setFont(Font font);

	/**
	 * Sets the location of the last mouse button activity. This method is used for
	 * example by external mouse listeners.
	 *
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 */
	void setLocationOfLastMouseButtonActivity(int x, int y);
}
