package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.TeamPlugin;

public class FileModificationValidator implements IFileModificationValidator {
	private static final Status OK = new Status(Status.OK, TeamPlugin.ID, Status.OK, Policy.bind("FileModificationValidator.ok"), null); //$NON-NLS-1$
	private static final Status READ_ONLY = new Status(Status.ERROR, TeamPlugin.ID, Status.ERROR, Policy.bind("FileModificationValidator.isReadOnly"), null); //$NON-NLS-1$
	
	/*
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		// To do: hash the files by provider and only call each provider once.
		IStatus[] result = new IStatus[files.length];
		// Optimization so we don't create a new IFile[] each time
		IFile[] fileArray = new IFile[1];
		for (int i = 0; i < files.length; i++) {
			IFile file = files[i];
			RepositoryProvider provider = RepositoryProviderType.getProvider(file.getProject());
			IFileModificationValidator validator = null;
			if (provider != null) {
				validator = provider.getFileModificationValidator();
				if(validator!=null) {
					fileArray[0] = file;
					result[i] = validator.validateEdit(fileArray, context);
				}
			}	
				
			if(validator==null) {	
				result[i] =	(file.isReadOnly())	? READ_ONLY : OK;
			}
		}
		if (result.length == 1) {
			return result[0];
		} 
		return new MultiStatus(TeamPlugin.ID, 0, result, Policy.bind("FileModificationValidator.validateEdit"), null); //$NON-NLS-1$
	}

	/*
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		RepositoryProvider provider = RepositoryProviderType.getProvider(file.getProject());
		if (provider != null) {
			IFileModificationValidator validator = provider.getFileModificationValidator();
			if(validator!=null) {
				return validator.validateSave(file);
			}
		}
		return OK;
	}
}
