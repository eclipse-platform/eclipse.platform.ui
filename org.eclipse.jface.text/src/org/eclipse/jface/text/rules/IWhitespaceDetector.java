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
package org.eclipse.jface.text.rules;


/**
 * Defines the interface by which <code>WhitespaceRule</code>
 * determines whether a given character is to be considered
 * whitespace in the current context.
 */
public interface IWhitespaceDetector {

	/**
	 * Returns whether the specified character is whitespace.
	 *
	 * @param c the character to be checked
	 * @return <code>true</code> if the specified character is a whitespace char
	 */
	boolean isWhitespace(char c);
}
