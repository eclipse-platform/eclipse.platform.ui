/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;


public class SimpleTestProject {

	public static final String TEST_PROJECT_NAME= "TestProject";
	private IProject fProject;
	
	public SimpleTestProject() throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		fProject= root.getProject(TEST_PROJECT_NAME);
		fProject.create(null);
		fProject.open(null);
	}
	
	public IProject getProject() {
		return fProject;
	}
	
	public IFolder createFolder(String name) throws CoreException {
		return createFolder(fProject, name);
	}
	
	public IFolder createFolder(IContainer parent, String name) throws CoreException {
		IFolder result= parent.getFolder(new Path(name));
		result.create(true, true, null);
		return result;
	}
	
	public IFile createFile(IContainer parent, String name, String content) throws CoreException {
		IFile result= parent.getFile(new Path(name));
		result.create(new ByteArrayInputStream(content.getBytes()), true, null);
		return result;
	}

	public void delete() throws CoreException {
		fProject.delete(true, true, null);
	}
	
	public String getContent(IFile file) throws CoreException, IOException {
		StringBuffer result= new StringBuffer();
		InputStreamReader reader= new InputStreamReader(file.getContents());
		char[] buffer= new char[1024];
		int amount;
		while ((amount= reader.read(buffer)) != -1) {
			result.append(buffer, 0, amount);
		}
		reader.close();
		return result.toString();
	}
}
