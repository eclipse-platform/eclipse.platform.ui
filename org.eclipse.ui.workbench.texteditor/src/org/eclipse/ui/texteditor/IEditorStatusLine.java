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
 * An interface to use the status line of an editor.
 *
 * @since 2.1
 */
public interface IEditorStatusLine {
	/**
	 * Sets the image and message to be displayed on the status line.
	 * <p>
	 * The error flag indicates that the message is an error message.
	 * If the error flag is set, a potential non-error message is overridden.
	 * If the error message is <code>null</code>, the non-error message is displayed.
	 * </p>
	 *
	 * @param error indicates that the message is an error message
	 * @param message the message to set (may be <code>null</code> to clear the message)
	 * @param image the image to set (may be <code>null</code> to clear the image)
	 */
	void setMessage(boolean error, String message, Image image);

}
