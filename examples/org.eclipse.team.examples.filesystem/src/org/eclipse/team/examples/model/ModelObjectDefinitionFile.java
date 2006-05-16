/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;

public class ModelObjectDefinitionFile extends ModelFile {

	private static final String MODEL_OBJECT_DEFINITION_FILE_EXTENSION = "mod";

	public static boolean isModFile(IResource resource) {
		return resource instanceof IFile 
		&& resource.getFileExtension().equals(MODEL_OBJECT_DEFINITION_FILE_EXTENSION);
	}
	
	public ModelObjectDefinitionFile(IFile file) {
		super(file);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.examples.model.ModelObject#getChildren()
	 */
	public ModelObject[] getChildren() throws CoreException {
		return getModelObjectElementFiles();
	}

	private ModelObjectElementFile[] getModelObjectElementFiles() throws CoreException {
		List result = new ArrayList();
		String[] filePaths = readLines((IFile)getResource());
		for (int i = 0; i < filePaths.length; i++) {
			String path = filePaths[i];
			IFile file = getFile(path);
			if (file != null) {
				ModelObjectElementFile moeFile = getMoeFile(file);
				if (moeFile != null)
					result.add(moeFile);
			}
		}
		return (ModelObjectElementFile[]) result.toArray(new ModelObjectElementFile[result.size()]);
	}

	private String[] readLines(IFile file) throws CoreException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
		String line = null;
		List result = new ArrayList();
		try {
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, FileSystemPlugin.ID, 0, 
					NLS.bind("Error reading from file {0}", file.getFullPath()), e));
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// Ignore
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	private ModelObjectElementFile getMoeFile(IFile file) {
		if (ModelObjectElementFile.isMoeFile(file)) {
			return new ModelObjectElementFile(this, file);
		}
		return null;
	}

	private IFile getFile(String path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validatePath(path, IResource.FILE);
		if (status.isOK()) {
			return workspace.getRoot().getFile(new Path(path));
		}
		FileSystemPlugin.log(status);
		return null;
	}

}
