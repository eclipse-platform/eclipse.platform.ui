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
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

/**
 * A character pair matcher finds to a character at a certain document offset
 * the matching peer character. It is the matchers responsibility to define the
 * concepts of "matching" and "peer". The matching process starts at a given
 * offset. Starting of this offset, the matcher chooses a character close to
 * this offset. The anchor defines whether the chosen character is left or right
 * of the initial offset. The matcher then searches for the matching peer
 * character of the chosen character and if it finds one, delivers the minimal
 * region of the document that contains both characters.
 *
 * @since 2.1
 */
public interface ICharacterPairMatcher {

	/**
	 * Indicates the anchor value "right".
	 */
	int RIGHT= 0;
	/**
	 * Indicates the anchor value "left".
	 */
	int LEFT= 1;


	/**
	 * Disposes this pair matcher.
	 */
	void dispose();

	/**
	 * Clears this pair matcher. I.e. the matcher throws away all state it might
	 * remember and prepares itself for a new call of the <code>match</code>
	 * method.
	 */
	void clear();

	/**
	 * Starting at the given offset, the matcher chooses a character close to this offset.
	 * The matcher then searches for the matching peer character of the chosen character
	 * and if it finds one, returns the minimal region of the document that contains both characters.
	 * It returns <code>null</code> if there is no peer character.
	 *
	 * @param iDocument the document to work on
	 * @param i the start offset
	 * @return the minimal region containing the peer characters
	 */
	IRegion match(IDocument iDocument, int i);

	/**
	 * Returns the anchor for the region of the matching peer characters. The anchor
	 * says whether the character that has been chosen to search for its peer character
	 * has been left or right of the initial offset.
	 *
	 * @return <code>RIGHT</code> or <code>LEFT</code>
	 */
	int getAnchor();
}
