package org.eclipse.team.tests.core;

import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;

public class RepositoryProviderBic extends RepositoryProvider {
	
	final public static String NATURE_ID = "org.eclipse.team.tests.core.bic-provider";
	
	private IMoveDeleteHook mdh;
	private IFileModificationValidator mv;
	
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
	
	/*
	 * @see RepositoryProvider#getFileModificationValidator()
	 */
	public IFileModificationValidator getFileModificationValidator() {
		return mv;
	}

	/*
	 * @see RepositoryProvider#getMoveDeleteHook()
	 */
	public IMoveDeleteHook getMoveDeleteHook() {
		return mdh;
	}
	
	public void setModificationValidator(IFileModificationValidator mv) {
		this.mv = mv;
	}
	
	public void setMoveDeleteHook(IMoveDeleteHook mdh) {
		this.mdh = mdh;
	}
}