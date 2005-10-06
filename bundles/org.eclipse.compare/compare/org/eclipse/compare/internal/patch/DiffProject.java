/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class DiffProject implements IWorkbenchAdapter, IAdaptable {

	List fDiffs= new ArrayList();
	IProject fProject;

	//used for patch retargeting
	String fOriginalProjectName= null;

	DiffProject(IProject project) {
		this.fProject= project;
		this.fOriginalProjectName= project.getName();
	}

	void addDiff(Diff diff) {
		fDiffs.add(diff);
	}

	void addDiffs(Diff[] diffs) {
		for (int i= 0; i<diffs.length; i++) {
			fDiffs.add(diffs[i]);
		}
	}

	IProject getProject() {
		return this.fProject;
	}

	String getName() {
		return fProject.getName();
	}

	void setProject(IProject project) {
		this.fProject= project;
	}

	/**
	 * Resets all of the diffs contained by this project
	 * @param patcher
	 * @param strip
	 * @param fuzzfactor
	 * @return a list of which hunks need to be checked
	 */
	ArrayList reset(WorkspacePatcher patcher, int strip, int fuzzfactor) {
		ArrayList hunksToCheck= new ArrayList();
		for (Iterator iter= fDiffs.iterator(); iter.hasNext();) {
			Diff diff= (Diff) iter.next();
			hunksToCheck.addAll(diff.reset(patcher, strip, fuzzfactor));
		}
		return hunksToCheck;
	}

	public IFile getFile(IPath path) {
		return fProject.getFile(path);
	}

	/**
	 * Returns the target files of all the Diffs contained by this 
	 * DiffProject.
	 * @return An array of IFiles that are targeted by the Diffs
	 */
	public IFile[] getTargetFiles() {
		List files= new ArrayList();
		for (Iterator iter= fDiffs.iterator(); iter.hasNext();) {
			Diff diff= (Diff) iter.next();
			if (diff.isEnabled()) {
				files.add(diff.getTargetFile());
			}
		}
		return (IFile[]) files.toArray(new IFile[files.size()]);
	}

	//IWorkbenchAdapter methods
	public Object[] getChildren(Object o) {
		return fDiffs.toArray();
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		Object o= fProject.getAdapter(IWorkbenchAdapter.class);
		if (o instanceof IWorkbenchAdapter) {
			ImageDescriptor id= ((IWorkbenchAdapter) o).getImageDescriptor(fProject);
			return id;
		}
		return null;
	}

	public String getLabel(Object o) {
		return getName();
	}

	public Object getParent(Object o) {
		return null;
	}

	//IAdaptable methods
	public Object getAdapter(Class adapter) {
		if (adapter==IWorkbenchAdapter.class) {
			return this;
		}
		return null;
	}

	public String getOriginalProjectName() {
		return fOriginalProjectName;
	}
}
