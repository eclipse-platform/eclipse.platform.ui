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
package org.eclipse.compare.internal;

import org.eclipse.jface.util.Assert;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;

/**
 * Implements the <code>ITokenComparator</code> interface for words (or tokens)
 * in a string.
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
	 * @param text the string that is split into token
	 */
	public TokenComparator(String text) {
		
		Assert.isNotNull(text);

		fText= text;
		
		int length= fText.length();
		fStarts= new int[length];	// pessimistic assumption!
		fLengths= new int[length];
		fCount= 0;
		
		char lastCategory= 0;	// 0: no category
		for (int i= 0; i < length; i++) {
			char c= fText.charAt(i);
			
			char category= '?';	// unspecified category
			if (Character.isWhitespace(c))
				category= ' ';	// white space category
			else if (Character.isDigit(c))
				category= '0';	// digits
			else if (Character.isLetter(c))
				category= 'a';	// letters
			
			if (category != lastCategory) {
				// start a new token
				fStarts[fCount++]= i;
				lastCategory= category;
			}
			fLengths[fCount-1]++;
		}
	}
	
	/**
	 * Creates a <code>TokenComparator</code> for the given string.
	 *
	 * @param text the string that is split into token
	 * @param shouldEscape
	 */
	public TokenComparator(String text, boolean shouldEscape) {
		this(text);
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

	/*
	 * Aborts the comparison if the number of tokens is too large.
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
//		String in= "for do    i= 123; i++";
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
