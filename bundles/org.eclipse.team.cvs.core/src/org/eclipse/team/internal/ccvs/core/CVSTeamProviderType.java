package org.eclipse.team.internal.ccvs.core;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamException;


/**
 * This class represents the CVS Provider's capabilities in the absence of a
 * particular project.
 */

public class CVSTeamProviderType extends RepositoryProviderType {
	
	/**
	 * @see org.eclipse.team.core.RepositoryProviderType#supportsProjectSetImportRelocation()
	 */
	public boolean supportsProjectSetImportRelocation() {
		return false;
	}

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
				Object context,
				IProgressMonitor monitor) {
					
				int i = 0;
			}
		};
	}

}
