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
 * Extention of the IDebugModelPresentation interface to compute labels lazily.
 * <p>
 * A 'lazy' methods should be implemented such as it returns as fast as possible.
 * The returned value can be the final value, or a tempory value which will be display
 * while the final value is computed.
 * When the final value is computed, the listener method should be called, so the label
 * can be updated.
 * </p>
 * <p>
 * There is no guarantee that the execution time will be short.
 * There is no guarantee that the listener method will be called.
 * </p>
 * 
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.debug.ui.ILazyLabelListener
 * 
 * @since 3.0
 */
public interface ILazyDebugModelPresentation extends IDebugModelPresentation {
	
	/**
	 * Request to compute the image lazily.
	 * @param element the debug model element.
	 * @param listener the listener to notify when the final image is computed.
	 * @return the final image or a tempory image.
	 */
	Image getLazyImage(Object element, ILazyLabelListener listener);
	
	/**
	 * Request to compute the text lazily.
	 * @param element the debug model element.
	 * @param listener the listener to notify when the final text is computed.
	 * @return the final text or a tempory text.
	 */
	String getLazyText(Object element, ILazyLabelListener listener);

}
