/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.core.patch;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * A diff project represents a project that was read from a workspace patch.
 * It contains the set of file diffs that were associated with the project
 * in the patch file.
 */
public class DiffProject {

	private IProject fProject;
	private Set fDiffs= new HashSet();

	/**
	 * Create a diff project for the given workspace project.
	 * @param project a workspace project
	 */
	public DiffProject(IProject project) {
		this.fProject= project;
	}

	/**
	 * Add the file diff to this project.
	 * @param diff the file diff.
	 */
	public void add(FileDiff diff) {
		fDiffs.add(diff);
		if (diff.getProject() != this)
			diff.setProject(this);
	}

	
	/**
	 * Return the workspace project associated with this diff project.
	 * @return the workspace project associated with this project
	 */
	public IProject getProject() {
		return this.fProject;
	}

	/**
	 * Return the name of this project.
	 * @return the name of this project
	 */
	public String getName() {
		return fProject.getName();
	}

	/**
	 * Return the file at the given path relative to this project.
	 * @param path the relative path
	 * @return the file at the given path relative to this project
	 */
	public IFile getFile(IPath path) {
		return fProject.getFile(path);
	}

	/**
	 * Remove the file diff from this project.
	 * @param diff the diff to be removed
	 */
	public void remove(FileDiff diff) {
		fDiffs.remove(diff);
	}

	/**
	 * Return whether this project contains the given diff.
	 * @param diff a file diff
	 * @return whether this project contains the given diff
	 */
	public boolean contains(FileDiff diff) {
		return fDiffs.contains(diff);
	}

	/**
	 * Return the file diffs associated with this project.
	 * @return the file diffs associated with this project
	 */
	public FileDiff[] getFileDiffs() {
		return (FileDiff[]) fDiffs.toArray(new FileDiff[fDiffs.size()]);
	}
}
