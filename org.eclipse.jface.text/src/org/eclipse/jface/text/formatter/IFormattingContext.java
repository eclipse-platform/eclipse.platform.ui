/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package org.eclipse.jface.text.formatter;

/**
 * Formatting context used in formatting strategies implementing
 * interface <code>IFormattingStrategyExtension</code>.
 * 
 * @see IFormattingStrategyExtension
 * @see IFormattingContextProperties
 * @since 3.0
 */
public interface IFormattingContext {

	/**
	 * Retrieves the property <code>key</code> from the formatting context
	 * 
	 * @param key Key of the property to store in the context
	 * @return The property <code>key</code> if available, <code>null</code> otherwise
	 */
	public Object getProperty(Object key);

	/**
	 * Stores the property <code>key</code> in the formatting context.
	 * 
	 * @param key Key of the property to store in the context
	 * @param property Property to store in the context. If already present, the
	 * new property overwrites the present one.
	 */
	void setProperty(Object key, Object property);
	
	/**
	 * Disposes this formatting context.
	 */
	void dispose();
}
