/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import org.eclipse.compare.internal.core.patch.FileDiff;
import org.eclipse.compare.patch.IFilePatch;
import org.eclipse.compare.patch.IFilePatchResult;
import org.eclipse.compare.patch.IHunk;
import org.eclipse.compare.patch.PatchConfiguration;
import org.eclipse.compare.patch.ReaderCreator;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class FileDiffWrapper implements IFilePatch {

	private FileDiff fileDiff;

	public FileDiffWrapper(FileDiff fileDiff) {
		this.fileDiff = fileDiff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.compare.patch.IFilePatch#apply(org.eclipse.core.resources
	 * .IStorage, org.eclipse.compare.patch.PatchConfiguration,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFilePatchResult apply(IStorage content,
			PatchConfiguration configuration, IProgressMonitor monitor) {
		return fileDiff.apply(content != null ? Utilities
				.getReaderCreator(content) : null, configuration, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.compare.patch.IFilePatch2#apply(org.eclipse.compare.patch
	 * .ReaderCreator, org.eclipse.compare.patch.PatchConfiguration,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFilePatchResult apply(ReaderCreator content,
			PatchConfiguration configuration, IProgressMonitor monitor) {
		return fileDiff.apply(content, configuration, monitor);
	}

	public long getAfterDate() {
		return fileDiff.getAfterDate();
	}

	public long getBeforeDate() {
		return fileDiff.getBeforeDate();
	}

	public String getHeader() {
		return fileDiff.getHeader();
	}

	public IPath getTargetPath(PatchConfiguration configuration) {
		return fileDiff.getTargetPath(configuration);
	}

	public IHunk[] getHunks() {
		return fileDiff.getHunks();
	}

}
