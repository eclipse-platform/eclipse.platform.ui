/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.Position;


/**
 * The position updater used to adapt the fragments of a document. If an
 * insertion happens at a fragment's offset, the fragment is extended rather
 * than shifted. Also, the last fragment is extended if an insert operation
 * happens at the end of the fragment.
 * <p>
 * Internal class. Do not use. Only public for testing purposes.
 * 
 * @since 3.0
 */
public class FragmentUpdater extends DefaultPositionUpdater {
	
	/** Indicates whether the position being updated represents the last fragment. */
	private boolean fIsLast= false;
	
	/**
	 * Creates the fragment updater for the given category.
	 * 
	 * @param fragmentCategory the position category used for managing the fragments of a document
	 */
	protected FragmentUpdater(String fragmentCategory) {
		super(fragmentCategory);
	}
	
	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	public void update(DocumentEvent event) {

		try {

			Position[] category= event.getDocument().getPositions(getCategory());

			fOffset= event.getOffset();
			fLength= event.getLength();
			fReplaceLength= (event.getText() == null ? 0 : event.getText().length());
			fDocument= event.getDocument();

			for (int i= 0; i < category.length; i++) {
				
				fPosition= category[i];
				fIsLast= (i == category.length -1);
				
				fOriginalPosition.offset= fPosition.offset;
				fOriginalPosition.length= fPosition.length;

				if (notDeleted())
					adaptToReplace();
			}

		} catch (BadPositionCategoryException x) {
			// do nothing
		}
	}
		
	/*
	 * @see org.eclipse.jface.text.DefaultPositionUpdater#adaptToInsert()
	 */
	protected void adaptToInsert() {
		int myStart= fPosition.offset;
		int myEnd= Math.max(myStart, fPosition.offset + fPosition.length - (fIsLast || isAffectingReplace() ? 0 : 1));
		
		if (myEnd < fOffset)
			return;

		if (fLength <= 0) {

			if (myStart <= fOffset)
				fPosition.length += fReplaceLength;
			else
				fPosition.offset += fReplaceLength;

		} else {

			if (myStart <= fOffset && fOriginalPosition.offset <= fOffset)
				fPosition.length += fReplaceLength;
			else
				fPosition.offset += fReplaceLength;
		}
	}
}
