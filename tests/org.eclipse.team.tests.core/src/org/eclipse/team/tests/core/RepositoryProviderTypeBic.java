/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.core;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;

public class RepositoryProviderTypeBic extends RepositoryProviderType {
	File createdFile;
	/**
	 * @see org.eclipse.team.core.RepositoryProviderType#getProjectSetCapability()
	 */
	public ProjectSetCapability getProjectSetCapability() {
		return new ProjectSetCapability() {
			public IProject[] addToWorkspace(
				String[] referenceStrings,
				String filename,
				IPath root,
				Object context,
				IProgressMonitor monitor)
				throws TeamException {
				return null;
			}

			public void projectSetCreated(
				File file,
				IProgressMonitor monitor) {
					
				createdFile = file;
			}
		};
	}

	/**
	 * @return File
	 */
	public File getCreatedFile() {
		return createdFile;
	}

	/**
	 * Sets the createdFile.
	 * @param createdFile The createdFile to set
	 */
	public void setCreatedFile(File createdFile) {
		this.createdFile = createdFile;
	}

}
