/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import org.eclipse.jface.text.IRegion;


/**
 * Represents a hyperlink.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * NOTE: This API is work in progress and may change before the final API freeze. (FIXME)
 * </p>
 * 
 * @since 3.1
 */
public interface IHyperlink {
	IRegion getRegion();
	
	/**
	 * Optional label for this type of hyperlink.
	 * <p>
	 * This type label can be used by {@link IHyperlinkController}s
	 * which show several hyperlinks at once.
	 * </p>
	 * 
	 * @return the type label or <code>null</code> if none.
	 */
	String getTypeLabel();
	
	/**
	 * Optional text for this hyperlink.
	 * <p>
	 * This can be used in situations where there are
	 * several targets for the same hyperlink location.
	 * </p>
	 * 
	 * @return the text or <code>null</code> if none.
	 */
	String getText();
	
	/**
	 * Opens the given hyperlink.
	 */
	void open();
}
