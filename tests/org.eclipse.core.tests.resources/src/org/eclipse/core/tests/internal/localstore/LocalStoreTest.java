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
package org.eclipse.core.tests.internal.localstore;

import java.io.*;
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
	protected void createFile(File target, String content) throws IOException {
		Workspace.clear(target);
		InputStream input = new ByteArrayInputStream(content.getBytes());
		FileOutputStream output = new FileOutputStream(target);
		transferData(input, output);
		assertTrue(target.isFile());
	}

	protected void createNode(File node) throws IOException {
		char type = node.getName().charAt(0);
		if (type == 'd')
			node.mkdirs();
		else {
			InputStream input = getRandomContents();
			FileOutputStream output = new FileOutputStream(node);
			transferData(input, output);
		}
	}

	protected void createTree(File[] tree) throws IOException {
		for (int i = 0; i < tree.length; i++)
			createNode(tree[i]);
	}

	/**
	 * The returned arry will have at least the specified size.
	 */
	protected byte[] getBigContents(int size) {
		return getBigString(size).getBytes();
	}

	/**
	 * The returned arry will have at least the specified size.
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

	protected File[] getTree(File root) {
		return getTree(root, getTreeElements());
	}

	protected File[] getTree(File root, String[] elements) {
		File[] tree = new File[elements.length];
		for (int i = 0; i < elements.length; i++)
			tree[i] = new File(root, elements[i]);
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
		getWorkspace().getRoot().delete(true, true, getMonitor());
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

	protected boolean verifyNode(File node) {
		char type = node.getName().charAt(0);
		if (type == 'd')
			return true == node.isDirectory();
		else
			return true == node.isFile();
	}

	protected boolean verifyTree(File[] tree) {
		for (int i = 0; i < tree.length; i++)
			if (!verifyNode(tree[i]))
				return false;
		return true;
	}
}