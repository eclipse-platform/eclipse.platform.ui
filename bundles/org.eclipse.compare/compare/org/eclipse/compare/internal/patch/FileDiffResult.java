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

import java.io.*;
import java.util.*;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.patch.*;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

public class FileDiffResult implements IFilePatchResult {

	private FileDiff fDiff;
	private boolean fMatches= false;
	private boolean fDiffProblem;
	private String fErrorMessage;
	private Map fHunkResults = new HashMap();
	private List fBeforeLines, fAfterLines;
	private final PatchConfiguration configuration;
	private String charset;
	private int fuzz;
	
	public FileDiffResult(FileDiff diff, PatchConfiguration configuration) {
		super();
		fDiff = diff;
		this.configuration = configuration;
	}
	
	public PatchConfiguration getConfiguration() {
		return configuration;
	}
	
	public boolean canApplyHunk(Hunk hunk) {
		HunkResult result = getHunkResult(hunk);
		return result.isOK() && !fDiffProblem;
	}
	
	/**
	 * Refreshes the state of the diff to {no matches, no problems} and checks to see what hunks contained
	 * by this Diff can actually be applied.
	 *
	 * Checks to see:
	 * 1) if the target file specified in fNewPath exists and is patchable
	 * 2) which hunks contained by this diff can actually be applied to the file
	 * @param storage the contents being patched or <code>null</code> for an addition
	 * @param monitor a progress monitor or <code>null</code> if no progress monitoring is desired
	 */
	 public void refresh(IStorage storage, IProgressMonitor monitor) {
		fMatches= false;
		fDiffProblem= false;
		boolean create= false;
		charset = Utilities.getCharset(storage);
		//If this diff is an addition, make sure that it doesn't already exist
		boolean exists = targetExists(storage);
		if (fDiff.getDiffType(getConfiguration().isReversed()) == Differencer.ADDITION) {
			if ((!exists || isEmpty(storage)) && canCreateTarget(storage)) {
				fMatches= true;
			} else {
				// file already exists
				fDiffProblem= true;
				fErrorMessage= PatchMessages.PreviewPatchPage_FileExists_error;
			}
			create= true;
		} else { //This diff is not an addition, try to find a match for it
			//Ensure that the file described by the path exists and is modifiable
			if (exists) {
				fMatches= true;
			} else {
				// file doesn't exist
				fDiffProblem= true;
				fErrorMessage= PatchMessages.PreviewPatchPage_FileDoesNotExist_error;
			}
		}

		if (fDiffProblem) {
			// We couldn't find the target file but we need to 
			// initialize the hunk results for display
			fBeforeLines = new ArrayList();
			fAfterLines = new ArrayList();
			Hunk[] hunks = fDiff.getHunks();
			for (int i = 0; i < hunks.length; i++) {
				Hunk hunk = hunks[i];
				HunkResult result = getHunkResult(hunk);
				result.setMatches(false);
			}
		} else {
			// If this diff has no problems discovered so far, try applying the patch
			patch(getLines(storage, create), monitor);
		}

		if (containsProblems()) {
			if (fMatches) {
				// Check to see if we have at least one hunk that matches
				fMatches = false;
				Hunk[] hunks = fDiff.getHunks();
				for (int i = 0; i < hunks.length; i++) {
					Hunk hunk = hunks[i];
					HunkResult result = getHunkResult(hunk);
					if (result.isOK()) {
						fMatches = true;
						break;
					}
				}
			}
		}
	}

	protected boolean canCreateTarget(IStorage storage) {
		return true;
	}

	protected boolean targetExists(IStorage storage) {
		return storage != null;
	}
	
	protected List getLines(IStorage storage, boolean create) {
		List lines = Patcher.load(storage, create);
		return lines;
	}
	
	protected boolean isEmpty(IStorage storage) {
		if (storage == null)
			return true;
		return Patcher.load(storage, false).isEmpty();
	}

	/*
	 * Tries to patch the given lines with the specified Diff.
	 * Any hunk that couldn't be applied is returned in the list failedHunks.
	 */
	public void patch(List lines, IProgressMonitor monitor) {
		fBeforeLines = new ArrayList();
		fBeforeLines.addAll(lines);
		if (getConfiguration().getFuzz() == -1) {
			fuzz = calculateFuzz(fBeforeLines, monitor);
		}
		int shift= 0;
		Hunk[] hunks = fDiff.getHunks();
		for (int i = 0; i < hunks.length; i++) {
			Hunk hunk = hunks[i];
			HunkResult result = getHunkResult(hunk);
			result.setShift(shift);
			if (result.patch(lines)) {
				shift = result.getShift();
			}
		}
		fAfterLines = lines;
	}
	
	protected boolean getDiffProblem() {
		return fDiffProblem;
	}

