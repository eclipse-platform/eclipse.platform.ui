package org.eclipse.ui.tests;

import java.io.StringBufferInputStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

/**
 * FileUtil contains file creation and manipulation utilities.
 */
public class FileUtil {

	/**
	 * Creates a new project.
	 */
	public static IProject createProject(String name) 
		throws CoreException
	{
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		IProject proj = root.getProject(name);
		proj.create(null);
		proj.open(null);
		return proj;
	}

	/**
	 * Deletes a project
	 */
	public static void deleteProject(IProject proj) 
		throws CoreException
	{
		proj.delete(true, null);
	}
	
	/**
	 * Creates a new file in a project.
	 */
	public static IFile createFile(String name, IProject proj) 
		throws CoreException
	{
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		IFile file = proj.getFile(name);
		file.create(new StringBufferInputStream(" "),
			true, null);
		return file;
	}
	
}

