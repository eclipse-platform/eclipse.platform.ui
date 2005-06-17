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

import java.text.BreakIterator;
import java.text.CharacterIterator;


/**
 * Standard implementation of
 * {@link org.eclipse.jface.text.ITextDoubleClickStrategy}.
 * <p>
 * Selects words using <code>java.text.BreakIterator</code> for the default
 * locale.</p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 *
 * @see java.text.BreakIterator
 */
public class DefaultTextDoubleClickStrategy implements ITextDoubleClickStrategy {


	/**
	 * Implements a character iterator that works directly on
	 * instances of <code>IDocument</code>. Used to collaborate with
	 * the break iterator.
	 *
	 * @see IDocument
	 * @since 2.0
	 */
	static class DocumentCharacterIterator implements CharacterIterator {

		/** Document to iterate over. */
		private IDocument fDocument;
		/** Start offset of iteration. */
		private int fOffset= -1;
		/** End offset of iteration. */
		private int fEndOffset= -1;
		/** Current offset of iteration. */
		private int fIndex= -1;

		/** Creates a new document iterator. */
		public DocumentCharacterIterator() {
		}

		/**
		 * Configures this document iterator with the document section to be visited.
		 *
		 * @param document the document to be iterated
		 * @param iteratorRange the range in the document to be iterated
		 */
		public void setDocument(IDocument document, IRegion iteratorRange) {
			fDocument= document;
			fOffset= iteratorRange.getOffset();
			fEndOffset= fOffset + iteratorRange.getLength();
		}

		/*
		 * @see CharacterIterator#first()
		 */
		public char first() {
			fIndex= fOffset;
			return current();
		}

		/*
		 * @see CharacterIterator#last()
		 */
		public char last() {
	        fIndex= fOffset < fEndOffset ? fEndOffset -1 : fEndOffset;
        	return current();
		}

		/*
		 * @see CharacterIterator#current()
		 */
		public char current() {
			if (fOffset <= fIndex && fIndex < fEndOffset) {
				try {
					return fDocument.getChar(fIndex);
				} catch (BadLocationException x) {
				}
			}
			return DONE;
		}

		/*
		 * @see CharacterIterator#next()
		 */
		public char next() {
			if (fIndex == fEndOffset -1)
				return DONE;

			if (fIndex < fEndOffset)
				++ fIndex;

			return current();
		}

		/*
		 * @see CharacterIterator#previous()
		 */
		public char previous() {
			if (fIndex == fOffset)
				return DONE;

			if (fIndex > fOffset)
				-- fIndex;

			return current();
		}

		/*
		 * @see CharacterIterator#setIndex(int)
		 */
		public char setIndex(int index) {
			fIndex= index;
			return current();
		}

		/*
		 * @see CharacterIterator#getBeginIndex()
		 */
		public int getBeginIndex() {
			return fOffset;
		}

		/*
		 * @see CharacterIterator#getEndIndex()
		 */
		public int getEndIndex() {
			return fEndOffset;
		}

		/*
		 * @see CharacterIterator#getIndex()
		 */
		public int getIndex() {
			return fIndex;
		}

		/*
		 * @see CharacterIterator#clone()
		 */
		public Object clone() {
			DocumentCharacterIterator i= new DocumentCharacterIterator();
			i.fDocument= fDocument;
			i.fIndex= fIndex;
			i.fOffset= fOffset;
			i.fEndOffset= fEndOffset;
			return i;
		}
	}


	/**
	 * The document character iterator used by this strategy.
	 * @since 2.0
	 */
	private DocumentCharacterIterator fDocIter= new DocumentCharacterIterator();


	/**
	 * Creates a new default text double click strategy.
	 */
	public DefaultTextDoubleClickStrategy() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(org.eclipse.jface.text.ITextViewer)
	 */
	public void doubleClicked(ITextViewer text) {

		int position= text.getSelectedRange().x;

		if (position < 0)
			return;

		try {

			IDocument document= text.getDocument();
			IRegion line= document.getLineInformationOfOffset(position);
			if (position == line.getOffset() + line.getLength())
				return;

			fDocIter.setDocument(document, line);

			BreakIterator breakIter= BreakIterator.getWordInstance();
			breakIter.setText(fDocIter);

			int start= breakIter.preceding(position);
			if (start == BreakIterator.DONE)
				start= line.getOffset();

			int end= breakIter.following(position);
			if (end == BreakIterator.DONE)
				end= line.getOffset() + line.getLength();

			if (breakIter.isBoundary(position)) {
				if (end - position > position- start)
					start= position;
				else
					end= position;
			}

			if (start != end)
				text.setSelectedRange(start, end - start);

		} catch (BadLocationException x) {
		}
	}
}
