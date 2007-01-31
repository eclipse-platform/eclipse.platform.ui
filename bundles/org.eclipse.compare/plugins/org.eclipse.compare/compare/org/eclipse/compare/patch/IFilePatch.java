/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.patch;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A representation of a file patch that can be applied to an input stream.
 * <p>
 * This interface is not intended to be implemented by clients. Clients can
 * obtain file patches by calling
 * {@link ApplyPatchOperation#parsePatch(org.eclipse.core.resources.IStorage)}.
 * <p>
 * This interface is still under development and is to be considered
 * <strong>EXPERIMENTAL</strong>. 
 * @see ApplyPatchOperation#parsePatch(org.eclipse.core.resources.IStorage)
 * @since 3.3
 */
public interface IFilePatch {

	/**
	 * Return the target path for this patch. The target path may differ
	 * depending on whether the patch is being reversed or not.
	 * 
	 * @param configuration the patch configuration
	 * @return the target path for this patch
	 * @see PatchConfiguration#isReversed()
	 */
	public IPath getTargetPath(PatchConfiguration configuration);

	/**
	 * Apply this patch to the given file contents. The result provides the
	 * original and patch contents and also indicates whether some portions of
	 * the patch (called hunks) failed to apply.
	 * 
	 * @param contents the file contents
	 * @param configuration the patch configuration
	 * @param monitor a progress monitor
	 * @return the result of the patch application
	 */
	public IFilePatchResult apply(IStorage contents,
			PatchConfiguration configuration, IProgressMonitor monitor);
	
	/**
	 * Return the header information of the patch or
	 * <code>null</code> if there was no header text.
	 * The header may be multi-line.
	 * @return the header information of the patch or
	 * <code>null</code>
	 */
	public String getHeader();
}
