/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.util.StringTokenizer;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;

/**
 * Implements the <code>ITokenComparator</code> interface for words (or tokens) in a string.
 * A <code>TokenComparator</code> is used as the input for the <code>RangeDifferencer</code>
 * engine to perform a token oriented compare on strings.
 */
public class TokenComparator implements ITokenComparator {

	private boolean fShouldEscape= true;
	private String fText;
	private int fCount;
	private int[] fStarts;
	private int[] fLengths;

	/**
	 * Creates a <code>TokenComparator</code> for the given string.
	 *
	 * @param string the string that is split into token
	 */
	public TokenComparator(String string) {
		
		if (string != null)
			fText= string;
		else
			fText= ""; //$NON-NLS-1$
		
		fStarts= new int[fText.length()];
		fLengths= new int[fText.length()];
		fCount= 0;
		
		StringTokenizer tokenizer= new StringTokenizer(fText, " \t\n\r", true); //$NON-NLS-1$
		
		for (int pos= 0; tokenizer.hasMoreElements();) {
			fStarts[fCount]= pos;
			String s= tokenizer.nextToken();
			int l= 0;
			if (s != null)
				l= s.length();
			pos += l;
			fLengths[fCount]= l;
			fCount++;
		}
	}

	/**
	 * Creates a <code>TokenComparator</code> for the given string.
	 *
	 * @param string the string that is split into token
	 * @param shouldEscape
	 */
	public TokenComparator(String s, boolean shouldEscape) {
		this(s);
		fShouldEscape= shouldEscape;
	}

	/**
	 * Returns the number of token in the string.
	 *
	 * @return number of token in the string
	 */
	public int getRangeCount() {
		return fCount;
	}

	/* (non Javadoc)
	 * see ITokenComparator.getTokenStart
	 */
	public int getTokenStart(int index) {
		if (index < fCount)
			return fStarts[index];
		return fText.length();
	}

	/* (non Javadoc)
	 * see ITokenComparator.getTokenLength
	 */
	public int getTokenLength(int index) {
		if (index < fCount)
			return fLengths[index];
		return 0;
	}
		
	/**
	 * Returns the content of tokens in the specified range as a String.
	 * If the number of token is 0 the empty string ("") is returned.
	 *
	 * @param start index of first token
	 * @param length number of tokens
	 * @return the contents of the specified token range as a String
	 */
//	public String extract(int start, int length) {
//		int startPos= fStarts[start];
//		int endPos= 0;
//		if (length > 0) {
//			int e= start + length-1;
//			endPos= fStarts[e] + fLengths[e];
//		} else {
//			endPos= fStarts[start];
//		}
//		//int endPos= getTokenStart(start + length);
//		if (endPos >= fText.length())
//			return fText.substring(startPos);
//		return fText.substring(startPos, endPos);
//	}

	/**
	 * Returns <code>true</code> if a token given by the first index
	 * matches a token specified by the other <code>IRangeComparator</code> and index.
	 *
	 * @param thisIndex	the number of the token within this range comparator
	 * @param other the range comparator to compare this with
	 * @param otherIndex the number of the token within the other comparator
	 * @return <code>true</code> if the token are equal
	 */
	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
		if (other != null && getClass() == other.getClass()) {
			TokenComparator tc= (TokenComparator) other;
			int thisLen= getTokenLength(thisIndex);
			int otherLen= tc.getTokenLength(otherIndex);
			if (thisLen == otherLen)
				return fText.regionMatches(false, getTokenStart(thisIndex), tc.fText, tc.getTokenStart(otherIndex), thisLen);
		}
		return false;
	}

	/**
	 * Aborts the comparison if the number of tokens is too large.
	 *
	 * @return <code>true</code> to abort a token comparison
	 */
	public boolean skipRangeComparison(int length, int max, IRangeComparator other) {

		if (!fShouldEscape)
			return false;

		if (getRangeCount() < 50 || other.getRangeCount() < 50)
			return false;

		if (max < 100)
			return false;

		if (length < 100)
			return false;

		if (max > 800)
			return true;

		if (length < max / 4)
			return false;

		return true;
	}
		
//	public static void main(String args[]) {
//		//String in= "private static boolean isWhitespace(char c) {";
//		//String in= "for (int j= 0; j < l-1; j++) {";
//		String in= "for do";
//		TokenComparator tc= new TokenComparator(in, false);
//		
//		System.out.println("n: " + tc.getRangeCount());
//		System.out.println(in);
//		
//		int p= 0;
//		for (int i= 0; i < tc.getRangeCount(); i++) {
//			int l= tc.getTokenLength(i);
//			System.out.print("<");
//			
//			for (int j= 0; j < l-1; j++)
//				System.out.print(" ");
//		}
//		System.out.println();
//		
//		//System.out.println("extract: <" + tc.extract(16, 1) + ">");
//	}
}
