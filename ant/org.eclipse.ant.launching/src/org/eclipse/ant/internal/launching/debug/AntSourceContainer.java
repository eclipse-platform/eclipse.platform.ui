/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
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

package org.eclipse.ant.internal.launching.debug;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;

public class AntSourceContainer extends AbstractSourceContainer {

	private IWorkspaceRoot fRoot;

	public AntSourceContainer() {
		fRoot = ResourcesPlugin.getWorkspace().getRoot();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	@Override
	public Object[] findSourceElements(String path) throws CoreException {
		ArrayList<IStorage> sources = new ArrayList<>();
		File osFile = new File(path);
		if (osFile.exists()) {
			try {
				IPath canonicalPath = new Path(osFile.getCanonicalPath());
				IFile[] files = fRoot.findFilesForLocationURI(canonicalPath.makeAbsolute().toFile().toURI());
				if (files.length > 0) {
					for (int i = 0; i < files.length; i++) {
						sources.add(files[i]);
					}
				} else {
					sources.add(new LocalFileStorage(osFile));
				}
			}
			catch (IOException e) {
				// do nothing
			}
		}
		return sources.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	@Override
	public String getName() {
		return AntDebugMessages.AntSourceContainer_0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType() Not persisted via the launch configuration
	 */
	@Override
	public ISourceContainerType getType() {
		return null;
	}
}