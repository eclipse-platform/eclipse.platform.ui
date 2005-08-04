/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

public class DefaultFileModificationValidator implements IFileModificationValidator {
	
	/*
	 * A validator plugged in the the Team UI that will prompt
	 * the user to make read-only files writtable. In the absense of
	 * this validator, edit/save fail on read-only files.
	 */
	private IFileModificationValidator uiValidator;

	private IStatus getDefaultStatus(IFile file) {
		return 
			file.isReadOnly()
			? new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.READ_ONLY_LOCAL, NLS.bind(Messages.FileModificationValidator_fileIsReadOnly, new String[] { file.getFullPath().toString() }), null) 
				: Status.OK_STATUS;
	}
	
	/**
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
	    IFile[] readOnlyFiles = getReadOnly(files);
	    if (readOnlyFiles.length == 0)
	        return Status.OK_STATUS;
	    synchronized (this) {
	        if (uiValidator == null) 
	            uiValidator = loadUIValidator();
	    }
	    if (uiValidator != null) {
	        return uiValidator.validateEdit(files, context);
	    }
	    // There was no plugged in validator so fail gracefully
		return getStatus(files); 
	}

    protected IStatus getStatus(IFile[] files) {
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
			allOK
					? Messages.ok
					: Messages.FileModificationValidator_someReadOnly,
			null);
    }

    private IFile[] getReadOnly(IFile[] files) {
        List result = new ArrayList(files.length);
        for (int i = 0; i < files.length; i++) {
            IFile file = files[i];
            if (file.isReadOnly()) {
                result.add(file);
            }
        }
        return (IFile[]) result.toArray(new IFile[result.size()]);
    }

    /**
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		return getDefaultStatus(file);
	}
	
    private IFileModificationValidator loadUIValidator() {
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(TeamPlugin.ID, TeamPlugin.DEFAULT_FILE_MODIFICATION_VALIDATOR_EXTENSION);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			if (extensions.length > 0) {
				IConfigurationElement[] configElements = extensions[0].getConfigurationElements();
				if (configElements.length > 0) {
					try {
                        Object o = configElements[0].createExecutableExtension("class"); //$NON-NLS-1$
                        if (o instanceof IFileModificationValidator) {
                            return (IFileModificationValidator)o;
                        }
                    } catch (CoreException e) {
                        TeamPlugin.log(e);
                    }
				}
			}
		}
		return null;
    }
}
