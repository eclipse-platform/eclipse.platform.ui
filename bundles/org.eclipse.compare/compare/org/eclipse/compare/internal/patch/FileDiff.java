/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.*;

import org.eclipse.compare.patch.*;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.*;

/**
 * A file diff represents a set of hunks that were associated with the
 * same path in a patch file.
 */
public class FileDiff implements IFilePatch {

	private IPath fOldPath, fNewPath;
	private List fHunks= new ArrayList();
	private DiffProject fProject; //the project that contains this diff
	private String header;
	
	/**
	 * Create a file diff for the given path and date information.
	 * @param oldPath the path of the before state of the file
	 * @param oldDate the timestamp of the before state
	 * @param newPath the path of the after state
	 * @param newDate the timestamp of the after state
	 */
 	protected FileDiff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
		fOldPath= oldPath;
		fNewPath= newPath;	
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
	protected IPath getPath(boolean reverse) {
		if (getDiffType(reverse) == Differencer.ADDITION) {
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
	protected void add(Hunk hunk) {
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
				if (type == Hunk.ADDED){
					add = true;
				} else if (type == Hunk.DELETED ){
					delete = true;
				}
			}
			if (add && !delete){
				return Differencer.ADDITION;
			} else if (!add && delete){
				return Differencer.DELETION;
			}
		}
		return Differencer.CHANGE;
	}
	
	/**
	 * Return the path of this file diff with the specified number
	 * of leading segments striped.
	 * @param strip the number of leading segments to strip from the path
	 * @param reverse whether the patch is being reversed
	 * @return the path of this file diff with the specified number
	 * of leading segments striped
	 */
	protected IPath getStrippedPath(int strip, boolean reverse) {
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
}
