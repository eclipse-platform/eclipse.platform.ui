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
package org.eclipse.ui.tests.harness.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * <code>FileUtil</code> contains methods to create and
 * delete files and projects.
 */
public class FileUtil {

    /**
     * Creates a new project.
     * 
     * @param name the project name
     */
    public static IProject createProject(String name) throws CoreException {
        IWorkspace ws = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = ws.getRoot();
        IProject proj = root.getProject(name);
        if (!proj.exists())
            proj.create(null);
        if (!proj.isOpen())
            proj.open(null);
        return proj;
    }

    /**
     * Deletes a project.
     * 
     * @param proj the project
     */
    public static void deleteProject(IProject proj) throws CoreException {
        proj.delete(true, null);
    }

    /**
     * Creates a new file in a project.
     * 
     * @param name the new file name
     * @param proj the existing project
     * @return the new file
     */
    public static IFile createFile(String name, IProject proj)
            throws CoreException {
        IFile file = proj.getFile(name);
        if (!file.exists()) {
            String str = " ";
            InputStream in = new ByteArrayInputStream(str.getBytes());
            file.create(in, true, null);
        }
        return file;
    }

}

