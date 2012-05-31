/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
 * Extension interface for {@link org.eclipse.jface.text.source.ICharacterPairMatcher}.
 * <p>
 * Extends the character pair matcher with the concept of matching peer character and enclosing peer
 * characters for a given selection.
 * 
 * @see org.eclipse.jface.text.source.ICharacterPairMatcher
 * @since 3.8
 */
public interface ICharacterPairMatcherExtension {

	/**
	 * Starting at the given offset (i.e. length 0) or the selected character, the matcher searches
	 * for the matching peer character and if it finds one, returns the minimal region of the
	 * document that contains both characters.
	 * 
	 * @param document the document to work on
	 * @param offset the start offset
	 * @param length the selection length which can be negative indicating right-to-left selection
	 * @return the minimal region containing the peer characters or <code>null</code> if there is no
	 *         peer character
	 */
	IRegion match(IDocument document, int offset, int length);


	/**
	 * Starting at the given selection, the matcher searches for a pair of enclosing peer characters
	 * and if it finds one, returns the minimal region of the document that contains the pair.
	 * 
	 * @param document the document to work on
	 * @param offset the start offset
	 * @param length the selection length which can be negative indicating right-to-left selection
	 * @return the minimal region containing the peer characters or <code>null</code> if there is no
	 *         enclosing pair
	 */
	IRegion findEnclosingPeerCharacters(IDocument document, int offset, int length);

	/**
	 * Checks whether the character is one of the characters matched by the pair matcher.
	 * 
	 * @param ch the character
	 * @return <code>true</code> if the the character is one of the characters matched by the pair
	 *         matcher, and <code>false</code> otherwise
	 */
	boolean isMatchedChar(char ch);

	/**
	 * Checks whether the character is one of the characters matched by the pair matcher.
	 * 
	 * <p>
	 * Clients can use this method to handle characters which may have special meaning in some
	 * situations. E.g. in Java, '<' is used as an angular bracket and as well as less-than operator.
	 * </p>
	 * 
	 * @param ch the character
	 * @param document the document
	 * @param offset the offset in document
	 * @return <code>true</code> if the the character is one of the characters matched by the pair
	 *         matcher, and <code>false</code> otherwise
	 */
	boolean isMatchedChar(char ch, IDocument document, int offset);

	/**
	 * Computes whether a client needs to recompute the enclosing pair after a selection change in
	 * the document.
	 * 
	 * <p>
	 * This is intended to be a quick test to determine whether a re-computation of the enclosing pair is
	 * required, as the re-computation after each selection change via a
	 * {@link #findEnclosingPeerCharacters(IDocument, int, int)} call can be expensive for some
	 * clients.
	 * </p>
	 * 
	 * @param document the document to work on
	 * @param currentSelection the current selection in the document
	 * @param previousSelection the previous selection in the document
	 * @return <code>true</code> if the enclosing pair needs to be recomputed, <code>false</code>
	 *         otherwise
	 */
	boolean isRecomputationOfEnclosingPairRequired(IDocument document, IRegion currentSelection, IRegion previousSelection);
}
