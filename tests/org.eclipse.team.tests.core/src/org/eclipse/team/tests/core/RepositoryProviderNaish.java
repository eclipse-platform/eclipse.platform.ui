/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.tests.core;

import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.RepositoryProvider;

public class RepositoryProviderNaish extends RepositoryProvider {
	
	final public static String NATURE_ID = "org.eclipse.team.tests.core.naish-provider";
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
	
	public void setModificationValidator(IFileModificationValidator mv) {
		this.mv = mv;
	}
	
	public void setMoveDeleteHook(IMoveDeleteHook mdh) {
		this.mdh = mdh;
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
}