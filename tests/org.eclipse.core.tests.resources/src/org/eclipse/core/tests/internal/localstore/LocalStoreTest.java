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
package org.eclipse.core.tests.internal.localstore;

import java.io.*;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.tests.resources.ResourceTest;

public abstract class LocalStoreTest extends ResourceTest {
	// test configuration attributes
	protected static int numberOfProjects = 3;
	protected static int numberOfProperties = 5;
	protected static final int sleepTime = 5000;

	// projects names and projects
	protected String[] projectNames;
	protected IProject[] projects;

	public LocalStoreTest() {
		super();
	}

	public LocalStoreTest(String name) {
		super(name);
	}

	protected int countChildren(File root) {
		String[] children = root.list();
		if (children == null)
			return 0;
		int result = 0;
		for (int i = 0; i < children.length; i++) {
			File child = new File(root, children[i]);
			if (child.isDirectory())
				result += countChildren(child);
			result++;
		}
		return result;
	}

	public int countChildren(IFolder root) throws CoreException {
		int total = 0;
		IResource[] children = root.members();
		for (int i = 0; i < children.length; i++) {
			if (children[i].getType() == IResource.FILE)
				total++;
			else
				total += countChildren((IFolder) children[i]);
		}
		return total;
	}

	/**
	 * Create a file with random content. If a resource exists in the same path,
	 * the resource is deleted.
	 */
	protected void createFile(IFileStore target, String content) throws CoreException {
		target.delete(EFS.NONE, null);
		InputStream input = new ByteArrayInputStream(content.getBytes());
		transferData(input, target.openOutputStream(EFS.NONE, null));
		IFileInfo info = target.fetchInfo();
		assertTrue(info.exists() && !info.isDirectory());
	}

	/**
	 * Create a file with random content. If a resource exists in the same path,
	 * the resource is deleted.
	 */
	protected void createIOFile(java.io.File target, String content) throws IOException {
		target.delete();
		InputStream input = new ByteArrayInputStream(content.getBytes());
		transferData(input, new FileOutputStream(target));
		assertTrue(target.exists() && !target.isDirectory());
	}

	protected void createNode(IFileStore node) throws CoreException {
		char type = node.getName().charAt(0);
		if (type == 'd')
			node.mkdir(EFS.NONE, null);
		else {
			InputStream input = getRandomContents();
			OutputStream output = node.openOutputStream(EFS.NONE, null);
			transferData(input, output);
		}
	}

	protected void createTree(IFileStore[] tree) throws CoreException {
		for (int i = 0; i < tree.length; i++)
			createNode(tree[i]);
	}

	/**
	 * The returned array will have at least the specified size.
	 */
	protected byte[] getBigContents(int size) {
		return getBigString(size).getBytes();
	}

	/**
	 * The returned array will have at least the specified size.
	 */
	protected String getBigString(int size) {
		StringBuffer sb = new StringBuffer();
		while (sb.length() < size)
			sb.append(getRandomString());
		return sb.toString();
	}

	public FileSystemResourceManager getLocalManager() {
		return ((Workspace) getWorkspace()).getFileSystemManager();
	}

	protected IFileStore[] getTree(IFileStore root) {
		return getTree(root, getTreeElements());
	}

	protected IFileStore[] getTree(IFileStore root, String[] elements) {
		IFileStore[] tree = new IFileStore[elements.length];
		for (int i = 0; i < elements.length; i++)
			tree[i] = root.getChild(elements[i]);
		return tree;
	}

	protected String[] getTreeElements() {
		String[] tree = new String[10];
		tree[0] = "d-folder";
		tree[1] = tree[0] + File.separator + "d-subfolder";
		tree[2] = tree[0] + File.separator + "f-file";
		tree[3] = tree[1] + File.separator + "f-anotherFile";
		tree[4] = tree[1] + File.separator + "d-subfolder";
		tree[5] = "d-1";
		tree[6] = "d-2";
		tree[7] = "f-3";
		tree[8] = "f-4";
		tree[9] = "f-5";
		return tree;
	}

	protected void setUp() throws Exception {
		super.setUp();

		projectNames = new String[numberOfProjects];
		projects = new IProject[numberOfProjects];
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < projectNames.length; i++) {
					projectNames[i] = "Project" + i;
					projects[i] = getWorkspace().getRoot().getProject(projectNames[i]);
					projects[i].create(null);
					projects[i].open(null);
				}
			}
		}, null);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Copy the data from the input stream to the output stream.
	 * Close just the input stream.
	 */
	public void transferDataWithoutCloseStreams(InputStream input, OutputStream output) {
		try {
			int c = 0;
			while ((c = input.read()) != -1) {
				output.write(c);
			}
			//input.close();
			//output.close();
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(e.toString(), false);
		}
	}

	protected boolean verifyNode(IFileStore node) {
		char type = node.getName().charAt(0);
		//if the name starts with d it must be a directory
		return (type == 'd') == node.fetchInfo().isDirectory();
	}

	protected boolean verifyTree(IFileStore[] tree) {
		for (int i = 0; i < tree.length; i++)
			if (!verifyNode(tree[i]))
				return false;
		return true;
	}
}
