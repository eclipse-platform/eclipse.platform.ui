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
package org.eclipse.team.internal.ccvs.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;

/**
 * Core validator that will load the UI validator only if a prompt is needed
 */
public class CVSCoreFileModificationValidator implements ICVSFileModificationValidator {
    
    IFileModificationValidator uiValidator;

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IFileModificationValidator#validateEdit(org.eclipse.core.resources.IFile[], java.lang.Object)
     */
    public IStatus validateEdit(IFile[] files, Object context) {
	    IFile[] unmanagedReadOnlyFiles = getUnmanagedReadOnlyFiles(files);
	    if (unmanagedReadOnlyFiles.length > 0) {
	        IStatus status = setWritable(unmanagedReadOnlyFiles);
	        if (!status.isOK()) {
	            return status;
	        }
	    }
		IFile[] readOnlyFiles = getManagedReadOnlyFiles(files);
		if (readOnlyFiles.length == 0) return Status.OK_STATUS;
		return edit(readOnlyFiles, context);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IFileModificationValidator#validateSave(org.eclipse.core.resources.IFile)
     */
    public IStatus validateSave(IFile file) {
		if (!needsCheckout(file)) {
		    if (file.isReadOnly()) {
		        setWritable(new IFile[] { file } );
		    }
		    return Status.OK_STATUS;
		}
		return edit(new IFile[] {file}, (Object)null);
    }

    /**
     * Method for editing a set of files. Is overriden by the
     * UI to prompt the user. Default behavior is to try and load the
     * UI validator and, failing that, to edit without 
     * prompting.
     * @param readOnlyFiles
     * @param context
     * @return
     */
    protected IStatus edit(IFile[] readOnlyFiles, Object context) {
        synchronized(this) {
	        if (uiValidator == null) {
	            uiValidator = getPluggedInValidator();
	        }
        }
        if (uiValidator != null) {
            return uiValidator.validateEdit(readOnlyFiles, context);
        } else {
	        try {
	            performEdit(readOnlyFiles, new NullProgressMonitor());
	        } catch (CVSException e) {
	            return e.getStatus();
	        }
	        return Status.OK_STATUS;
        }
    }
	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFileModificationValidator#validateMoveDelete(org.eclipse.core.resources.IFile[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus validateMoveDelete(IFile[] files, IProgressMonitor monitor) {
		IFile[] readOnlyFiles = getManagedReadOnlyFiles(files);
		if (readOnlyFiles.length == 0) return Status.OK_STATUS;

		try {
			performEdit(readOnlyFiles, monitor);
			return Status.OK_STATUS;
		} catch (CVSException e) {
			return e.getStatus();
		}
	}
	
	protected CVSTeamProvider getProvider(IFile[] files) {
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(files[0].getProject(), CVSProviderPlugin.getTypeId());
		return provider;
	}
	
	protected void performEdit(IFile[] files, IProgressMonitor monitor) throws CVSException {
		getProvider(files).edit(files, false /* recurse */, true /* notify server */, ICVSFile.NO_NOTIFICATION, monitor);
	}
	
	private boolean needsCheckout(IFile file) {
		try {
			if (file.isReadOnly()) {
				ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
				boolean managed = cvsFile.isManaged();
                return managed;
			}
		} catch (CVSException e) {
			// Log the exception and assume we don't need a checkout
			CVSProviderPlugin.log(e);
		}
		return false;
	}
	
	protected IStatus setWritable(final IFile[] files) {
        for (int i = 0; i < files.length; i++) {
        	IFile file = files[i];
        	ResourceAttributes attributes = file.getResourceAttributes();
        	if (attributes != null) {
        		attributes.setReadOnly(false);
        	}
        	try {
        		file.setResourceAttributes(attributes);
        	} catch (CoreException e) {
        		return CVSException.wrapException(e).getStatus();
        	}
        }
        return Status.OK_STATUS;
    }
    
	private IFile[] getManagedReadOnlyFiles(IFile[] files) {
		List readOnlys = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IFile iFile = files[i];
			if (needsCheckout(iFile)) {
				readOnlys.add(iFile);
			}
		}
		return (IFile[]) readOnlys.toArray(new IFile[readOnlys.size()]);
	}
	
    protected IFile[] getUnmanagedReadOnlyFiles(IFile[] files) {
		List readOnlys = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IFile iFile = files[i];
			if (iFile.isReadOnly() && !needsCheckout(iFile)) {
				readOnlys.add(iFile);
			}
		}
		return (IFile[]) readOnlys.toArray(new IFile[readOnlys.size()]);
    }
    
	private static IFileModificationValidator getPluggedInValidator() {
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(CVSProviderPlugin.ID, CVSProviderPlugin.PT_FILE_MODIFICATION_VALIDATOR).getExtensions();
		if (extensions.length == 0)
			return null;
		IExtension extension = extensions[0];
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0) {
			CVSProviderPlugin.log(IStatus.ERROR, Policy.bind("CVSAdapter.noConfigurationElement", new Object[] {extension.getUniqueIdentifier()}), null);//$NON-NLS-1$
			return null;
		}
		try {
			IConfigurationElement config = configs[0];
			return (IFileModificationValidator) config.createExecutableExtension("run");//$NON-NLS-1$
		} catch (CoreException ex) {
			CVSProviderPlugin.log(IStatus.ERROR, Policy.bind("CVSAdapter.unableToInstantiate", new Object[] {extension.getUniqueIdentifier()}), ex);//$NON-NLS-1$
			return null;
		}
	}
}
