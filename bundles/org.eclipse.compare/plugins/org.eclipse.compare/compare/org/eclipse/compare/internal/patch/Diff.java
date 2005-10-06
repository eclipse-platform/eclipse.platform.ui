/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class Diff implements IWorkbenchAdapter, IAdaptable {

	IPath fOldPath, fNewPath;
	long fOldDate, fNewDate;	// if 0: no file
	List fHunks= new ArrayList();
	boolean fMatches= false;
	private boolean fIsEnabled2= true;
	String fRejected;
	DiffProject fProject; //the project that contains this diff
	boolean fDiffProblem;
	String fErrorMessage;
	int fStrip;
	int fFuzzFactor;

	static ImageDescriptor addId= CompareUIPlugin.getImageDescriptor("ovr16/add_ov.gif"); //$NON-NLS-1$
	static ImageDescriptor delId= CompareUIPlugin.getImageDescriptor("ovr16/del_ov.gif"); //$NON-NLS-1$
	private WorkspacePatcher patcher;
	
 	/* package */ Diff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
		fOldPath= oldPath;
		fOldDate= oldPath == null ? 0 : oldDate;
		fNewPath= newPath;
		fNewDate= newPath == null ? 0 : newDate;	
	}
	
	boolean isEnabled() {
		return fIsEnabled2;
	}
	
	void setEnabled(boolean b) {
		fIsEnabled2= b;
	}
	
	void reverse() {
		IPath tp= fOldPath;
		fOldPath= fNewPath;
		fNewPath= tp;
		
		long t= fOldDate;
		fOldDate= fNewDate;
		fNewDate= t;
		
		Iterator iter= fHunks.iterator();
		while (iter.hasNext()) {
			Hunk hunk= (Hunk) iter.next();
			hunk.reverse();
		}
	}
	
	Hunk[] getHunks() {
		return (Hunk[]) fHunks.toArray(new Hunk[fHunks.size()]);
	}

	IPath getPath() {
		if (fOldPath != null)
			return fOldPath;
		return fNewPath;
	}
	
	void finish() {
		if (fHunks.size() == 1) {
			Hunk h= (Hunk) fHunks.get(0);
			if (h.fNewLength == 0) {
				fNewDate= 0;
				fNewPath= fOldPath;
			}
		}
	}
	
	/* package */ void add(Hunk hunk) {
		fHunks.add(hunk);
	}
	
	/* package */ int getType() {
		if (fOldDate == 0)
			return Differencer.ADDITION;
		if (fNewDate == 0)
			return Differencer.DELETION;
		return Differencer.CHANGE;
	}
	
	/* package */ String getDescription(int strip) {
		IPath path= getStrippedPath(strip);
		return path.toOSString();
	}

	private IPath getStrippedPath(int strip) {
		IPath path= fOldPath;
		if (fOldDate == 0)
			path= fNewPath;
		if (strip > 0 && strip < path.segmentCount())
			path= path.removeFirstSegments(strip);
		return path;
	}

	DiffProject getProject() {
		return fProject;
	}

	public void setProject(DiffProject diffProject) {
		this.fProject= diffProject;
		fProject.addDiff(this);
	}
	
	/**
	 * Resets the state of this diff to {no matches, no problems} and checks to see what hunks contained
	 * by this Diff can actually be applied
	 * @param wspatcher
	 * @param strip
	 * @param fuzzfactor
	 * @return ArrayList containing which hunks contained by this diff can be checked
	 */
	ArrayList reset(WorkspacePatcher wspatcher, int strip, int fuzzfactor) {
		//reset state - no matches, no problems
		this.fMatches= false;
		this.fDiffProblem= false;
		this.fStrip= strip;
		this.fFuzzFactor= fuzzfactor;
		this.patcher= wspatcher;
		//Make sure that the file that contains this diff exists and is modifiable
		ArrayList failedHunks= checkForFileExistance();
		ArrayList hunksToCheck= new ArrayList();
		//Ensure that, for workspace patches, the containing project exists in the workspace
		boolean projectExistsInWorkspace=true;
		if (fProject != null){
			projectExistsInWorkspace = fProject.getProject().exists();
		}
		//Verify if any of the hunks have failed, and reset the state of each hunk
		//accordingly
		for (Iterator iter= fHunks.iterator(); iter.hasNext();) {
			Hunk hunk= (Hunk) iter.next();
			boolean hunkFailed= failedHunks.contains(hunk);
			//if any hunk has failed we have to alter this Diff's fMatches field
			if (hunkFailed)
				this.fMatches= false;
			hunk.reset(hunkFailed);
			if (!hunkFailed && projectExistsInWorkspace)
				hunksToCheck.add(hunk);
		}
		return hunksToCheck;
	}

	/**
	 * Checks to see:
	 * 1) if the target file specified in fNewPath exists and is patchable
	 * 2) which hunks contained by this diff can be catually applied to the file
	 * @return a list containg the hunks that could not be applied
	 */
	private ArrayList checkForFileExistance() {
		IFile file= getTargetFile();
		boolean create= false;
		//If this diff is an addition, make sure that it doesn't already exist
		if (getType() == Differencer.ADDITION) {
			if (file == null || !file.exists()) {
				fMatches= true;
			} else {
				// file already exists
				fDiffProblem= true;
				fErrorMessage= PatchMessages.PreviewPatchPage_FileExists_error;
			}
			create= true;
		} else { //This diff is not an addition, try to find a match for it
			//Ensure that the file described by the path exists and is modifiable
			if (file != null) {
				fMatches= true;
			} else {
				// file doesn't exist
				fDiffProblem= true;
				fErrorMessage= PatchMessages.PreviewPatchPage_FileDoesNotExist_error;
			}
		}

		ArrayList failedHunks= new ArrayList();
		patcher.setFuzz(fFuzzFactor);
		//If this diff  has no problems discovered so far, try applying the patch
		if (!fDiffProblem)
			patcher.apply(this, file, create, failedHunks);

		if (failedHunks.size() > 0)
			fRejected= patcher.getRejected(failedHunks);

		return failedHunks;
	}

	public WorkspacePatcher getPatcher() {
		return patcher;
	}

	public IFile getTargetFile() {
		if (fProject != null)
			return fProject.getFile(getStrippedPath(fStrip));
		return getPatcher().existsInTarget(getStrippedPath(fStrip));
	}

	//IWorkbenchAdapter methods
	public Object[] getChildren(Object o) {
		return fHunks.toArray();
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		if (object instanceof Diff) {
			Diff diff= (Diff) object;
			switch (diff.getType()) {
				case Differencer.ADDITION :
					return addId;
				case Differencer.DELETION :
					return delId;
			}
		}
		return null;
	}

	public String getLabel(Object o) {
		String label= getDescription(fStrip);
		if (this.fDiffProblem)
			return NLS.bind(PatchMessages.Diff_2Args, new String[] {label, fErrorMessage});
		return label;
	}

	public Object getParent(Object o) {
		return fProject;
	}

	//IAdaptable methods
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return this;
		return null;
	}

}

