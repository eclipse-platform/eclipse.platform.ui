/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.io.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.*;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.ui.IWorkbenchPart;

public class WorkspaceFileDiffOperation extends FileDiffOperation {

	public WorkspaceFileDiffOperation(IWorkbenchPart part, ResourceMapping[] mappings, LocalOption[] options, File file, boolean isMultiPatch, boolean includeFullPathInformation, IPath patchRoot) {
		super(part, mappings, options, file, isMultiPatch, includeFullPathInformation, patchRoot);
	}
	
	protected void copyFile() throws CVSException {
		
		IWorkspaceRoot root =ResourcesPlugin.getWorkspace().getRoot();
		IFile finalFile = root.getFileForLocation(new Path(this.file.getPath()));
		InputStream fileInputStream = null;
		try {
			fileInputStream = new BufferedInputStream(new FileInputStream(tempFile));
			if(!finalFile.exists()) {
				finalFile.create(fileInputStream, IResource.FORCE , null);
			} else {
				finalFile.setContents(fileInputStream, IResource.FORCE, null);
			}	
		} catch (FileNotFoundException e) {
			throw CVSException.wrapException(e); 
		} catch (CoreException e) {
			throw CVSException.wrapException(e); 
		}
		finally{
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// Ignore
				} finally {					
					if (tempFile != null)
						tempFile.delete();
				}
			}
	    }
	}

}
