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
 * Defines the interface by which <code>WordRule</code>
 * determines whether a given character is valid as part
 * of a word in the current context.
 */
public interface IWordDetector {

	/**
	 * Returns whether the specified character is
	 * valid as the first character in a word.
	 *
	 * @param c the character to be checked
	 * @return <code>true</code> is a valid first character in a word, <code>false</code> otherwise
	 */
	boolean isWordStart(char c);

	/**
	 * Returns whether the specified character is
	 * valid as a subsequent character in a word.
	 *
	 * @param c the character to be checked
	 * @return <code>true</code> if the character is a valid word part, <code>false</code> otherwise
	 */
	boolean isWordPart(char c);
}
