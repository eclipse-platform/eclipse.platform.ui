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
package org.eclipse.jface.text;


/**
 * Default implementation of {@link org.eclipse.jface.text.IPositionUpdater}.<p>
 * A default position updater must be configured with the position category
 * whose positions it will update. Other position categories are not affected
 * by this updater.<p>
 * This implementation follows the following specification:
 * <ul>
 * <li> Inserting or deleting text before the position shifts the position accordingly.
 * <li> Inserting text at the position offset shifts the position accordingly.
 * <li> Inserting or deleting text completely surrounded by the position shrinks or stretches the position.
 * <li> Inserting or deleting text after a position does not affect the position.
 * <li> Deleting text which completely contains the position deletes the position.
 * <li> Replacing text overlapping with the position considered as a sequence of first
 * 		deleting the replaced text and afterwards inserting the new text. Thus, a
 * 		position might first be shifted and shrink and then be stretched.
 * </ul>
 * This class can be used as is or be adapted by subclasses. Fields are protected to
 * allow subclasses direct access. Because of the frequency with which position updaters
 * are used this is a performance decision.
 */
public class DefaultPositionUpdater implements IPositionUpdater {

	/** The position category the updater draws responsible for */
	private String fCategory;

	/** Caches the currently investigated position */
	protected Position fPosition;
	/**
	 * Remembers the original state of the investigated position
	 * @since 2.1
	 */
	protected Position fOriginalPosition= new Position(0, 0);
	/** Caches the offset of the replaced text */
	protected int fOffset;
	/** Caches the length of the replaced text */
	protected int fLength;
	/** Caches the length of the newly inserted text */
	protected int fReplaceLength;
	/** Catches the document */
	protected IDocument fDocument;


	/**
	 * Creates a new default position updater for the given category.
	 *
	 * @param category the category the updater is responsible for
	 */
	public DefaultPositionUpdater(String category) {
		fCategory= category;
	}

	/**
	 * Returns the category this updater is responsible for.
	 *
	 * @return the category this updater is responsible for
	 */
	protected String getCategory() {
		return fCategory;
	}

	/**
	 * Returns whether the current event describes a well formed replace
	 * by which the current position is directly affected.
	 *
	 * @return <code>true</code> the current position is directly affected
	 * @since 3.0
	 */
	protected boolean isAffectingReplace() {
		return fLength > 0 && fReplaceLength > 0 && fPosition.length < fOriginalPosition.length;
	}

	/**
	 * Adapts the currently investigated position to an insertion.
	 */
	protected void adaptToInsert() {

		int myStart= fPosition.offset;
		int myEnd=   fPosition.offset + fPosition.length - (isAffectingReplace() ? 0 : 1);
		myEnd= Math.max(myStart, myEnd);

		int yoursStart= fOffset;
		int yoursEnd=   fOffset + fReplaceLength -1;
		yoursEnd= Math.max(yoursStart, yoursEnd);

		if (myEnd < yoursStart)
			return;

		if (fLength <= 0) {

			if (myStart < yoursStart)
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

	/**
	 * Adapts the currently investigated position to a deletion.
	 */
	protected void adaptToRemove() {

		int myStart= fPosition.offset;
		int myEnd=   fPosition.offset + fPosition.length -1;
		myEnd= Math.max(myStart, myEnd);

		int yoursStart= fOffset;
		int yoursEnd=   fOffset + fLength -1;
		yoursEnd= Math.max(yoursStart, yoursEnd);

		if (myEnd < yoursStart)
			return;

		if (myStart <= yoursStart) {

			if (yoursEnd <= myEnd)
				fPosition.length -= fLength;
			else
				fPosition.length -= (myEnd - yoursStart +1);

		} else if (yoursStart < myStart) {

			if (yoursEnd < myStart)
				fPosition.offset -= fLength;
			else {
				fPosition.offset -= (myStart - yoursStart);
				fPosition.length -= (yoursEnd - myStart +1);
			}

		}

		// validate position to allowed values
		if (fPosition.offset < 0)
			fPosition.offset= 0;

		if (fPosition.length < 0)
			fPosition.length= 0;
	}

	/**
	 * Adapts the currently investigated position to the replace operation.
	 * First it checks whether the change replaces the whole range of the position.
	 * If not, it performs first the deletion of the previous text and afterwards
	 * the insertion of the new text.
	 */
	protected void adaptToReplace() {

		if (fPosition.offset == fOffset && fPosition.length == fLength && fPosition.length > 0) {

			// replace the whole range of the position
			fPosition.length += (fReplaceLength - fLength);
			if (fPosition.length < 0) {
				fPosition.offset += fPosition.length;
				fPosition.length= 0;
			}

		} else {

			if (fLength >  0)
				adaptToRemove();

			if (fReplaceLength > 0)
				adaptToInsert();
		}
	}

	/**
	 * Determines whether the currently investigated position has been deleted by
	 * the replace operation specified in the current event. If so, it deletes
	 * the position and removes it from the document's position category.
	 *
	 * @return <code>true</code> if position has been deleted
	 */
	protected boolean notDeleted() {

		if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {

			fPosition.delete();

			try {
				fDocument.removePosition(fCategory, fPosition);
			} catch (BadPositionCategoryException x) {
			}

			return false;
		}

		return true;
	}

	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	public void update(DocumentEvent event) {

		try {


			fOffset= event.getOffset();
			fLength= event.getLength();
			fReplaceLength= (event.getText() == null ? 0 : event.getText().length());
			fDocument= event.getDocument();

			Position[] category= fDocument.getPositions(fCategory);
			for (int i= 0; i < category.length; i++) {

				fPosition= category[i];
				fOriginalPosition.offset= fPosition.offset;
				fOriginalPosition.length= fPosition.length;

				if (notDeleted())
					adaptToReplace();
			}

		} catch (BadPositionCategoryException x) {
			// do nothing
		} finally {
			fDocument= null;
		}
	}
}
