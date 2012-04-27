/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Plesner Hansen (plesner@quenta.org) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;
import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

/**
 * A character pair matcher that matches a specified set of character
 * pairs against each other.  Only characters that occur in the same
 * partitioning are matched.
 *
 * @since 3.3
 */
public class DefaultCharacterPairMatcher implements ICharacterPairMatcher, ICharacterPairMatcherExtension {

	private int fAnchor= -1;
	private final CharPairs fPairs;
	private final String fPartitioning;
	private final boolean fCaretEitherSideOfBracket;

	/**
	 * Creates a new character pair matcher that matches the specified characters within the
	 * specified partitioning. The specified list of characters must have the form <blockquote>{
	 * <i>start</i>, <i>end</i>, <i>start</i>, <i>end</i>, ..., <i>start</i>, <i>end</i>
	 * }</blockquote> For instance:
	 * 
	 * <pre>
	 * char[] chars = new char[] {'(', ')', '{', '}', '[', ']'};
	 * new DefaultCharacterPairMatcher(chars, ...);
	 * </pre>
	 * 
	 * @param chars a list of characters
	 * @param partitioning the partitioning to match within
	 */
	public DefaultCharacterPairMatcher(char[] chars, String partitioning) {
		this(chars, partitioning, false);
	}

	/**
	 * Creates a new character pair matcher that matches the specified characters within the
	 * specified partitioning. The specified list of characters must have the form <blockquote>{
	 * <i>start</i>, <i>end</i>, <i>start</i>, <i>end</i>, ..., <i>start</i>, <i>end</i>
	 * }</blockquote> For instance:
	 * 
	 * <pre>
	 * char[] chars = new char[] {'(', ')', '{', '}', '[', ']'};
	 * new DefaultCharacterPairMatcher(chars, ...);
	 * </pre>
	 * 
	 * @param chars a list of characters
	 * @param partitioning the partitioning to match within
	 * @param caretEitherSideOfBracket controls the matching behavior. When <code>true</code>, the
	 *            matching peer will be found when the caret is placed either before or after a
	 *            character. When <code>false</code>, the matching peer will be found only when the
	 *            caret is placed after a character.
	 * @since 3.8
	 */
	public DefaultCharacterPairMatcher(char[] chars, String partitioning, boolean caretEitherSideOfBracket) {
		Assert.isLegal(chars.length % 2 == 0);
		Assert.isNotNull(partitioning);
		fPairs= new CharPairs(chars);
		fPartitioning= partitioning;
		fCaretEitherSideOfBracket= caretEitherSideOfBracket;
	}

	/**
	 * Creates a new character pair matcher that matches characters within the default partitioning.
	 * The specified list of characters must have the form <blockquote>{ <i>start</i>, <i>end</i>,
	 * <i>start</i>, <i>end</i>, ..., <i>start</i>, <i>end</i> }</blockquote> For instance:
	 * 
	 * <pre>
	 * char[] chars= new char[] { '(', ')', '{', '}', '[', ']' };
	 * new DefaultCharacterPairMatcher(chars);
	 * </pre>
	 * 
	 * @param chars a list of characters
	 */
	public DefaultCharacterPairMatcher(char[] chars) {
		this(chars, IDocumentExtension3.DEFAULT_PARTITIONING);
	}

