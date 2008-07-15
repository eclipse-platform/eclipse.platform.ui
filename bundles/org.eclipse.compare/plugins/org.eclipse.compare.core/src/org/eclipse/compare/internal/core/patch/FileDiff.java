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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.patch.IFilePatch;
import org.eclipse.compare.patch.IFilePatchResult;
import org.eclipse.compare.patch.PatchConfiguration;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * A file diff represents a set of hunks that were associated with the
 * same path in a patch file.
 */
public class FileDiff implements IFilePatch {

	/**
	 * Difference constant (value 1) indicating one side was added.
	 */
	public static final int ADDITION= 1;
	/**
	 * Difference constant (value 2) indicating one side was removed.
	 */
	public static final int DELETION= 2;
	/**
	 * Difference constant (value 3) indicating side changed.
	 */
	public static final int CHANGE= 3;
	
	private IPath fOldPath, fNewPath;
	private long oldDate, newDate;
	private List fHunks= new ArrayList();
	private DiffProject fProject; //the project that contains this diff
	private String header;
	private int addedLines, removedLines;
	
	/**
	 * Create a file diff for the given path and date information.
	 * @param oldPath the path of the before state of the file
	 * @param oldDate the timestamp of the before state
	 * @param newPath the path of the after state
	 * @param newDate the timestamp of the after state
	 */
 	public FileDiff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
		fOldPath= oldPath;
		this.oldDate = oldDate;
		fNewPath= newPath;
		this.newDate = newDate;
	}
	
 	/**
 	 * Return the parent project or <code>null</code> if there isn't one.
 	 * @return the parent project or <code>null</code>
 	 */
	public DiffProject getProject() {
		return fProject;
	}
	
	/**
	 * Set the project of this diff to the given project.
	 * This method should only be called from
	 * {@link DiffProject#add(FileDiff)}
	 * @param diffProject the parent project
	 */
	void setProject(DiffProject diffProject) {
		if (fProject == diffProject)
			return;
		if (fProject != null)
			fProject.remove(this);
		this.fProject= diffProject;
	}
	
	/**
	 * Get the path of the file diff.
	 * @param reverse whether the path of the before state or after state 
	 * should be used
	 * @return the path of the file diff
	 */
	public IPath getPath(boolean reverse) {
		if (getDiffType(reverse) == ADDITION) {
			if (reverse)
				return fOldPath;
			return fNewPath;
		}
		if (reverse && fNewPath != null)
			return fNewPath;
		if (fOldPath != null)
			return fOldPath;
		return fNewPath;
	}
	
	/**
	 * Add the hunk to this file diff.
	 * @param hunk the hunk
	 */
	public void add(Hunk hunk) {
		fHunks.add(hunk);
		hunk.setParent(this);
	}
	
	/**
	 * Remove the hunk from this file diff
	 * @param hunk the hunk
	 */
	protected void remove(Hunk hunk) {
		fHunks.remove(hunk);
	}
	
	/**
	 * Return the hunks associated with this file diff.
	 * @return the hunks associated with this file diff
	 */
	public Hunk[] getHunks() {
		return (Hunk[]) fHunks.toArray(new Hunk[fHunks.size()]);
	}
	
	/**
	 * Return the number of hunks associated with this file diff.
	 * @return the number of hunks associated with this file diff
	 */
	public int getHunkCount() {
		return fHunks.size();
	}
	
	/**
	 * Return the difference type of this file diff.
	 * @param reverse whether the patch is being reversed
	 * @return the type of this file diff
	 */
	public int getDiffType(boolean reverse) {
		if (fHunks.size() == 1) {
			boolean add = false;
			boolean delete = false;
			Iterator iter = fHunks.iterator();
			while (iter.hasNext()){
				Hunk hunk = (Hunk) iter.next();
				int type =hunk.getHunkType(reverse);
				if (type == ADDITION){
					add = true;
				} else if (type == DELETION ){
					delete = true;
				}
			}
			if (add && !delete){
				return ADDITION;
			} else if (!add && delete){
				return DELETION;
			}
		}
		return CHANGE;
	}
	
	/**
	 * Return the path of this file diff with the specified number
	 * of leading segments striped.
	 * @param strip the number of leading segments to strip from the path
	 * @param reverse whether the patch is being reversed
	 * @return the path of this file diff with the specified number
	 * of leading segments striped
	 */
	public IPath getStrippedPath(int strip, boolean reverse) {
		IPath path= getPath(reverse);
		if (strip > 0 && strip < path.segmentCount())
			path= path.removeFirstSegments(strip);
		return path;
	}
	
	/**
	 * Return the segment count of the path of this file diff.
	 * @return the segment count of the path of this file diff
	 */
	public int segmentCount() {
		//Update prefix count - go through all of the diffs and find the smallest
		//path segment contained in all diffs.
		int length= 99;
		if (fOldPath != null)
			length= Math.min(length, fOldPath.segmentCount());
		if (fNewPath != null)
			length= Math.min(length, fNewPath.segmentCount());
		return length;
	}

	public IFilePatchResult apply(IStorage contents,
			PatchConfiguration configuration, IProgressMonitor monitor) {
		FileDiffResult result = new FileDiffResult(this, configuration);
		result.refresh(contents, monitor);
		return result;
	}

	public IPath getTargetPath(PatchConfiguration configuration) {
		return getStrippedPath(configuration.getPrefixSegmentStripCount(), configuration.isReversed());
	}

	public FileDiff asRelativeDiff() {
		if (fProject == null)
			return this;
		IPath adjustedOldPath = null;
		if (fOldPath != null) {
			adjustedOldPath = new Path(null, fProject.getName()).append(fOldPath);
		}
		IPath adjustedNewPath = null;
		if (fNewPath != null) {
			adjustedNewPath = new Path(null, fProject.getName()).append(fNewPath);
		}
		FileDiff diff = new FileDiff(adjustedOldPath, 0, adjustedNewPath, 0);
		for (Iterator iterator = fHunks.iterator(); iterator.hasNext();) {
			Hunk hunk = (Hunk) iterator.next();
			// Creating the hunk adds it to the parent diff
			new Hunk(diff, hunk);
		}
		return diff;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getHeader() {
		return header;
	}

	public long getBeforeDate() {
		return oldDate;
	}

	public long getAfterDate() {
		return newDate;
	}

	public void setAddedLines(int addedLines) {
		this.addedLines = addedLines;
	}
	
	public void setRemovedLines(int removedLines) {
		this.removedLines = removedLines;
	}

	public int getAddedLines() {
		return addedLines;
	}
	
	public int getRemovedLines() {
		return removedLines;
	}

}
