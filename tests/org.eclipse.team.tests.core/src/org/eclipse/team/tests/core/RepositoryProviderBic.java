package org.eclipse.team.tests.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;

public class RepositoryProviderBic extends RepositoryProvider {
	
	final public static String NATURE_ID = "org.eclipse.team.tests.core.bic-provider";
	
	/*
	 * @see RepositoryProvider#configureProject()
	 */
	public void configureProject() throws CoreException {
	}

	/*
	 * @see RepositoryProvider#getID()
	 */
	public String getID() {
		return NATURE_ID;
	}
	/*
	 * @see IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
	}
}