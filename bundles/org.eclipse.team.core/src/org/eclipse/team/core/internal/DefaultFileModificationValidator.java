package org.eclipse.team.core.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamPlugin;

public class DefaultFileModificationValidator implements IFileModificationValidator {
	private static final Status OK = new Status(Status.OK, TeamPlugin.ID, Status.OK, Policy.bind("FileModificationValidator.ok"), null); //$NON-NLS-1$
	private static final Status READ_ONLY = new Status(Status.ERROR, TeamPlugin.ID, Status.ERROR, Policy.bind("FileModificationValidator.isReadOnly"), null); //$NON-NLS-1$

	private IStatus getDefaultStatus(IFile file) {
		return file.isReadOnly()
				? READ_ONLY
				: OK;
	}
	
	/**
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		if(files.length == 1) {
			return getDefaultStatus(files[0]);
		}
		
		IStatus[] stati = new Status[files.length];
		
		for (int i = 0; i < files.length; i++) {
			stati[i] = getDefaultStatus(files[i]);	
		}
		
		return new MultiStatus(TeamPlugin.ID, 0, stati, Policy.bind("FileModificationValidator.validateEdit"), null); //$NON-NLS-1$
	}

	/**
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		return getDefaultStatus(file);
	}

}
