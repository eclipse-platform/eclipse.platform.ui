/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

import org.eclipse.compare.patch.AbstractHunk;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class HunkResult extends AbstractHunk {

	private static final boolean DEBUG= false;
	
	private Hunk fHunk;
	private boolean fMatches;
	private int fShift;

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
	 * the methods tries fuzz lines before and after.
	 * @param lines the lines to be patched
	 * @return whether the hunk could be applied
	 */
	public boolean patch(List lines) {
		fMatches = false;
		Patcher patcher = getPatcher();
		if (patcher.isEnabled(fHunk)) {
			if (fHunk.tryPatch(patcher, lines, fShift)) {
				fShift+= fHunk.doPatch(patcher, lines, fShift);
				fMatches = true;
			} else {
				boolean found= false;
				int oldShift= fShift;
				
				for (int i= 1; i <= patcher.getFuzz(); i++) {
					if (fHunk.tryPatch(patcher, lines, fShift-i)) {
						if (patcher.isAdjustShift())
							fShift-= i;
						found= true;
						break;
					}
				}
				
				if (! found) {
					for (int i= 1; i <= patcher.getFuzz(); i++) {
						if (fHunk.tryPatch(patcher, lines, fShift+i)) {
							if (patcher.isAdjustShift())
								fShift+= i;
							found= true;
							break;
						}
					}
				}
				
				if (found) {
					if (DEBUG) System.out.println("patched hunk at offset: " + (fShift-oldShift)); //$NON-NLS-1$
					fShift+= fHunk.doPatch(patcher, lines, fShift);
					fMatches = true;
				}
			}
		}
		return fMatches;
	}
	
	private Patcher getPatcher() {
		return getDiffResult().getPatcher();
	}

	/**
	 * Calculate the fuzz factor that will allow the most hunks to be matched.
	 * @param lines the lines of the target file
	 * @param monitor a progress monitor
	 * @return the fuzz factor or -1 if the hunk could not be matched
	 */
	public int calculateFuzz(List lines, IProgressMonitor monitor) {
		
		fMatches= false;
		int fuzz = 0;
		Patcher patcher = getPatcher();
		if (fHunk.tryPatch(patcher, lines, fShift)) {
			fShift+= fHunk.doPatch(patcher, lines, fShift);
			fMatches = true;
		} else {
			int hugeFuzz= lines.size();	// the maximum we need for this file
			fuzz= -1;	// not found
			
			for (int i= 1; i <= hugeFuzz; i++) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				if (fHunk.tryPatch(patcher, lines, fShift-i)) {
					fuzz= i;
					if (patcher.isAdjustShift())
						fShift-= i;
					fMatches= true;
					break;
				}
			}
			
			if (! fMatches) {
				for (int i= 1; i <= hugeFuzz; i++) {
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					if (fHunk.tryPatch(patcher, lines, fShift+i)) {
						fuzz= i;
						if (patcher.isAdjustShift())
							fShift+= i;
						fMatches= true;
						break;
					}
				}
			}
			
			if (fMatches)
				fShift+= fHunk.doPatch(patcher, lines, fShift);
		}
		return fuzz;
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
				return getPatcher().createString(lines);
		}
		return getHunk().getContents(afterState, getPatcher().isReversed());
	}

	public void setMatches(boolean matches) {
		fMatches = matches;
	}

	public int getStartPosition() {
		return fHunk.getStart(getPatcher().isReversed()) + fShift;
	}
}
