/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.patch;

import java.util.List;

import org.eclipse.jface.util.Assert;


/* package */ class Hunk {
	int fOldStart, fOldLength;
	int fNewStart, fNewLength;
	String[] fLines;
	
	/* package */ Hunk(int[] oldRange, int[] newRange, List lines) {
		if (oldRange[0] > 0)
			fOldStart= oldRange[0]-1;
		else
			fOldStart= 0;
		fOldLength= oldRange[1];
		if (newRange[0] > 0)
			fNewStart= newRange[0]-1;
		else
			fNewStart= 0;
		fNewLength= newRange[1];
		
		fLines= (String[]) lines.toArray(new String[lines.size()]);
	}
	
	/**
	 * Returns the contents of this hunk.
	 * Each line starts with a control character. Their meaning is a s follows:
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
		for (int i= 0; i < fLines.length; i++)
			sb.append(fLines[i]);
		return sb.toString();
	}
	
	/**
	 * Returns a descriptive String for this hunk.
	 * It is in the form old_start,old_length -> new_start,new_length.
	 */
	String getDescription() {
		StringBuffer sb= new StringBuffer();
		sb.append(Integer.toString(fOldStart));
		sb.append(',');
		sb.append(Integer.toString(fOldLength));
		sb.append(" -> ");
		sb.append(Integer.toString(fNewStart));
		sb.append(',');
		sb.append(Integer.toString(fNewLength));
		return sb.toString();
	}
	
	/**
	 * Compares two strings without taking the line endings into account.
	 * Supported line endings are "\n", "\r", and "\r\n".
	 */
	/* package */ static boolean equals(String line1, String line2) {
		
		int l1= line1.length();
		if (l1 > 0 && line1.charAt(l1-1) == '\n')
			l1--;
		if (l1 > 1 && line1.charAt(l1-2) == '\r')
			l1--;
			
		int l2= line2.length();
		if (l2 > 0 && line2.charAt(l2-1) == '\n')
			l2--;
		if (l2 > 1 && line2.charAt(l2-2) == '\r')
			l2--;

		if (l1 != l2)
			return false;
		return line1.regionMatches(0, line2, 0, l1);
	}
	
	/* package */ int patch(List lines, int shift) {
		if (tryPatch(lines, shift)) {
			shift+= doPatch(lines, shift);
		} else {
			boolean found= false;
			int oldShift= shift;
			
			for (int i= shift-1; i > shift-3; i--) {
				if (tryPatch(lines, i)) {
					shift= i;
					found= true;
					break;
				}
			}
			
			if (! found) {
				for (int i= shift+1; i < shift+3; i++) {
					if (tryPatch(lines, i)) {
						shift= i;
						found= true;
						break;
					}
				}
			}
			
			if (found) {
				//System.out.println("patched hunk at offset: " + (shift-oldShift));
				shift+= doPatch(lines, shift);
			} else {
				System.out.println("hunk ignored");
			}
		}
		return shift;
	}
		
	private boolean tryPatch(List lines, int shift) {
		int pos= fOldStart + shift;
		int contextMatches= 0;
		int deleteMatches= 0;
		for (int i= 0; i < fLines.length; i++) {
			String s= fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			switch (s.charAt(0)) {
			case ' ':	// context lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (equals(line, (String) lines.get(pos))) {
						contextMatches++;
						pos++;
						break;
					}
					if (contextMatches <= 0)
						return false;
					pos++;
				}
				break;
			case '-':	// deleted lines
				while (true) {
					if (pos < 0 || pos >= lines.size())
						return false;
					if (equals(line, (String) lines.get(pos))) {
						deleteMatches++;
						pos++;
						break;
					}
					if (deleteMatches <= 0)
						return false;
					pos++;
				}
				break;
			case '+':	// added lines
				break;
			}
		}
		return true;
	}
	
	private int doPatch(List lines, int shift) {
		int pos= fOldStart + shift;
		for (int i= 0; i < fLines.length; i++) {
			String s= fLines[i];
			Assert.isTrue(s.length() > 0);
			String line= s.substring(1);
			char type= s.charAt(0);
			switch (type) {
			case ' ':	// context lines
				while (true) {
					Assert.isTrue(pos < lines.size(), "3");
					if (equals(line, (String) lines.get(pos))) {
						pos++;
						break;
					}
					pos++;
				}
				break;
			case '-':	// deleted lines
				while (true) {
					Assert.isTrue(pos < lines.size(), "3");
					if (equals(line, (String) lines.get(pos))) {
						break;
					}
					pos++;
				}
				lines.remove(pos);
				break;			
			case '+':	// added lines
				lines.add(pos,  line);
				pos++;
				break;
			}
		}
		return fNewLength - fOldLength;
	}
	
	/* package */ void dump() {
		System.out.println("  " + getDescription());
		//for (int i= 0; i < fLines.length; i++)
		//	System.out.println("    " + fLines[i]);
	}
}