	/**
	 * Returns whether this Diff has any problems
	 * @return true if this Diff or any of its children Hunks have a problem, false if it doesn't
	 */
	protected boolean containsProblems() {
		if (fDiffProblem)
			return true;
		for (Iterator iterator = fHunkResults.values().iterator(); iterator.hasNext();) {
			HunkResult result = (HunkResult) iterator.next();
			if (!result.isOK())
				return true;
		}
		return false;
	}
	
	public String getLabel() {
		String label= getTargetPath().toString();
		if (this.fDiffProblem)
			return NLS.bind(PatchMessages.Diff_2Args, new String[] {label, fErrorMessage});
		return label;
	}
	
	public boolean hasMatches() {
		return fMatches;
	}

	/**
	 * Return the lines of the target file with all matched hunks applied.
	 * @return the lines of the target file with all matched hunks applied
	 */
	public List getLines() {
		return fAfterLines;
	}

	/**
	 * Calculate the fuzz factor that will allow the most hunks to be matched.
	 * @param lines the lines of the target file
	 * @param monitor a progress monitor
	 * @return the fuzz factor or <code>-1</code> if no hunks could be matched
	 */
	public int calculateFuzz(List lines, IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		fBeforeLines = new ArrayList();
		fBeforeLines.addAll(lines);
		// TODO: What about deletions?
		if (fDiff.getDiffType(getConfiguration().isReversed()) == Differencer.ADDITION) {
			// Additions don't need to adjust the fuzz factor
			// TODO: What about the after lines?
			return -1;
		}
		int shift= 0;
		int fuzz = 0;
		String name= getTargetPath().lastSegment();
		Hunk[] hunks = fDiff.getHunks();
		for (int j = 0; j < hunks.length; j++) {
			Hunk h = hunks[j];
			monitor.subTask(NLS.bind(PatchMessages.PreviewPatchPage_GuessFuzzProgress_format, new String[] {name, Integer.toString(j + 1)}));
			HunkResult result = getHunkResult(h);
			result.setShift(shift);
			int f= result.calculateFuzz(lines, monitor);
			if (f >= 0) {
				shift = result.getShift();
			}
			if (f>fuzz)
				fuzz= f;
			monitor.worked(1);
		}
		fAfterLines = lines;
		return fuzz;
	}
	
	public IPath getTargetPath() {
		return fDiff.getStrippedPath(getConfiguration().getPrefixSegmentStripCount(), getConfiguration().isReversed());
	}

	private HunkResult getHunkResult(Hunk hunk) {
		HunkResult result = (HunkResult)fHunkResults.get(hunk);
		if (result == null) {
			result = new HunkResult(this, hunk);
			fHunkResults .put(hunk, result);
		}
		return result;
	}

	List getFailedHunks() {
		List failedHunks = new ArrayList();
		for (Iterator iterator = fHunkResults.values().iterator(); iterator.hasNext();) {
			HunkResult result = (HunkResult) iterator.next();
			if (!result.isOK())
				failedHunks.add(result.getHunk());
		}
		return failedHunks;
	}

	public FileDiff getDiff() {
		return fDiff;
	}

	List getBeforeLines() {
		return fBeforeLines;
	}

	List getAfterLines() {
		return fAfterLines;
	}

	public HunkResult[] getHunkResults() {
		return (HunkResult[]) fHunkResults.values().toArray(new HunkResult[fHunkResults.size()]);
	}

	public InputStream getOriginalContents() {
		String contents = Patcher.createString(isPreserveLineDelimeters(), getBeforeLines());
		return asInputStream(contents, getCharSet());
	}

	public InputStream getPatchedContents() {
		String contents = Patcher.createString(isPreserveLineDelimeters(), getLines());
		return asInputStream(contents, getCharSet());
	}

	protected String getCharSet() {
		return charset;
	}

	protected boolean isPreserveLineDelimeters() {
		return false;
	}

	public IHunk[] getRejects() {
		List failedHunks = getFailedHunks();
		return (IHunk[]) failedHunks.toArray(new IHunk[failedHunks.size()]);
	}

	public boolean hasRejects() {
		return !getFailedHunks().isEmpty();
	}
	
	public static InputStream asInputStream(String contents, String charSet) {
		byte[] bytes = null;
		if (charSet != null) {
			try {
				bytes = contents.getBytes(charSet);
			} catch (UnsupportedEncodingException e) {
				CompareUIPlugin.log(e);
			}
		}
		if (bytes == null) {
			bytes = contents.getBytes();
		}
		return new ByteArrayInputStream(bytes);
	}

	public int getFuzz() {
		int cf = configuration.getFuzz();
		if (cf == -1) {
			return fuzz;
		}
		return cf;
	}
	
}
