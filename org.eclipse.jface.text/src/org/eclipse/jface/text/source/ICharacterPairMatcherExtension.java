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
 * Extension interface {@link org.eclipse.jface.text.source.ICharacterPairMatcher}.
 * <p>
 * Extends the character pair matcher with the concept of matching peer character and enclosing peer
 * characters for a given selection.
 * 
 * @see org.eclipse.jface.text.source.ICharacterPairMatcher
 * @since 3.8
 */
public interface ICharacterPairMatcherExtension {

	/**
	 * Starting with the selected character or a character close to the given <code>offset</code>,
	 * the matcher searches for the matching peer character and if it finds one, returns the minimal
	 * region of the document that contains both characters.
	 * 
	 * @param document the document to work on
	 * @param offset the start offset
	 * @param length the selection length. The length can be negative indicating right-to-left
	 *            selection.
	 * @return the minimal region containing the peer characters or <code>null</code> if there is no
	 *         peer character.
	 */
	IRegion match(IDocument document, int offset, int length);


	/**
	 * Starting at the given selection, the matcher searches for a pair of enclosing peer characters
	 * and if it finds one, returns the minimal region of the document that contains the pair.
	 * 
	 * @param document the document to work on
	 * @param offset the start offset
	 * @param length the selection length. The length can be negative indicating right-to-left
	 *            selection.
	 * @return the minimal region containing the peer characters or <code>null</code> if there is no
	 *         enclosing pair
	 */
	IRegion findEnclosingPeerCharacters(IDocument document, int offset, int length);
}
