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

package org.eclipse.jface.text;


/**
 * The position updater used to adapt the fragments of a projection document to
 * changes of the master document.
 */
public class FragmentUpdater extends DefaultPositionUpdater {
	
	private boolean fShiftMode= false;
	private boolean fIsLast= false;
	
	/**
	 * Creates the fragment updater.
	 */
	protected FragmentUpdater(String fragmentCategory) {
		super(fragmentCategory);
	}
	
	public void enableShiftMode(boolean enable) {
		fShiftMode= enable;
	}
	
	/**
	 * If an insertion happens at a fragment's offset, the fragment is extended
	 * rather than shifted. Also, the last fragment is extended if an inserted
	 * happends at the end of the fragment.
	 */
	protected void adaptToInsert() {
		
		if (fShiftMode) {
			super.adaptToInsert();
			return;
		}

		int myStart= fPosition.offset;
		int myEnd= fPosition.offset + fPosition.length - (fIsLast ? 0 : 1);
		myEnd= Math.max(myStart, myEnd);

		int yoursStart= fOffset;
		int yoursEnd= fOffset + fReplaceLength -1;
		yoursEnd= Math.max(yoursStart, yoursEnd);

		if (myEnd < yoursStart)
			return;

		if (fLength <= 0) {

			if (myStart <= yoursStart)
				fPosition.length += fReplaceLength;
			else
				fPosition.offset += fReplaceLength;

		} else {

			if (myStart <= yoursStart && fOriginalPosition.offset <= yoursStart)
				fPosition.length += fReplaceLength;
			else
				fPosition.offset += fReplaceLength;
		}
	}
	
	/*
	 * @see IPositionUpdater#update(DocumentEvent event)
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
};
