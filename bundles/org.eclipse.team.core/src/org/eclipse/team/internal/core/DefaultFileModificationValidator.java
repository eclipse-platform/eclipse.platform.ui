/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.Team;

public class DefaultFileModificationValidator implements IFileModificationValidator {
	private static final Status OK = Team.OK_STATUS;

	private IStatus getDefaultStatus(IFile file) {
		return 
			file.isReadOnly()
			? new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.READ_ONLY_LOCAL, Policy.bind("FileModificationValidator.fileIsReadOnly", file.getFullPath().toString()), null) //$NON-NLS-1$
				: OK;
	}
	
	/**
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		if (files.length == 1) {
			return getDefaultStatus(files[0]);
		}
		
		IStatus[] stati = new Status[files.length];
		boolean allOK = true;
		
		for (int i = 0; i < files.length; i++) {
			stati[i] = getDefaultStatus(files[i]);	
			if(! stati[i].isOK())
				allOK = false;
		}
		
		return new MultiStatus(TeamPlugin.ID,
			0, stati,
			Policy.bind(
				allOK
					? "FileModificationValidator.ok"	//$NON-NLS-1$
					: "FileModificationValidator.someReadOnly" ),	//$NON-NLS-1$
			null); 
	}

	/**
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		return getDefaultStatus(file);
	}
}
