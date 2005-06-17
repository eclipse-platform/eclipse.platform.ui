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

package org.eclipse.jface.text.formatter;


/**
 * A formatting strategy is assumed to be specialized on formatting text
 * of a particular content type. Each formatting process calls the strategy's
 * methods in the following sequence:
 * <ul>
 * <li><code>formatterStarts</code>
 * <li><code>format</code>
 * <li><code>formatterStops</code>
 * </ul>
 * <p>
 * This interface must be implemented by clients. Implementers should be registered with
 * a content formatter in order get involved in the formatting process.</p>
 */
public interface IFormattingStrategy {

	/**
	 * Informs the strategy about the start of a formatting process in which it will
	 * participate.
	 *
	 * @param initialIndentation the indent string of the first line at which the
	 *		overall formatting process starts.
	 */
	void formatterStarts(String initialIndentation);

	/**
	 * Formats the given string. During the formatting process this strategy must update
	 * the given character positions according to the changes applied to the given string.
	 *
	 * @param content the initial string to be formatted
	 * @param isLineStart indicates whether the beginning of content is a line start in its document
	 * @param indentation the indentation string to be used
	 * @param positions the character positions to be updated
	 * @return the formatted string
	 */
	String format(String content, boolean isLineStart, String indentation, int[] positions);

	/**
	 * Informs the strategy that the formatting process in which it has participated
	 * has been finished.
	 */
	void formatterStops();
}
