/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.wizards.datatransfer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.IPath;

/**
 * A simple resource visitor that allows to find one or more files by name in a
 * {@link IContainer}
 *
 * @since 3.12
 */
public class RecursiveFileFinder implements IResourceVisitor {

	private IFile firstFoundFile = null;
	private Set<IFile> foundFiles = new HashSet<>();
	private String fileName;
	private Set<IPath> ignoredDirectories;

	/**
	 *
	 * @param fileName
	 *            the name of the file to look for
	 * @param ignoredDirectories
	 *            which directories are excluded from research. Sub-directories
	 *            will get ignored too.
	 */
	public RecursiveFileFinder(String fileName, Set<IPath> ignoredDirectories) {
		this.fileName = fileName;
		this.ignoredDirectories = ignoredDirectories;
	}

	@Override
	public boolean visit(IResource res) {
		if (ignoredDirectories != null) {
			for (IPath ignoedDirectory : this.ignoredDirectories) {
				if (ignoedDirectory.isPrefixOf(res.getLocation())) {
					return false;
				}
			}
		}

		if (res.getType() == IResource.FILE && res.getName().equals(fileName)) {
			if (this.firstFoundFile == null) {
				this.firstFoundFile = (IFile)res;
			}
			this.foundFiles.add( (IFile)res );
		}
		return res instanceof IContainer;
	}

	/**
	 * @return the first found file with right name
	 */
	public IFile getFile() {
		return this.firstFoundFile;
	}

	/**
	 * @return All found files
	 */
	public Set<IFile> getFiles() {
		return this.foundFiles;
	}

}