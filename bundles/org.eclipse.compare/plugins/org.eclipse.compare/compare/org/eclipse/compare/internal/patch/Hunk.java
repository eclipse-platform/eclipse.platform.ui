/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.List;

import org.eclipse.compare.patch.PatchConfiguration;
import org.eclipse.core.runtime.Assert;

/**
 * A Hunk describes a range of changed lines and some context lines.
 */
public class Hunk {
	
	public static final int ADDED = 0x1;
	public static final int DELETED = 0x2;
	public static final int CHANGED = 0x4;
	public static final int UNKNOWN = 0x8;
	
	private FileDiff fParent;
	private int fOldStart, fOldLength;
	private int fNewStart, fNewLength;
	private String[] fLines;
	private int hunkType;
	
    public Hunk(FileDiff parent, Hunk toCopy) {
        fParent = parent;
        if (fParent != null) {
            fParent.add(this);
        }
        
        fOldStart = toCopy.fOldStart;
        fOldLength = toCopy.fOldLength;
        fNewStart = toCopy.fNewStart;
        fNewLength = toCopy.fOldLength;
        fLines = toCopy.fLines;
        hunkType = toCopy.hunkType;
    }
    
	public Hunk(FileDiff parent, int[] oldRange, int[] newRange, List lines, boolean encounteredPlus, boolean encounteredMinus, boolean encounteredSpace) {
		
		fParent= parent;
		if (fParent != null)
			fParent.add(this);
		
		if (oldRange[0] > 0)
			fOldStart= oldRange[0]-1;	// line number start at 0!
		else
			fOldStart= 0;
		fOldLength= oldRange[1];
		if (newRange[0] > 0)
			fNewStart= newRange[0]-1;	// line number start at 0!
		else
			fNewStart= 0;
		fNewLength= newRange[1];
		
		fLines= (String[]) lines.toArray(new String[lines.size()]);
		
		if (encounteredSpace && (encounteredPlus || encounteredMinus)){
			hunkType = CHANGED;
		} else if (encounteredPlus && !encounteredMinus && !encounteredSpace){
			hunkType = ADDED;
		} else if (!encounteredPlus && encounteredMinus && !encounteredSpace) { 
			hunkType = DELETED;
		} else {
			hunkType = UNKNOWN;
		}
	}

	/*
	 * Returns the contents of this hunk.
	 * Each line starts with a control character. Their meaning is as follows:
	 * <ul>
	 * <li>
	 * '+': add the line
	 * <li>
	 * '-': delete the line
	 * <li>
	 * ' ': no change, context line
	 * </ul>
	 */
	String getContent() {
		StringBuffer sb= new StringBuffer();
		for (int i= 0; i < fLines.length; i++) {
			String line= fLines[i];
			sb.append(line.substring(0, Patcher.length(line)));
			sb.append('\n');
		}
		return sb.toString();
	}
	
	/*
	 * Returns a descriptive String for this hunk.
	 * It is in the form old_start,old_length -> new_start,new_length.
	 */
	String getDescription() {
		StringBuffer sb= new StringBuffer();
		sb.append(Integer.toString(fOldStart));
		sb.append(',');
		sb.append(Integer.toString(fOldLength));
		sb.append(" -> "); //$NON-NLS-1$
		sb.append(Integer.toString(fNewStart));
		sb.append(',');
		sb.append(Integer.toString(fNewLength));
		return sb.toString();
	}
	
	String getRejectedDescription() {
		StringBuffer sb= new StringBuffer();
		sb.append("@@ -"); //$NON-NLS-1$
		sb.append(Integer.toString(fOldStart));
		sb.append(',');
		sb.append(Integer.toString(fOldLength));
		sb.append(" +"); //$NON-NLS-1$
		sb.append(Integer.toString(fNewStart));
		sb.append(',');
		sb.append(Integer.toString(fNewLength));
		sb.append(" @@"); //$NON-NLS-1$
		return sb.toString();
	}
	
	int getHunkType(boolean reverse) {
		if (reverse) {
			if (hunkType == ADDED)
				return DELETED;
			if (hunkType == DELETED)
				return ADDED;
		}
		return hunkType;
	}

	void setHunkType(int hunkType) {
		this.hunkType = hunkType;
	}

	public String[] getLines() {
		return fLines;
	}

	/**
	 * Set the parent of this hunk. This method
	 * should only be invoked from {@link FileDiff#add(Hunk)}
	 * @param diff the parent of this hunk
	 */
	void setParent(FileDiff diff) {
		if (fParent == diff)
			return;
		if (fParent != null)
			fParent.remove(this);
		fParent = diff;	
	}

	public FileDiff getParent() {
		return fParent;
	}
	