	/* @see ICharacterPairMatcher#match(IDocument, int) */
	public IRegion match(IDocument doc, int offset) {
		if (doc == null || offset < 0 || offset > doc.getLength()) return null;
		try {
			return performMatch(doc, offset);
		} catch (BadLocationException ble) {
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcherExtension#match(org.eclipse.jface.text.IDocument,
	 *      int, int)
	 * @since 3.8
	 */
	public IRegion match(IDocument document, int offset, int length) {
		if (document == null || offset < 0 || offset > document.getLength() || Math.abs(length) > 1)
			return null;

		try {
			int sourceCaretOffset= offset + length;
			if (Math.abs(length) == 1) {
				char ch= length > 0 ? document.getChar(offset) : document.getChar(sourceCaretOffset);
				if (!fPairs.contains(ch))
					return null;
			}
			int adjustment= getOffsetAdjustment(document, sourceCaretOffset, length);
			sourceCaretOffset+= adjustment;
			return match(document, sourceCaretOffset);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcherExtension#findEnclosingPeerCharacters(org.eclipse.jface.text.IDocument,
	 *      int, int)
	 * @since 3.8
	 */
	public IRegion findEnclosingPeerCharacters(IDocument document, int offset, int length) {
		if (document == null || offset < 0 || offset > document.getLength())
			return null;

		//maybe a bracket is selected
		IRegion region= match(document, offset, length);
		fAnchor= ICharacterPairMatcher.LEFT; //always set the anchor to LEFT
		if (region != null) {
			return region;
		}

		//bracket is not selected
		try {
			final String partition= TextUtilities.getContentType(document, fPartitioning, offset, false);
			DocumentPartitionAccessor partDoc= new DocumentPartitionAccessor(document, fPartitioning, partition);
			IRegion enclosingPeers= findEnclosingPeers(document, partDoc, offset, length, 0, document.getLength());
			if (enclosingPeers != null)
				return enclosingPeers;
			partDoc= new DocumentPartitionAccessor(document, fPartitioning, IDocument.DEFAULT_CONTENT_TYPE);
			return findEnclosingPeers(document, partDoc, offset, length, 0, document.getLength());
		} catch (BadLocationException ble) {
			fAnchor= -1;
			return null;
		}
	}

	/**
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcherExtension#isMatchedChar(char)
	 * @since 3.8
	 */
	public boolean isMatchedChar(char ch) {
		return fPairs.contains(ch);
	}

	/**
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcherExtension#isMatchedChar(char,
	 *      org.eclipse.jface.text.IDocument, int)
	 * @since 3.8
	 */
	public boolean isMatchedChar(char ch, IDocument document, int offset) {
		return isMatchedChar(ch);
	}

	/**
	 * @see org.eclipse.jface.text.source.ICharacterPairMatcherExtension#isRecomputationOfEnclosingPairRequired(org.eclipse.jface.text.IDocument,
	 *      org.eclipse.jface.text.IRegion, org.eclipse.jface.text.IRegion)
	 * @since 3.8
	 */
	public boolean isRecomputationOfEnclosingPairRequired(IDocument document, IRegion currentSelection, IRegion previousSelection) {
		int previousStartOffset= previousSelection.getOffset();
		int currentStartOffset= currentSelection.getOffset();
		int previousEndOffset= previousStartOffset + previousSelection.getLength();
		int currentEndOffset= currentStartOffset + currentSelection.getLength();

		try {
			String prevEndContentType= TextUtilities.getContentType(document, fPartitioning, previousEndOffset, false);
			String currEndContentType= TextUtilities.getContentType(document, fPartitioning, currentEndOffset, false);
			if (!prevEndContentType.equals(currEndContentType))
				return true;
			
			String prevStartContentType= TextUtilities.getContentType(document, fPartitioning, previousStartOffset, true);
			String currStartContentType= TextUtilities.getContentType(document, fPartitioning, currentStartOffset, true);
			if (!prevStartContentType.equals(currStartContentType))
				return true;
			
			int start;
			int end;
			if (currentEndOffset > previousEndOffset) {
				start= previousEndOffset;
				end= currentEndOffset;
			} else {
				start= currentEndOffset;
				end= previousEndOffset;
			}
			for (int i= Math.max(start - 1, 0); i <= end; i++) {
				if (isMatchedChar(document.getChar(i))) {
					return true;
				}
			}
			
			if (currentStartOffset > previousStartOffset) {
				start= previousStartOffset;
				end= currentStartOffset;
			} else {
				start= currentStartOffset;
				end= previousStartOffset;
			}
			for (int i= Math.max(start - 1, 0); i <= end; i++) {
				if (isMatchedChar(document.getChar(i))) {
					return true;
				}
			}
		} catch (BadLocationException e) {
			//do nothing
		}
		return false;
	}

	/**
	 * Computes the adjustment in the start offset for the purpose of finding a matching peer. This
	 * is required as the direction of selection can be right-to-left or left-to-right.
	 * 
	 * @param document the document to work on
	 * @param offset the start offset
	 * @param length the selection length
	 * @return the start offset adjustment which can be -1, 0 or +1
	 * @since 3.8
	 */
	private int getOffsetAdjustment(IDocument document, int offset, int length) {
		if (length == 0 || Math.abs(length) > 1 || offset >= document.getLength())
			return 0;
		try {
			if (length < 0) {
				if (fPairs.isStartCharacter(document.getChar(offset))) {
					return 1;
				}
			} else {
				if (fCaretEitherSideOfBracket && fPairs.isEndCharacter(document.getChar(offset - 1))) {
					return -1;
				}
			}
		} catch (BadLocationException e) {
			//do nothing
		}
		return 0;
	}

	/*
	 * Performs the actual work of matching for #match(IDocument, int).
	 */
	private IRegion performMatch(IDocument doc, int caretOffset) throws BadLocationException {
		char prevChar= (caretOffset - 1 >= 0) ? doc.getChar(caretOffset - 1) : Character.MIN_VALUE;
		boolean isForward;
		final char ch;
		if (fCaretEitherSideOfBracket) {
			char currChar= (caretOffset != doc.getLength()) ? doc.getChar(caretOffset) : Character.MIN_VALUE;
			if (fPairs.isEndCharacter(prevChar) && !fPairs.isEndCharacter(currChar)) { //https://bugs.eclipse.org/bugs/show_bug.cgi?id=372516
				caretOffset--;
				currChar= prevChar;
				prevChar= doc.getChar(Math.max(caretOffset - 1, 0));
			} else if (fPairs.isStartCharacter(currChar) && !fPairs.contains(prevChar)) {
				caretOffset++;
				prevChar= currChar;
				currChar= doc.getChar(caretOffset);
			}

			isForward= fPairs.contains(prevChar) && fPairs.isStartCharacter(prevChar);
			boolean isBackward= fPairs.contains(currChar) && !fPairs.isStartCharacter(currChar);
			if (!isForward && !isBackward) {
				return null;
			}
			ch= isForward ? prevChar : currChar;
		} else {
			if (!fPairs.contains(prevChar))
				return null;
			isForward= fPairs.isStartCharacter(prevChar);
			ch= prevChar;
		}

		fAnchor= isForward ? ICharacterPairMatcher.LEFT : ICharacterPairMatcher.RIGHT;
		final int searchStartPosition= isForward ? caretOffset : (fCaretEitherSideOfBracket ? caretOffset - 1 : caretOffset - 2);
		final int adjustedOffset= isForward ? caretOffset - 1 : (fCaretEitherSideOfBracket ? caretOffset + 1 : caretOffset);
		final String partition= TextUtilities.getContentType(doc, fPartitioning, ((!isForward && fCaretEitherSideOfBracket) ? caretOffset : Math.max(caretOffset - 1, 0)), false);
		final DocumentPartitionAccessor partDoc= new DocumentPartitionAccessor(doc, fPartitioning, partition);
		int endOffset= findMatchingPeer(partDoc, ch, fPairs.getMatching(ch),
				isForward, isForward ? doc.getLength() : -1, searchStartPosition);
		if (endOffset == -1)
			return null;
		final int adjustedEndOffset= isForward ? endOffset + 1 : endOffset;
		if (adjustedEndOffset == adjustedOffset)
			return null;
		return new Region(Math.min(adjustedOffset, adjustedEndOffset),
				Math.abs(adjustedEndOffset - adjustedOffset));
	}

	/**
	 * Searches <code>doc</code> for the specified end character, <code>end</code>.
	 *
	 * @param doc the document to search
	 * @param start the opening matching character
	 * @param end the end character to search for
	 * @param searchForward search forwards or backwards?
	 * @param boundary a boundary at which the search should stop
	 * @param startPos the start offset
	 * @return the index of the end character if it was found, otherwise -1
	 * @throws BadLocationException if the document is accessed with invalid offset or line
	 */
	private int findMatchingPeer(DocumentPartitionAccessor doc, char start, char end, boolean searchForward, int boundary, int startPos) throws BadLocationException {
		int pos= startPos;
		while (pos != boundary) {
			final char c= doc.getChar(pos);
			if (doc.isMatch(pos, end)) {
				return pos;
			} else if (c == start && doc.inPartition(pos)) {
				pos= findMatchingPeer(doc, start, end, searchForward, boundary,
						doc.getNextPosition(pos, searchForward));
				if (pos == -1) return -1;
			}
			pos= doc.getNextPosition(pos, searchForward);
		}
		return -1;
	}

	/*
	 * Performs the actual work of finding enclosing peer characters for #findEnclosingPeerCharacters(IDocument, int, int).
	 */
	private IRegion findEnclosingPeers(IDocument document, DocumentPartitionAccessor doc, int offset, int length, int lowerBoundary, int upperBoundary) throws BadLocationException {
		char[] pairs= fPairs.fPairs;
	
		int start;
		int end;
		if (length >= 0) {
			start= offset;
			end= offset + length;
		} else {
			end= offset;
			start= offset + length;
		}
	
		boolean lowerFound= false;
		boolean upperFound= false;
		int[][] counts= new int[pairs.length][2];
		char currChar= (start != document.getLength()) ? doc.getChar(start) : Character.MIN_VALUE;
		int pos1;
		int pos2;
		if (fPairs.isEndCharacter(currChar)) {
			pos1= doc.getNextPosition(start, false);
			pos2= start;
		} else {
			pos1= start;
			pos2= doc.getNextPosition(start, true);
		}
	
		while ((pos1 >= lowerBoundary && !lowerFound) || (pos2 < upperBoundary && !upperFound)) {
			for (int i= 0; i < counts.length; i++) {
				counts[i][0]= counts[i][1]= 0;
			}
	
			outer1: while (pos1 >= lowerBoundary && !lowerFound) {
				final char c= doc.getChar(pos1);
				int i= getCharacterIndex(c, document, pos1);
				if (i != -1 && doc.inPartition(pos1)) {
					if (i % 2 == 0) {
						counts[i / 2][0]--; //start
					} else {
						counts[i / 2][0]++; //end
					}
					for (int j= 0; j < counts.length; j++) {
						if (counts[j][0] == -1) {
							lowerFound= true;
							break outer1;
						}
					}
				}
				pos1= doc.getNextPosition(pos1, false);
			}
	
			outer2: while (pos2 < upperBoundary && !upperFound) {
				final char c= doc.getChar(pos2);
				int i= getCharacterIndex(c, document, pos2);
				if (i != -1 && doc.inPartition(pos2)) {
					if (i % 2 == 0) {
						counts[i / 2][1]++; //start
					} else {
						counts[i / 2][1]--; //end
					}
					for (int j= 0; j < counts.length; j++) {
						if (counts[j][1] == -1 && counts[j][0] == -1) {
							upperFound= true;
							break outer2;
						}
					}
				}
				pos2= doc.getNextPosition(pos2, true);
			}
	
			if (pos1 > start || pos2 < end - 1) {
				//match inside selection => discard
				pos1= doc.getNextPosition(pos1, false);
				pos2= doc.getNextPosition(pos2, true);
				lowerFound= false;
				upperFound= false;
			}
		}
		pos2++;
		if (pos1 < lowerBoundary || pos2 > upperBoundary)
			return null;
		return new Region(pos1, pos2 - pos1);
	}

	/**
	 * Determines the index of the character in the char array passed to the constructor of the pair
	 * matcher.
	 * 
	 * @param ch the character
	 * @param document the document
	 * @param offset the offset in document
	 * @return the index of the character in the char array passed to the constructor of the pair
	 *         matcher, and -1 if the character is not one of the matched characters
	 * @since 3.8
	 */
	private int getCharacterIndex(char ch, IDocument document, int offset) {
		char[] pairs= fPairs.fPairs;
		for (int i= 0; i < pairs.length; i++) {
			if (pairs[i] == ch && isMatchedChar(ch, document, offset)) {
				return i;
			}
		}
		return -1;
	}

	/* @see ICharacterPairMatcher#getAnchor() */
	public int getAnchor() {
		return fAnchor;
	}

	/* @see ICharacterPairMatcher#dispose() */
	public void dispose() { }

	/* @see ICharacterPairMatcher#clear() */
	public void clear() {
		fAnchor= -1;
	}

	/**
	 * Utility class that wraps a document and gives access to
	 * partitioning information.  A document is tied to a particular
	 * partition and, when considering whether or not a position is a
	 * valid match, only considers position within its partition.
	 */
	private static class DocumentPartitionAccessor {

		private final IDocument fDocument;
		private final String fPartitioning, fPartition;
		private ITypedRegion fCachedPartition;
		private int fLength;

		/**
		 * Creates a new partitioned document for the specified document.
		 *
		 * @param doc the document to wrap
		 * @param partitioning the partitioning used
		 * @param partition the partition managed by this document
		 */
		public DocumentPartitionAccessor(IDocument doc, String partitioning,
				String partition) {
			fDocument= doc;
			fPartitioning= partitioning;
			fPartition= partition;
			fLength= doc.getLength();
		}

		/**
		 * Returns the character at the specified position in this document.
		 *
		 * @param pos an offset within this document
		 * @return the character at the offset
		 * @throws BadLocationException if the offset is invalid in this document
		 */
		public char getChar(int pos) throws BadLocationException {
			return fDocument.getChar(pos);
		}

		/**
		 * Returns true if the character at the specified position is a valid match for the
		 * specified end character. To be a valid match, it must be in the appropriate partition and
		 * equal to the end character.
		 *
		 * @param pos an offset within this document
		 * @param end the end character to match against
		 * @return true exactly if the position represents a valid match
		 * @throws BadLocationException if the offset is invalid in this document
		 */
		public boolean isMatch(int pos, char end) throws BadLocationException {
			return getChar(pos) == end && inPartition(pos);
		}

		/**
		 * Returns true if the specified offset is within the partition
		 * managed by this document.
		 *
		 * @param pos an offset within this document
		 * @return true if the offset is within this document's partition
		 */
		public boolean inPartition(int pos) {
			final ITypedRegion partition= getPartition(pos);
			return partition != null && partition.getType().equals(fPartition);
		}

		/**
		 * Returns the next position to query in the search. The position
		 * is not guaranteed to be in this document's partition.
		 *
		 * @param pos an offset within the document
		 * @param searchForward the direction of the search
		 * @return the next position to query
		 */
		public int getNextPosition(int pos, boolean searchForward) {
			final ITypedRegion partition= getPartition(pos);
			if (partition == null || fPartition.equals(partition.getType()))
				return simpleIncrement(pos, searchForward);
			if (searchForward) {
				int end= partition.getOffset() + partition.getLength();
				if (pos < end)
					return end;
			} else {
				int offset= partition.getOffset();
				if (pos > offset)
					return offset - 1;
			}
			return simpleIncrement(pos, searchForward);
		}

		private int simpleIncrement(int pos, boolean searchForward) {
			return pos + (searchForward ? 1 : -1);
		}

		/**
		 * Returns partition information about the region containing the
		 * specified position.
		 *
		 * @param pos a position within this document.
		 * @return positioning information about the region containing the
		 *   position
		 */
		private ITypedRegion getPartition(int pos) {
			if (fCachedPartition == null || !contains(fCachedPartition, pos)) {
				Assert.isTrue(pos >= 0 && pos <= fLength);
				try {
					fCachedPartition= TextUtilities.getPartition(fDocument, fPartitioning, pos, false);
				} catch (BadLocationException e) {
					fCachedPartition= null;
				}
			}
			return fCachedPartition;
		}

		private static boolean contains(IRegion region, int pos) {
			int offset= region.getOffset();
			return offset <= pos && pos < offset + region.getLength();
		}

	}

	/**
	 * Utility class that encapsulates access to matching character pairs.
	 */
	private static class CharPairs {

		private final char[] fPairs;

		public CharPairs(char[] pairs) {
			fPairs= pairs;
		}

		/**
		 * Returns true if the specified character occurs in one of the character pairs.
		 * 
		 * @param c a character
		 * @return true exactly if the character occurs in one of the pairs
		 */
		public boolean contains(char c) {
			char[] pairs= fPairs;
			for (int i= 0, n= pairs.length; i < n; i++) {
				if (c == pairs[i])
					return true;
			}
			return false;
		}

		/**
		 * Returns true if the specified character opens a character pair
		 * when scanning in the specified direction.
		 *
		 * @param c a character
		 * @param searchForward the direction of the search
		 * @return whether or not the character opens a character pair
		 */
		public boolean isOpeningCharacter(char c, boolean searchForward) {
			for (int i= 0; i < fPairs.length; i += 2) {
				if (searchForward && getStartChar(i) == c) return true;
				else if (!searchForward && getEndChar(i) == c) return true;
			}
			return false;
		}

		/**
		 * Returns true if the specified character is a start character.
		 * 
		 * @param c a character
		 * @return true exactly if the character is a start character
		 */
		public boolean isStartCharacter(char c) {
			return this.isOpeningCharacter(c, true);
		}

		/**
		 * Returns true if the specified character is an end character.
		 * 
		 * @param c a character
		 * @return true exactly if the character is an end character
		 * @since 3.8
		 */
		public boolean isEndCharacter(char c) {
			return this.isOpeningCharacter(c, false);
		}

		/**
		 * Returns the matching character for the specified character.
		 * 
		 * @param c a character occurring in a character pair
		 * @return the matching character
		 */
		public char getMatching(char c) {
			for (int i= 0; i < fPairs.length; i += 2) {
				if (getStartChar(i) == c) return getEndChar(i);
				else if (getEndChar(i) == c) return getStartChar(i);
			}
			Assert.isTrue(false);
			return '\0';
		}

		private char getStartChar(int i) {
			return fPairs[i];
		}

		private char getEndChar(int i) {
			return fPairs[i + 1];
		}

	}

}
