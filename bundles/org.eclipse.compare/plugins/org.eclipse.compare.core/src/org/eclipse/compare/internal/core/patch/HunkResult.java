/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.core.patch;

import java.io.InputStream;
import java.util.List;

import org.eclipse.compare.patch.IHunk;
import org.eclipse.compare.patch.PatchConfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class HunkResult implements IHunk {

	private static final boolean DEBUG= false;

	/**
	 * Default maximum fuzz factor equals 2. This is related to the default
	 * number of context lines, which is 3.
	 */
	private static final int MAXIMUM_FUZZ_FACTOR = 2;
	
	private Hunk fHunk;
	private boolean fMatches;
	private int fShift;
	private int fFuzz = -1; // not set or couldn't be found

	private final FileDiffResult fDiffResult;

	/**
	 * Create a hunk result for the given hunk
	 * @param diffResult the parent diff result
	 * @param hunk the hunk
	 */
	public HunkResult(FileDiffResult diffResult, Hunk hunk) {
		fDiffResult = diffResult;
		fHunk = hunk;
	}

	/**
	 * Try to apply the specified hunk to the given lines.
	 * If the hunk cannot be applied at the original position
	 * the method tries shift lines up and down.
	 * @param lines the lines to be patched
	 * @return whether the hunk could be applied
	 */
	public boolean patch(List lines) {
		fMatches = false;
		PatchConfiguration configuration = getConfiguration();
		// if the fuzz is not set for the current hunk use the one from fDiffResult
		int fuzz = fFuzz != -1 ? fFuzz : configuration.getFuzz();
		if (isEnabled(configuration)) {
			if (fHunk.tryPatch(configuration, lines, fShift, fuzz)) {
				// it's a perfect match, no shifting is needed
				fShift += fHunk.doPatch(configuration, lines, fShift, fuzz);
				fMatches = true;
			} else {
				boolean found= false;
				int oldShift= fShift;
				
				int hugeShift = lines.size();
				for (int i = 1; i <= hugeShift; i++) {
					if (fHunk.tryPatch(configuration, lines, fShift - i, fuzz)) {
						if (isAdjustShift())
							fShift -= i;
						found = true;
						break;
					}
				}
				
				if (!found) {
					for (int i = 1; i <= hugeShift; i++) {
						if (fHunk.tryPatch(configuration, lines, fShift + i, fuzz)) {
							if (isAdjustShift())
								fShift += i;
							found = true;
							break;
						}
					}
				}
				
				if (found) {
					if (DEBUG) System.out.println("patched hunk at offset: " + (fShift-oldShift)); //$NON-NLS-1$
					fShift+= fHunk.doPatch(configuration, lines, fShift, fuzz);
					fMatches = true;
				}
			}
		}
		return fMatches;
	}

	private boolean isAdjustShift() {
		return true;
	}

	private PatchConfiguration getConfiguration() {
		return getDiffResult().getConfiguration();
	}

	/**
	 * Calculate the fuzz that will allow the most hunks to be matched. Even
	 * though we're interested only in the value of the fuzz, the shifting is
	 * done anyway.
	 * 
	 * @param lines
	 *            the lines of the target file
	 * @param monitor
	 *            a progress monitor
	 * @return the fuzz factor or -1 if the hunk could not be matched
	 */
	public int calculateFuzz(List lines, IProgressMonitor monitor) {
		fMatches = false;
		PatchConfiguration configuration = getConfiguration();
		int fuzz = 0;
		int maxFuzz = configuration.getFuzz() == -1 ? MAXIMUM_FUZZ_FACTOR
				: configuration.getFuzz();
		for (; fuzz <= maxFuzz; fuzz++) {
			// try to apply using lines coordinates from the patch
			if (fHunk.tryPatch(configuration, lines, fShift, fuzz)) {
				// it's a perfect match, no adjustment is needed
				fShift += fHunk.doPatch(configuration, lines, fShift, fuzz);
				fMatches = true;
				break;
			}
			
			// TODO (tzarna): hugeShift=lines.size() is more than we need.
			// Lines to the beg/end of a file would be enough but this can still
			// in matching hunks out of order. Try to shift using only lines
			// available "between" hunks.
			int hugeShift = lines.size(); 
			
			// shift up 
			for (int i = 1; i <= hugeShift; i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				if (fHunk.tryPatch(configuration, lines, fShift - i, fuzz)) {
					if (isAdjustShift())
						fShift -= i;
					fMatches = true;
					break;
				}
			}

			// shift down
			if (!fMatches) {
				for (int i = 1; i <= hugeShift; i++) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					if (fHunk.tryPatch(configuration, lines, fShift + i, fuzz)) {
						if (isAdjustShift())
							fShift += i;
						fMatches = true;
						break;
					}
				}
			}

			if (fMatches) {
				fShift += fHunk.doPatch(configuration, lines, fShift, fuzz);
				break;
			}
		}
		// set fuzz for the current hunk
		fFuzz = fMatches ? fuzz : -1;
		return fFuzz;
	}

	/**
	 * Return the amount that this hunk should be shifted when a match with the file
	 * is attempted. The shift is needed to compensate for previous hunks that have
	 * been applied.
	 * @return the amount that this hunk should be shifted when applied
	 */
	public int getShift() {
		return fShift;
	}

	/**
	 * Set the amount that this hunk should be shifted when a match with the file
	 * is attempted. The shift is needed to compensate for previous hunks that have
	 * been applied.
	 * @param shift the amount to shift this hunk
	 */
	public void setShift(int shift) {
		fShift = shift;
	}

	/**
	 * Return the hunk to which this result applies.
	 * @return the hunk to which this result applies
	 */
	public Hunk getHunk() {
		return fHunk;
	}

	/**
	 * Return the parent diff result.
	 * @return the parent diff result
	 */
	public FileDiffResult getDiffResult() {
		return fDiffResult;
	}

	/**
	 * Return whether the hunk was matched with the target file.
	 * @return whether the hunk was matched with the target file
	 */
	public boolean isOK() {
		return fMatches;
	}

	/**
	 * Return the contents that should be displayed for the hunk result.
	 * @param afterState whether the after state or before state of the hunk is desired
	 * @param fullContext whether the hunk should be displayed with the entire file or
	 * only the lines in the hunk itself
	 * @return the contents to be display
	 */
	public String getContents(boolean afterState, boolean fullContext) {
		if (fullContext) {
			boolean problemFound = false;
			List lines = getDiffResult().getBeforeLines();
			if (afterState) {
				if (isOK()) {
					int oldShift = fShift;
					try {
						fShift = 0;
						problemFound = !patch(lines);
					} finally {
						fShift = oldShift;
					}
				} else {
					problemFound = true;
				}
			}
			// Only return the full context if we could apply the hunk
			if (!problemFound)
				return LineReader.createString(fDiffResult.isPreserveLineDelimeters(), lines);
		}
		return getHunk().getContents(afterState, getConfiguration().isReversed());
	}
	
	private boolean isEnabled(PatchConfiguration configuration) {
		Object property = configuration.getProperty(IHunkFilter.HUNK_FILTER_PROPERTY);
		if (property instanceof IHunkFilter) {
			IHunkFilter filter = (IHunkFilter) property;
			return filter.select(fHunk);
		}
		return true;
	}

	public void setMatches(boolean matches) {
		fMatches = matches;
	}

	public int getStartPosition() {
		return fHunk.getStart(getConfiguration().isReversed()) + fShift;
	}

	public String getLabel() {
		return getHunk().getDescription();
	}

	public InputStream getOriginalContents() {
		String contents = getContents(false, false);
		return asInputStream(contents);
	}

	public InputStream asInputStream(String contents) {
		String charSet = getCharset();
		return FileDiffResult.asInputStream(contents, charSet);
	}

	public InputStream getPatchedContents() {
		String contents = getContents(true, false);
		return asInputStream(contents);
	}

	public String getCharset() {
		return fDiffResult.getCharset();
	}
	
	public int getFuzz() {
		return fFuzz;
	}
}
