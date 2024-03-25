/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.swt.graphics.Image;


/**
 * Interface of a status field of a text editor. The field that shows up in the
 * workbench's status line if the contributing editor is active.
 * <p>
 * In order to provided backward compatibility for clients of
 * <code>IStatusField</code>, extension interfaces are used to provide a means
 * of evolution. The following extension interface exists:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.ui.texteditor.IStatusFieldExtension} since version 3.0
 * introducing error text, tooltips and visibility control.</li>
 * </ul>
 *
 * @see org.eclipse.ui.texteditor.IStatusFieldExtension
 * @since 2.0
 */
public interface IStatusField {

	/**
	 * Sets the text of this status field.
	 *
	 * @param text the text shown in the status field
	 */
	void setText(String text);

	/**
	 * Sets the image of this status field.
	 *
	 * @param image the image shown in the status field
	 */
	void setImage(Image image);
}

