package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.team.core.RepositoryProviderType;

public class CVSRepositoryProviderType extends RepositoryProviderType {

	private static CVSRepositoryProviderType instance;

	public CVSRepositoryProviderType() {
		instance = this;
	}

	/*
	 * Returns the one and only instance of the CVS repository type.
	 */
	public static CVSRepositoryProviderType getInstance() {
		return instance;
	}

	/*
	 * @see RepositoryProviderType#getID()
	 */
	public String getID() {
		return CVSProviderPlugin.getTypeId();
	}
}
