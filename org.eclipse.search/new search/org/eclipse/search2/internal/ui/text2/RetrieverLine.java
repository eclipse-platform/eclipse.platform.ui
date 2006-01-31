/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;


/**
 * @author markus.schorn@windriver.com
 */
public class RetrieverLine {
	private static final RetrieverMatch[] EMPTY_ARRAY= new RetrieverMatch[0];
	private static final int HIGH_BIT= 1 << 31;

	private IFile fParent;
	private int fLineNumber;
	private String fLineData;
	private RetrieverMatch[] fMatches;

	public RetrieverLine(IFile parent, int lineNumber) {
		fParent= parent;
		fLineNumber= lineNumber;
		fMatches= null;
		setIsFiltered(false);
	}
	public int getLineNumber() {
		return fLineNumber & ~HIGH_BIT;
	}
	public void setData(String line) {
		fLineData= line;
	}

	synchronized public void addMatch(RetrieverMatch match) {
		if (fMatches == null) {
			fMatches= new RetrieverMatch[] {match};
		} else {
			RetrieverMatch[] newMatches= new RetrieverMatch[fMatches.length + 1];
			System.arraycopy(fMatches, 0, newMatches, 0, fMatches.length);
			newMatches[fMatches.length]= match;
			fMatches= newMatches;
		}
	}

	synchronized public void filter(Pattern regex, boolean hideMatching, int acceptLocations, Collection changedMatches) {
		boolean lineWasFiltered= isFiltered();
		boolean lineIsFiltered= regex != null && regex.matcher(fLineData).find() == hideMatching;
		for (int i= 0; i < fMatches.length; i++) {
			RetrieverMatch match= fMatches[i];
			boolean wasFiltered= lineWasFiltered || match.isFiltered();
			boolean doFilter= lineIsFiltered || ((match.getKind() & acceptLocations) == 0);
			if (wasFiltered != doFilter) {
				if (changedMatches != null) {
					changedMatches.add(match);
				}
				match.setFiltered(doFilter);
			}
		}
		setIsFiltered(lineIsFiltered);
	}

	private void setIsFiltered(boolean value) {
		if (value) {
			fLineNumber|= HIGH_BIT;
		} else {
			fLineNumber&= ~HIGH_BIT;
		}
	}
	public String getLine() {
		return fLineData;
	}

	public RetrieverMatch[] getMatches(boolean copy) {
		return copy ? (RetrieverMatch[]) fMatches.clone() : fMatches;
	}

	public IFile getParent() {
		return fParent;
	}

	synchronized public void addMatchCount(int[] count) {
		count[0]+= fMatches.length;
		if (!isFiltered()) {
			for (int i= 0; i < fMatches.length; i++) {
				if (!fMatches[i].isFiltered()) {
					count[1]++;
				}
			}
		}
	}

	public int getLength() {
		return fLineData.length();
	}

	public String getString() {
		return getPreString(null);
	}

	synchronized public String getPreString(RetrieverMatch match) {
		StringBuffer pre= new StringBuffer();
		int offset= 0;
		int lineLength= fLineData.length();
		for (int i= 0; i < fMatches.length; i++) {
			RetrieverMatch m= fMatches[i];
			if (m == match) {
				break;
			}
			if (m.isReplaced()) {
				int start= m.getLineOffset();
				if (start >= lineLength) {
					break;
				}
				pre.append(fLineData.substring(offset, start));
				pre.append(m.getReplacement());
				offset= start + m.getOriginalLength();
				if (offset >= lineLength) {
					break;
				}
			}
			if (m == match) {
				break;
			}
		}
		if (offset < lineLength) {
			int end= match == null ? lineLength : Math.min(match.getLineOffset(), lineLength);
			pre.append(fLineData.substring(offset, end));
		}
		return pre.toString();
	}

	synchronized public String computeReplacement(RetrieverMatch match, Pattern p, String replacement) {
		if (match.getLineOffset() + match.getOriginalLength() > fLineData.length()) {
			return null;
		}
		RetrieverMatch prev= null;
		for (int i= 0; i < fMatches.length; i++) {
			RetrieverMatch m= fMatches[i];
			if (m == match) {
				break;
			}
			prev= m;
		}
		// find a good point to start the find
		int offsetInLine= prev == null ? 0 : prev.getLineOffset() + prev.getOriginalLength();
		Matcher matcher= p.matcher(fLineData);
		if (matcher.find(offsetInLine)) {
			int shouldStart= match.getLineOffset();
			if (shouldStart == matcher.start()) {
				StringBuffer help= new StringBuffer();
				try {
					matcher.appendReplacement(help, replacement);
				} catch (Exception e) {
					return replacement.replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$//$NON-NLS-2$
				}
				return help.substring(shouldStart);
			}
		}
		return null;
	}

	synchronized public String getPostString(RetrieverMatch match) {
		StringBuffer post= new StringBuffer();
		int offset= 0;
		int lineLength= fLineData.length();
		for (int i= 0; i < fMatches.length; i++) {
			RetrieverMatch m= fMatches[i];
			if (m == match) {
				offset= match.getLineOffset() + match.getOriginalLength();
			} else
				if (offset > 0) {
					if (m.isReplaced()) {
						int start= m.getLineOffset();
						if (start >= lineLength) {
							break;
						}
						post.append(fLineData.substring(offset, start));
						post.append(m.getReplacement());
						offset= start + m.getOriginalLength();
						if (offset >= lineLength) {
							break;
						}
					}
				}
		}
		if (offset < lineLength) {
			post.append(fLineData.substring(offset, lineLength));
		}
		return post.toString();
	}

	public void setParent(IFile file) {
		fParent= file;
	}

	synchronized public boolean checkState(boolean replace) {
		for (int i= 0; i < fMatches.length; i++) {
			RetrieverMatch match= fMatches[i];
			if (!match.isFiltered() && replace != match.isReplaced()) {
				return true;
			}
		}
		return false;
	}

	synchronized public void remove(Set matchset) {
		ArrayList matches= new ArrayList();
		for (int i= 0; i < fMatches.length; i++) {
			Object m= fMatches[i];
			if (!matchset.contains(m)) {
				matches.add(m);
			}
		}
		fMatches= (RetrieverMatch[]) matches.toArray(new RetrieverMatch[matches.size()]);
	}

	synchronized public int getDisplayedMatchCount() {
		int result= 0;
		if (!isFiltered()) {
			for (int i= 0; i < fMatches.length; i++) {
				if (!fMatches[i].isFiltered()) {
					result++;
				}
			}
		}
		return result;
	}

	synchronized public RetrieverMatch[] getDisplayedMatches() {
		int count= getDisplayedMatchCount();
		if (count == 0) {
			return EMPTY_ARRAY;
		}
		if (count == fMatches.length) {
			return fMatches;
		}
		RetrieverMatch[] result= new RetrieverMatch[count];
		int j= 0;
		for (int i= 0; i < fMatches.length; i++) {
			RetrieverMatch match= fMatches[i];
			if (!match.isFiltered()) {
				result[j++]= match;
			}
		}
		return result;
	}

	public boolean isFiltered() {
		return (fLineNumber & HIGH_BIT) != 0;
	}

	public int getMatchCount() {
		return 0;
	}

	public String substring(int begin, int end) {
		return fLineData.substring(begin, end);
	}
}
