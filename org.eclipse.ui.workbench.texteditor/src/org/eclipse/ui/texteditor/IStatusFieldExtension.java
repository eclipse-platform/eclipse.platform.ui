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

package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.Image;

/**
 * Extends {@link org.eclipse.ui.texteditor.IStatusField} with the following
 * concepts:
 * <ul>
 *    <li>set error text and image</li>
 *    <li>set tool tip</li>
 *    <li>control visibility</li>
 * </ul>
 *
 * @since 3.0
 */
public interface IStatusFieldExtension {
	/**
	 * Sets the text of this status field.
	 * <p>
	 * The error text overrides the current text until the error
	 * text is cleared (set to <code>null</code>).
	 * </p>
	 *
	 * @param text the error text shown in the status field or <code>null</code> to clear
	 * @see IStatusField#setText(String)
	 */
	void setErrorText(String text);

	/**
	 * Sets the error image of this status field.
	 * <p>
	 * The error image overrides the current image until the error
	 * image is cleared (set to <code>null</code>).
	 * </p>
	 *
	 * @param image the error image shown in the status field or <code>null</code> to clear
	 * @see IStatusField#setImage(Image)
	 */
	void setErrorImage(Image image);

	/**
	 * Sets tool tip text for this status field.
	 *
	 * @param string the new tool tip text or <code>null</code> to clear
	 */
	void setToolTipText (String string);

	/**
	 * Sets whether this status field is visible within the status line.
	 *
	 * @param visible <code>true</code> if this item should be visible, <code>false</code> otherwise
	 */
	void setVisible(boolean visible);
}
