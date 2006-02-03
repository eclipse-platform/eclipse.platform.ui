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

package org.eclipse.search.ui.text;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;

import org.eclipse.search.core.text.TextSearchMatchAccess;

/**
 * Abstract base class implementing a {@link SearchMatchInformationProvider}. Implementors only have
 * to implement {@link #scanFile(TextSearchMatchAccess)} that calls
 * {@link #addLocation(int, int, int)} for locations and {@link #addLineOffset(int)} for the starting
 * offsets of lines.
 * 
 * @since 3.2
 * 
 * This API is experimental and might be removed before 3.2
 * 
 */
public abstract class AbstractSearchMatchInformationProvider extends SearchMatchInformationProvider {

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

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.SearchMatchPartitioner#reset()
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
	 * 
	 * @param matchAccess provides access to the file to be scanned.
	 */
	protected abstract void scanFile(TextSearchMatchAccess matchAccess);

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
	 * @param offset the offset of the next line
	 */
	protected final void addLineOffset(int offset) {
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
	protected final void addLocation(int offset, int length, int kind) {
		fLocations.add(new Location(offset, length, kind));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.SearchMatchPartitioner#getLineInformation(org.eclipse.search.core.text.TextSearchMatchAccess)
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

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.text.SearchMatchPartitioner#getLineInformation(org.eclipse.search.core.text.TextSearchMatchAccess)
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