	/*
	 * Tries to apply the given hunk on the specified lines.
	 * The parameter shift is added to the line numbers given
	 * in the hunk.
	 */
	public boolean tryPatch(PatchConfiguration configuration, List lines, int shift) {
		boolean reverse = configuration.isReversed();
		int pos= getStart(reverse) + shift;
		int deleteMatches= 0;
		for (int i= 0; i < fLines.length; i++) {
			String s= fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			char controlChar= s.charAt(0);
			if (controlChar == ' ') {	// context lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (linesMatch(configuration, line, (String) lines.get(pos))) {
						pos++;
						break;
					}
					return false;
				}
			} else if (isDeletedDelimeter(controlChar, reverse)) {
				// deleted lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (linesMatch(configuration, line, (String) lines.get(pos))) {
						deleteMatches++;
						pos++;
						break;
					}
					
					// We must remove all lines at once, return false if this
					// fails. In other words, all lines considered for deletion
					// must be found one by one.

					// if (deleteMatches <= 0)
						return false;
					// pos++;
				}
			} else if (isAddedDelimeter(controlChar, reverse)) {
				// added lines
				// we don't have to do anything for a 'try'
			} else
				Assert.isTrue(false, "tryPatch: unknown control character: " + controlChar); //$NON-NLS-1$
		}
		return true;
	}
	
	int getStart(boolean reverse) {
		if (reverse) {
			return fNewStart;
		}
		return fOldStart;
	}
	
	private int getLength(boolean reverse) {
		if (reverse) {
			return fNewLength;
		}
		return fOldLength;
	}
	
	private int getShift(boolean reverse) {
		if (reverse) {
			return fOldLength - fNewLength;
		}
		return fNewLength - fOldLength;
	}
	
	int doPatch(PatchConfiguration configuration, List lines, int shift) {
		boolean reverse = configuration.isReversed();
		int pos = getStart(reverse) + shift;
		for (int i= 0; i < fLines.length; i++) {
			String s= fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			char controlChar= s.charAt(0);
			if (controlChar == ' ') {	// context lines
				while (true) {
					Assert.isTrue(pos < lines.size(), "doPatch: inconsistency in context"); //$NON-NLS-1$
					if (linesMatch(configuration, line, (String) lines.get(pos))) {
						pos++;
						break;
					}
					pos++;
				}
			} else if (isDeletedDelimeter(controlChar, reverse)) {
				// deleted lines				
				while (true) {
					Assert.isTrue(pos < lines.size(), "doPatch: inconsistency in deleted lines"); //$NON-NLS-1$
					if (linesMatch(configuration, line, (String) lines.get(pos))) {
						break;
					}
					pos++;
				}
				lines.remove(pos);
			} else if (isAddedDelimeter(controlChar, reverse)) {
				// added lines
				if (getLength(reverse) == 0 && pos+1 < lines.size())
					lines.add(pos+1, line);
				else
					lines.add(pos, line);
				pos++;
			} else
				Assert.isTrue(false, "doPatch: unknown control character: " + controlChar); //$NON-NLS-1$
		}
		return getShift(reverse);
	}

	private boolean isDeletedDelimeter(char controlChar, boolean reverse) {
		return (!reverse && controlChar == '-') || (reverse && controlChar == '+');
	}
	
	private boolean isAddedDelimeter(char controlChar, boolean reverse) {
		return (reverse && controlChar == '-') || (!reverse && controlChar == '+');
	}
	
	/*
	 * Compares two strings.
	 * If fIgnoreWhitespace is true whitespace is ignored.
	 */
	private boolean linesMatch(PatchConfiguration configuration, String line1, String line2) {
		if (configuration.isIgnoreWhitespace())
			return stripWhiteSpace(line1).equals(stripWhiteSpace(line2));
		if (isIgnoreLineDelimiter()) {
			int l1= Patcher.length(line1);
			int l2= Patcher.length(line2);
			if (l1 != l2)
				return false;
			return line1.regionMatches(0, line2, 0, l1);
		}
		return line1.equals(line2);
	}
	
	private boolean isIgnoreLineDelimiter() {
		return true;
	}

	/*
	 * Returns the given string with all whitespace characters removed.
	 * Whitespace is defined by <code>Character.isWhitespace(...)</code>.
	 */
	private String stripWhiteSpace(String s) {
		StringBuffer sb= new StringBuffer();
		int l= s.length();
		for (int i= 0; i < l; i++) {
			char c= s.charAt(i);
			if (!Character.isWhitespace(c))
				sb.append(c);
		}
		return sb.toString();
	}
	
	public String getContents(boolean isAfterState, boolean reverse) {
		StringBuffer result= new StringBuffer();
		for (int i= 0; i<fLines.length; i++) {
			String line= fLines[i];
			String rest= line.substring(1);
			char c = line.charAt(0);
			if (c == ' ') {
				result.append(rest);
			} else if (isDeletedDelimeter(c, reverse) && !isAfterState) {
				result.append(rest);	
			} else if (isAddedDelimeter(c, reverse) && isAfterState) {
				result.append(rest);
			}
		}
		return result.toString();
	}
}
