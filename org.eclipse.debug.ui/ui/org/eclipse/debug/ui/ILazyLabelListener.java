/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.swt.graphics.Image;

/**
 * Listener notified when the final value of a label has been computed.
 * 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.debug.ui.ILazyDebugModelPresentation
 * 
 * @since 3.0
 */
public interface ILazyLabelListener {
	
	/**
	 * Notifie the listener that the text label of the given elements
	 * have been computed.
	 * @param elements the computed elements.
	 * @param values the text label of the elements.
	 */
	void lazyTextsComputed(Object[] elements, String[] values);

	/**
	 * Notifie the listener that the image label of the given elements
	 * have been computed.
	 * @param elements the computed elements.
	 * @param values the image label of the elements.
	 */
	void lazyImagesComputed(Object[] elements, Image[] images);
}
