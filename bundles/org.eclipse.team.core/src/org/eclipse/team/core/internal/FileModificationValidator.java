package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamPlugin;

public class FileModificationValidator implements IFileModificationValidator {
	private static final IFileModificationValidator DEFAULT_VALIDATOR =
		new DefaultFileModificationValidator();

	private static final Status OK = new Status(Status.OK, TeamPlugin.ID,
		Status.OK,
		Policy.bind("FileModificationValidator.ok"), null); //$NON-NLS-1$
	private static final Status READ_ONLY = new Status(Status.ERROR, TeamPlugin.ID,
		Status.ERROR,
		Policy.bind("FileModificationValidator.isReadOnly"), null); //$NON-NLS-1$
	
	/*
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 * For all files, determine which provider.
	 * Ask each provider once for its files.
	 * Collect the resulting status' and return a MultiStatus.
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		ArrayList returnStati = new ArrayList();
		
		//map provider to the files under that provider's control
		Map providersToFiles = new HashMap(files.length);
		
		//for each file, determine which provider, map providers to files
		for (int i = 0; i < files.length; i++) {
			IFile file = files[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(file.getProject());
			
			if (!providersToFiles.containsKey(provider)) {
				providersToFiles.put(provider, new ArrayList());
			}
			
			((ArrayList)providersToFiles.get(provider)).add(file);
		}
		
		Iterator providersIterator = providersToFiles.keySet().iterator();
		
		//for each provider, validate its files
		while(providersIterator.hasNext()) {
			RepositoryProvider provider = (RepositoryProvider)providersIterator.next();
			ArrayList filesList = (ArrayList)providersToFiles.get(provider);
			IFile[] filesArray = (IFile[])filesList.toArray(new IFile[filesList.size()]);
			IFileModificationValidator validator = DEFAULT_VALIDATOR;

			//if no provider or no validator use the default validator
			if (provider != null) {
				IFileModificationValidator v = provider.getFileModificationValidator();
				if (v != null) validator = v;
			}
			
			returnStati.add(validator.validateEdit(filesArray, context));
		}				

		if (returnStati.size() == 1) {
			return (IStatus)returnStati.get(0);
		} 
		return new MultiStatus(TeamPlugin.ID,
			0, 
			(IStatus[])returnStati.toArray(new IStatus[returnStati.size()]),
			Policy.bind("FileModificationValidator.validateEdit"),
			null); //$NON-NLS-1$
	}

	/*
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		RepositoryProvider provider = RepositoryProvider.getProvider(file.getProject());
		IFileModificationValidator validator = DEFAULT_VALIDATOR;

		//if no provider or no validator use the default validator
		if (provider != null) {
			IFileModificationValidator v = provider.getFileModificationValidator();
			if (v != null) validator = v;
		}

		return validator.validateSave(file);
	}
}
