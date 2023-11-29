/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * Interface for file exporters of different file formats.  Used by the
 * zip and tar.gz exporters.
 *
 * @since 3.1
 */
public interface IFileExporter {

	/**
	 * Do all required cleanup now that we are finished with the
	 * currently-open file.
	 */
	public void finished() throws IOException;

	/**
	 * Write the entry for the folder's name into the current archive.
	 *
	 * @param container the container to write
	 * @param destinationPath the path that will be used in the archive
	 * @throws IOException if an IO error occurs while writing the folder entry
	 */
	public void write(IContainer container, String destinationPath)
		throws IOException;

	/**
	 * Write the passed resource to the current archive
	 */
	public void write(IFile resource, String destinationPath)
		throws IOException, CoreException;

}
