package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.text.Collator;
import java.util.Vector;

public class VersionCollator {
	static final int WORD = 0;
	static final int NUMBER = 1;
	
	public int compare(String version1, String version2) {
		if (version1 == null && version2 == null) return 0;
		if (version1 == null) return -1;
		if (version2 == null) return 1;
		String[] version1Segments = getStringSegments(version1);
		String[] version2Segments = getStringSegments(version2);
		Collator collator = Collator.getInstance();
		for (int i = 0; i < version1Segments.length && i < version2Segments.length; i++) {
			int oneType = isNumber(version1Segments[i]) ? NUMBER : WORD;
			int twoType = isNumber(version2Segments[i]) ? NUMBER : WORD;
			if (oneType != twoType) {
				return collator.compare(version1, version2);
			}
			if (oneType == NUMBER) {
				int intOne = Integer.parseInt(version1Segments[i]);
				int intTwo = Integer.parseInt(version2Segments[i]);
				if (intOne != intTwo) return (intOne < intTwo) ? -1 : 1;
			} else {
				int result = collator.compare(version1Segments[i], version2Segments[i]);
				if (result != 0) return result;
			}
		}
		if (version1Segments.length != version2Segments.length) {
			return version1Segments.length < version2Segments.length ? -1 : 1;
		}
		return 0;
	}
	String[] getStringSegments(String string) {
		int size = string.length();
		if (size == 0) return new String[0];
		StringBuffer buffer = new StringBuffer();
		Vector vector = new Vector();
		int current = Character.isDigit(string.charAt(0)) ? NUMBER : WORD;
		for (int i = 0; i < size; i++) {
			char ch = string.charAt(i);
			int newCurrent = Character.isDigit(string.charAt(i)) ? NUMBER : WORD;
			if (newCurrent != current) {
				vector.addElement(buffer.toString());
				buffer = new StringBuffer();
				current = newCurrent;
			}
			buffer.append(ch);
		}
		vector.addElement(buffer.toString());
		String[] result = new String[vector.size()];
		vector.toArray(result);
		return result;
	}
	boolean isNumber(String string) {
		return Character.isDigit(string.charAt(0));
	}
}

