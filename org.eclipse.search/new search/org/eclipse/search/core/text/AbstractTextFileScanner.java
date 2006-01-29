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

package org.eclipse.search.core.text;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;

/**
 * Abstract base class for scanners supplied via the 
 * org.eclipse.search.textFileScanner extension point. 
 * It suffices to implement the scanFile() method. 
 */
public abstract class AbstractTextFileScanner {
	public final static int LOCATION_OTHER= 0;
	public final static int LOCATION_STRING_LITERAL= 1;
	public final static int LOCATION_COMMENT= 2;
	public final static int LOCATION_IMPORT_OR_INCLUDE_STATEMENT= 3;
	public final static int LOCATION_PREPROCESSOR_DIRECTIVE= 4;
	public final static int LOCATION_FUNCTION= 5;

	/**
	 * LocationInfo objects are used to report information about matches.
	 */
	public final static class LineInformation {
		private int fLineNumber;
		private int fLineOffset;
		private int fLineLength;

		public LineInformation(int lineNumber, int lineOffset, int lineLength) {
			fLineNumber= lineNumber;
			fLineOffset= lineOffset;
			fLineLength= lineLength;
		}

		/**
		 * @return the line number where the match starts in.
		 */
		public int getLineNumber() {
			return fLineNumber;
		}

		/**
		 * Returns the total length of the lines containing the match. This also includes
		 * the characters of the line terminator.
		 * @return the length of the lines containing the match.
		 * */
		public int getLineLength() {
			return fLineLength;
		}

		/**
		 * The character offset of the beginning of the line where the match starts.
		 * */
		public int getLineOffset() {
			return fLineOffset;
		}
	}

	private static class Location {
		public Location(int offset, int length, int kind) {
			fOffset= offset;
			fLength= length;
			fKind= kind;
		}
		private int fOffset;
		private int fLength;
		private int fKind;
	}

	private ArrayList fLocations= new ArrayList();
	private int[] fLineOffsets= null;
	private int fLineCount;
	private IFile fFile;
	private int fFileLength;

	/**
	 * Clears any data cached by the scanner. 
	 * Implementors may override this method but must call super.reset();
	 */
	public void reset() {
		fFile= null;
		fLocations.clear();
		fLineOffsets= null;
		fLineCount= 0;
	}

	/**
	 * Called whenever information about a new file is requested. Implementors must
	 * scan the file and report lines and locations with addLineOffset() and addLocation().
	 * @param matchAccess provides access to the file to be scanned.
	 */
	abstract protected void scanFile(TextSearchMatchAccess matchAccess);

	private void checkScanFile(TextSearchMatchAccess match) {
		if (!match.getFile().equals(fFile)) {
			reset();
			scanFile(match);

			// trim line count
			if (fLineCount != 0) {
				int[] offsets= new int[fLineCount];
				System.arraycopy(fLineOffsets, 0, offsets, 0, fLineCount);
				fLineOffsets= offsets;
			}
			fFile= match.getFile();
			fFileLength= match.getFileContentLength();
		}
	}

	/**
	 * Adds the offset of another line to the list of offsets.
	 * Call this method for every line when scanning a file. The first offset that has to
	 * be reported is always 0.
	 */
	final protected void addLineOffset(int offset) {
		if (fLineCount == 0) {
			fLineOffsets= new int[1024];
		} else
			if (fLineCount == fLineOffsets.length) {
				int[] newOffsets= new int[fLineOffsets.length * 2];
				System.arraycopy(fLineOffsets, 0, newOffsets, 0, fLineCount);
				fLineOffsets= newOffsets;
			}
		fLineOffsets[fLineCount++]= offset;
	}

	/**
	 * Adds the location to the list of offsets.
	 * Call this method for every location when scanning a file. 
	 */
	final protected void addLocation(int offset, int length, int kind) {
		fLocations.add(new Location(offset, length, kind));
	}

	/**
	 * Returns information about the lines containing the match provided. 
	 * @param match access to the match.
	 * @return information about the provided match.
	 */
	public LineInformation getLineInformation(TextSearchMatchAccess match) {
		// check if we need to scan another file
		checkScanFile(match);
		return getLineInformation(match.getMatchOffset(), match.getMatchLength());
	}

	private LineInformation getLineInformation(int offset, int length) {
		if (fLineOffsets != null) {
			final int lineIdx= getLineIndexForOffset(offset);
			final int lineOffset= fLineOffsets[lineIdx];

			int endLineIdx= getLineIndexForOffset(offset + length - 1);
			int lineLength;
			if (endLineIdx < fLineOffsets.length - 1) {
				lineLength= fLineOffsets[endLineIdx + 1] - lineOffset;
			} else {
				lineLength= fFileLength - lineOffset;
			}
			return new LineInformation(lineIdx + 1, lineOffset, lineLength);
		}
		return new LineInformation(0, 0, 0);
	}

	private int getLineIndexForOffset(int offset) {
		int lineIdx= Arrays.binarySearch(fLineOffsets, offset);
		if (lineIdx < 0) {
			lineIdx= Math.max(0, -lineIdx - 2);
		}
		return lineIdx;
	}

	/**
	 * Returns the kind of location the given match starts in.
	 * @param match access to the match.
	 * @return one of LOCATION_...
	 */
	public int getLocationKind(TextSearchMatchAccess match) {
		checkScanFile(match);
		return getLocationKind(match.getMatchOffset());
	}

	private int getLocationKind(int offset) {
		int low= 0;
		int high= fLocations.size() - 1;

		while (low <= high) {
			int mid= (low + high) / 2;
			Location loc= (Location) fLocations.get(mid);
			int locOffset= loc.fOffset;
			if (offset >= locOffset) {
				if (offset < locOffset + loc.fLength) {
					return loc.fKind;
				}
				// offset is larger
				low= mid + 1;
			} else {
				// offset is smaller
				high= mid - 1;
			}
		}
		return 0;
	}
}
