package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.text.BreakIterator;
import java.text.CharacterIterator;


/**
 * Standard implementation of <code>ITextDoubleClickStrategy</code>.
 * Selects words using <code>java.text.BreakIterator</code> for the
 * default locale.
 * This class is not intended to be subclassed.
 */
public class DefaultTextDoubleClickStrategy implements ITextDoubleClickStrategy {
	
	
	
	/**
	 * Implements a character iterator for documents.
	 */
	static class DocumentCharacterIterator implements CharacterIterator {
		
		private IDocument fDocument;
		private int fOffset= -1;
		private int fEndOffset= -1;
		private int fIndex= -1;
		
		public DocumentCharacterIterator() {
		}
		
		public void setDocument(IDocument document, IRegion iteratorRange) {
			fDocument= document;
			fOffset= iteratorRange.getOffset();
			fEndOffset= fOffset + iteratorRange.getLength();
		}
		
		/**
		 * @see CharacterIterator#first()
		 */
		public char first() {
			fIndex= fOffset;
			return current();
		}
		
		/**
		 * @see CharacterIterator#last()
		 */
		public char last() {
	        fIndex= fOffset < fEndOffset ? fEndOffset -1 : fEndOffset;
        	return current();
		}
		
		/**
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
		
		/**
		 * @see CharacterIterator#next()
		 */
		public char next() {
			if (fIndex == fEndOffset -1)
				return DONE;
			
			if (fIndex < fEndOffset)
				++ fIndex;
				
			return current();
		}
		
		/**
		 * @see CharacterIterator#previous()
		 */
		public char previous() {
			if (fIndex == fOffset)
				return DONE;
				
			if (fIndex > fOffset)
				-- fIndex;
			
			return current();
		}
		
		/**
		 * @see CharacterIterator#setIndex(int)
		 */
		public char setIndex(int index) {
			fIndex= index;
			return current();
		}
		
		/**
		 * @see CharacterIterator#getBeginIndex()
		 */
		public int getBeginIndex() {
			return fOffset;
		}
		
		/**
		 * @see CharacterIterator#getEndIndex()
		 */
		public int getEndIndex() {
			return fEndOffset;
		}
		
		/**
		 * @see CharacterIterator#getIndex()
		 */
		public int getIndex() {
			return fIndex;
		}
		
		/**
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
	};
	
	
	/** The document character iterator used by this strategy */
	private DocumentCharacterIterator fDocIter= new DocumentCharacterIterator();
	
	
	/**
	 * Creates a new default text double click strategy.
	 */
	public DefaultTextDoubleClickStrategy() {
		super();
	}
	
	/*
	 * @see ITextDoubleClickStrategy#doubleClicked
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
			
			if (start != end)
				text.setSelectedRange(start, end - start);
			
		} catch (BadLocationException x) {
		}
	}
}
