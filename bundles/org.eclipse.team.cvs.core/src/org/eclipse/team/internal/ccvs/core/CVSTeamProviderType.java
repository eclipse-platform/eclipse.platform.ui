package org.eclipse.team.internal.ccvs.core;

import org.eclipse.team.core.RepositoryProviderType;


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


}
