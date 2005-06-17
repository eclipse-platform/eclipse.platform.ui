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
package org.eclipse.ui.editors.text;


/**
 * Interface to be implemented by objects supporting character encodings.
 *
 * @since 2.0
 */
public interface IEncodingSupport{

	/**
	 * Sets the character encoding.
	 *
	 * @param encoding the character encoding
	 */
	void setEncoding(String encoding);

	/**
	 * Returns the character encoding.
	 *
	 * @return the character encoding
	 */
	String getEncoding();

	/**
	 * Returns the default character encoding.
	 *
	 * @return the default character encoding
	 */
	String getDefaultEncoding();
}
