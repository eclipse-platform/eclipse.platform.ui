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
package org.eclipse.jface.text;

import org.eclipse.swt.graphics.Rectangle;


/**
 * Extension interface for {@link org.eclipse.jface.text.IInformationControl}.
 * Adds API which allows to get this information control's bounds and introduces
 * the concept of persistent size and location by introducing predicates for
 * whether the information control supports restoring of size and location.
 * <p>
 * Note: An information control which implements this interface can ignore calls
 * to
 * {@link org.eclipse.jface.text.IInformationControl#setSizeConstraints(int, int)}
 * or use it as hint for its very first appearance.
 * </p>
 *
 * @see org.eclipse.jface.text.IInformationControl
 * @since 3.0
 */
public interface IInformationControlExtension3 {

	/**
	 * Returns a rectangle describing the receiver's size and location
	 * relative to its parent (or its display if its parent is null).
	 * <p>
	 * Note: If the receiver is already disposed then this methods must
	 * return the last valid location and size.
	 * </p>
	 *
	 * @return the receiver's bounding rectangle
	 */
	Rectangle getBounds();

	/**
	 * Computes the trim for this control.
	 * x and y denote the upper left corner of the trimming relative
	 * to this control's location i.e. this will most likely be
	 * negative values. Width and height represent the border sizes.
	 *
	 * @return the receivers trim
	 */
	Rectangle computeTrim();

	/**
	 * Tells whether this control allows to restore the previously
	 * used size.
	 * <p>
	 * Note: This is not a static property - it can change during the
	 * lifetime of this control.</p>
	 *
	 * @return <code>true</code> if restoring size is supported
	 */
	boolean restoresSize();

	/**
	 * Tells whether this control allows to restore the previously
	 * used location.
	 * <p>
	 * Note: This is not a static property - it can change during the
	 * lifetime of this control.</p>
	 *
	 * @return <code>true</code> if restoring location is supported
	 */
	boolean restoresLocation();
}
