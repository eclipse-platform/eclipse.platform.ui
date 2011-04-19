/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;

import java.text.CharacterIterator;
import java.util.Locale;

import com.ibm.icu.text.BreakIterator;


/**
 * Standard implementation of
 * {@link org.eclipse.jface.text.ITextDoubleClickStrategy}.
 * <p>
 * Selects words using <code>java.text.BreakIterator</code> for the default
 * locale.</p>
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
			++fIndex;
			int end= getEndIndex();
			if (fIndex >= end) {
				fIndex= end;
				return DONE;
			}
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
	 * The locale specific word break iterator.
	 * @since 3.7
	 */
	private BreakIterator fWordBreakIterator;

	/**
	 * The POSIX word break iterator.
	 * <p>
	 * Used to workaround ICU bug not treating '.' as word boundary, see
	 * http://bugs.icu-project.org/trac/ticket/8371 for details.
	 * </p>
	 * 
	 * @since 3.7
	 */
	private BreakIterator fPOSIXWordBreakIterator;


	/**
	 * Creates a new default text double click strategy.
	 */
	public DefaultTextDoubleClickStrategy() {
	}

	/*
	 * @see org.eclipse.jface.text.ITextDoubleClickStrategy#doubleClicked(org.eclipse.jface.text.ITextViewer)
	 */
	public void doubleClicked(ITextViewer text) {

		int offset= text.getSelectedRange().x;

		if (offset < 0)
			return;

		final IDocument document= text.getDocument();
		IRegion region= findExtendedDoubleClickSelection(document, offset);
		if (region == null)
			region= findWord(document, offset);
		if (region != null)
			text.setSelectedRange(region.getOffset(), region.getLength());

	}

	/**
	 * Tries to find a suitable double click selection for the given offset.
	 * <p>
	 * <strong>Note:</strong> This method must return <code>null</code> if it simply selects the word at
	 * the given offset.
	 * </p>
	 * 
	 * @param document the document
	 * @param offset the offset
	 * @return the selection or <code>null</code> if none to indicate simple word selection
	 * @since 3.5
	 */
	protected IRegion findExtendedDoubleClickSelection(IDocument document, int offset) {
		return null;
	}

	/**
	 * Tries to find the word at the given offset.
	 * 
	 * @param document the document
	 * @param offset the offset
	 * @return the word or <code>null</code> if none
	 * @since 3.5
	 */
	protected IRegion findWord(IDocument document, int offset) {
		return findWord(document, offset, getWordBreakIterator());
	}

	/**
	 * Returns the locale specific word break iterator.
	 * 
	 * @return the locale specific word break iterator
	 * @since 3.7
	 */
	private BreakIterator getWordBreakIterator() {
		if (fWordBreakIterator == null)
			fWordBreakIterator= BreakIterator.getWordInstance();
		return fWordBreakIterator;
	}

	/**
	 * Returns the POSIX word break iterator.
	 * 
	 * <p>
	 * Used to workaround ICU bug not treating '.' as word boundary, see
	 * http://bugs.icu-project.org/trac/ticket/8371 for details.
	 * </p>
	 * 
	 * @return the POSIX word break iterator.
	 * @since 3.7
	 */
	private BreakIterator getPOSIXWordBreakIterator() {
		if (fPOSIXWordBreakIterator == null)
			fPOSIXWordBreakIterator= BreakIterator.getWordInstance(new Locale("en", "US", "POSIX")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return fPOSIXWordBreakIterator;
	}

	/**
	 * Tries to find the word at the given offset.
	 * 
	 * @param document the document
	 * @param offset the offset
	 * @param wordBreakIterator the word break iterator
	 * @return the word or <code>null</code> if none
	 * @since 3.7
	 */
	private IRegion findWord(IDocument document, int offset, BreakIterator wordBreakIterator) {
		IRegion line;
		try {
			line= document.getLineInformationOfOffset(offset);
		} catch (BadLocationException e) {
			return null;
		}

		if (offset == line.getOffset() + line.getLength())
			return null;

		fDocIter.setDocument(document, line);

		wordBreakIterator.setText(fDocIter);

		int start= wordBreakIterator.preceding(offset);
		if (start == BreakIterator.DONE)
			start= line.getOffset();

		int end= wordBreakIterator.following(offset);
		if (end == BreakIterator.DONE)
			end= line.getOffset() + line.getLength();

		if (wordBreakIterator.isBoundary(offset)) {
			if (end - offset > offset - start)
				start= offset;
			else
				end= offset;
		}

		if (end == start)
			return null;

		int length= end - start;
		try {
			// Workaround for ICU bug not treating '.' as word boundary, see http://bugs.icu-project.org/trac/ticket/8371 for details.
			if (fPOSIXWordBreakIterator != wordBreakIterator && document.get(start, length).indexOf('.') != -1) {
				IRegion wordRegion= findWord(document, offset, getPOSIXWordBreakIterator());
				if (wordRegion != null) {
					int wordStart= wordRegion.getOffset();
					int wordEnd= wordStart + wordRegion.getLength();
					// Check that no additional breaks besides '.' are introduced
					if ((wordStart == start || wordStart > start && document.getChar(wordStart - 1) == '.') && (wordEnd == end || wordEnd < end && document.getChar(wordEnd) == '.'))
						return wordRegion;
				}
			}
		} catch (BadLocationException e) {
			// Use previously computed word region
		}

		return new Region(start, length);
		
	}
}
