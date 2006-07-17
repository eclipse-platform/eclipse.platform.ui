/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import java.io.FileOutputStream;
import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * This builder is for investigating a PR.  It creates java files on disk, then 
 * does refresh local to bring them into the workspace.  It creates the same file
 * regardless of full vs. incremental build, or what the delta is.
 */
public class RefreshLocalJavaFileBuilder extends TestBuilder {
	public static final String BUILDER_NAME = "org.eclipse.core.tests.resources.refreshbuilder";

	/*
	 * @see InternalBuilder#build(int, Map, IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		super.build(kind, args, monitor);

		IProject project = getProject();
		IFile file = project.getFile("A.java");
		IPath localLocation = project.getLocation().append(file.getName());
		java.io.File localFile = localLocation.toFile();
		try {
			if (localFile.exists())
				localFile.delete();
			FileOutputStream out = new FileOutputStream(localFile);
			out.write("public class A {}".getBytes());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		return null;
	}

}
