/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.jface.text.*;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.rangedifferencer.IRangeComparator;

/**
 * Implements the <code>IRangeComparator</code> interface for lines in a document.
 * A <code>DocLineComparator</code> is used as the input for the <code>RangeDifferencer</code>
 * engine to perform a line oriented compare on documents.
 * <p>
 * A <code>DocLineComparator</code> doesn't know anything about line separators because
 * its notion of lines is solely defined in the underlying <code>IDocument</code>.
 */
public class DocLineComparator implements ITokenComparator {

	private IDocument fDocument;
	private int fLineOffset;
	private int fLineCount;
	private int fLength;
	private boolean fIgnoreWhiteSpace;

	/**
	 * Creates a <code>DocLineComparator</code> for the given document range.
	 * ignoreWhiteSpace controls whether comparing lines (in method
	 * <code>rangesEqual<code>) should ignore whitespace.
	 *
	 * @param document the document from which the lines are taken
	 * @param region if non-<code>null</code> only lines within this range are taken
	 * @param ignoreWhiteSpace if <code>true</code> white space is ignored when comparing lines
	 */
	public DocLineComparator(IDocument document, IRegion region, boolean ignoreWhiteSpace) {

		fDocument= document;
		fIgnoreWhiteSpace= ignoreWhiteSpace;

		fLineOffset= 0;
		if (region != null) {
			fLength= region.getLength();
			int start= region.getOffset();
			try {
				fLineOffset= fDocument.getLineOfOffset(start);
			} catch (BadLocationException ex) {
			}

			if (fLength == 0)
				fLineCount= 0;
			else {
				int endLine= fDocument.getNumberOfLines();
				try {
					endLine= fDocument.getLineOfOffset(start + fLength);
				} catch (BadLocationException ex) {
				}
				fLineCount= endLine - fLineOffset + 1;
			}

		} else {
			fLength= document.getLength();
			fLineCount= fDocument.getNumberOfLines();
		}
	}

	/**
	 * Returns the number of lines in the document.
	 *
	 * @return number of lines
	 */
	public int getRangeCount() {
		return fLineCount;
	}

	/* (non Javadoc)
	 * see ITokenComparator.getTokenStart
	 */
	public int getTokenStart(int line) {
		try {
			IRegion r= fDocument.getLineInformation(fLineOffset + line);
			return r.getOffset();
		} catch (BadLocationException ex) {
			return fDocument.getLength();
		}
	}

	/* (non Javadoc)
	 * Returns the length of the given line.
	 * see ITokenComparator.getTokenLength
	 */
	public int getTokenLength(int line) {
		return getTokenStart(line+1) - getTokenStart(line);
	}

	/**
	 * Returns <code>true</code> if a line given by the first index
	 * matches a line specified by the other <code>IRangeComparator</code> and index.
	 *
	 * @param thisIndex	the number of the line within this range comparator
	 * @param other the range comparator to compare this with
	 * @param otherIndex the number of the line within the other comparator
	 * @return <code>true</code> if the lines are equal
	 */
	public boolean rangesEqual(int thisIndex, IRangeComparator other0, int otherIndex) {

		if (other0 != null && other0.getClass() == getClass()) {
			DocLineComparator other= (DocLineComparator) other0;

			if (fIgnoreWhiteSpace) {
				String s1= extract(thisIndex);
				String s2= other.extract(otherIndex);
				return compare(s1, s2);
			}

			int tlen= getTokenLength(thisIndex);
			int olen= other.getTokenLength(otherIndex);
			if (tlen == olen) {
				String s1= extract(thisIndex);
				String s2= other.extract(otherIndex);
				return s1.equals(s2);
			}
		}
		return false;
	}

	/**
	 * Aborts the comparison if the number of tokens is too large.
	 *
	 * @return <code>true</code> to abort a token comparison
	 */
	public boolean skipRangeComparison(int length, int max, IRangeComparator other) {
		return false;
	}
		
	//---- private methods
	
	/**
	 * Extract a single line from the underlying document without the line separator.
	 *
	 * @param line the number of the line to extract
	 * @return the contents of the line as a String
	 */
	private String extract(int line) {
		if (line < fLineCount) {
			try {
				IRegion r= fDocument.getLineInformation(fLineOffset + line);
				return fDocument.get(r.getOffset(), r.getLength());
			} catch(BadLocationException e) {
			}
		}
		return ""; //$NON-NLS-1$
	}

	private boolean compare(String s1, String s2) {
		int i1= 0;
		int i2= 0;
		int l1= s1.length();
		int l2= s2.length();
		char c1= ' ';
		char c2= ' ';
		while (i1 < l1 || i2 < l2) {
			if (i1 < l1) {
				c1= s1.charAt(i1);
				if (Character.isWhitespace(c1)) {
					i1++;
					continue;
				}
			}
			if (i2 < l2) {
				c2= s2.charAt(i2);
				if (Character.isWhitespace(c2)) {
					i2++;
					continue;
				}
			}
			if (c1 != c2)
				return false;
			i1++;
			i2++;
		}
		return true;
	}
}

