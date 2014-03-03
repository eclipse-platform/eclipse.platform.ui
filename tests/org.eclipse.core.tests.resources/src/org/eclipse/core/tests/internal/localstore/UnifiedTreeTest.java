/*******************************************************************************
 *  Copyright (c) 2000, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.*;
import java.net.URI;
import java.util.Hashtable;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * 
 */
public class UnifiedTreeTest extends LocalStoreTest {
	protected static int limit = 10;

	public UnifiedTreeTest() {
		super();
	}

	public UnifiedTreeTest(String name) {
		super(name);
	}

	protected void createFiles(IFileStore folder, Hashtable set) throws Exception {
		for (int i = 0; i < limit; i++) {
			IFileStore child = folder.getChild("fsFile" + i);
			OutputStream out = null;
			try {
				out = child.openOutputStream(EFS.NONE, null);
				out.write("contents".getBytes());
			} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					//ignore
				}
			}
			set.put(child.toString(), "");
		}
	}

	protected void createFiles(final IContainer target, final Hashtable set) throws CoreException {
		final Workspace workspace = (Workspace) getWorkspace();
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < limit; i++) {
					IFile child = target.getFile(new Path("wbFile" + i));
					workspace.createResource(child, false);
					String location = child.getLocation().toOSString();
					set.put(location, "");
				}
			}
		};
		workspace.run(operation, null);
	}

	protected void createResourcesInFileSystem(IFileStore folder, Hashtable set) throws Exception {
		createFiles(folder, set);
		for (int i = 0; i < limit; i++) {
			IFileStore child = folder.getChild("fsFolder" + i);
			child.mkdir(EFS.NONE, null);
			set.put(child.toString(), "");
			if (i < (limit / 2))
				createFiles(child, set);
		}
	}

	protected void createResourcesInWorkspace(IContainer target, Hashtable set) throws CoreException {
		createFiles(target, set);
		for (int i = 0; i < limit; i++) {
			IFolder child = target.getFolder(new Path("wbFolder" + i));
			child.create(true, true, null);
			String location = child.getLocation().toOSString();
			set.put(location, "");
			if (i < (limit / 2))
				createFiles(child, set);
		}
	}

	public static Test suite() {
		//TestSuite suite = new TestSuite();
		//suite.addTest(new UnifiedTreeTest("testTraverseMechanismInProjectWithMappings"));
		//return suite;
		return new TestSuite(UnifiedTreeTest.class);
	}

	/**
	 * Creates some resources in the file system and some in the workspace. After that,
	 * makes sure the visitor is going to walk through all of them.
	 */
	public void testTraverseMechanismInFolder() throws Throwable {
		/* create common objects */
		IProject project = projects[0];
		IFolder folder = project.getFolder("root");
		folder.create(true, true, null);

		/* Create a hash table to hold all resources the tree should visit.
		 The resources are going to be removed from the hash table as
		 the visitor visits it. */
		final Hashtable set = new Hashtable();

		/* create some workspace structure */
		createResourcesInWorkspace(folder, set);

		/* create some file system structure */
		createResourcesInFileSystem(((Resource) folder).getStore(), set);

		/* create a visitor */
		IUnifiedTreeVisitor visitor = new IUnifiedTreeVisitor() {
			public boolean visit(UnifiedTreeNode node) {
				/* test the node.getLocalName() method */
				final IResource resource = node.getResource();
				final IFileStore store = ((Resource) resource).getStore();
				if (node.existsInFileSystem())
					assertEquals("1.0", store.fetchInfo().getName(), node.getLocalName());
				assertEquals("1.1", store, node.getStore());

				/* remove from the hash table the resource we're visiting */
				set.remove(resource.getLocation().toOSString());
				return true;
			}
		};

		/* instantiate a unified tree and use the visitor */
		UnifiedTree tree = new UnifiedTree(folder);
		tree.accept(visitor);

		/* if the hash table is empty, we walked through all resources */
		assertTrue("2.0", set.isEmpty());
	}

	/**
	 * Creates some resources in the file system and some in the workspace. After that,
	 * makes sure the visitor is going to walk through some of them.
	 */
	public void testTraverseMechanismInFolderSkippingSomeChildren() throws Throwable {
		/* create common objects */
		IProject project = projects[0];
		IFolder folder = project.getFolder("root");
		folder.create(true, true, null);

		/* Create a hash table to hold all resources the tree should visit.
		 The resources are going to be removed from the hash table as
		 the visitor visits it. */
		final Hashtable set = new Hashtable();

		/* create some workspace structure */
		createResourcesInWorkspace(folder, set);

		/* create some file system structure */
		createResourcesInFileSystem(((Resource) folder).getStore(), set);

		/* create a visitor */
		IUnifiedTreeVisitor visitor = new IUnifiedTreeVisitor() {
			public boolean visit(UnifiedTreeNode node) {

				/* test the node.getLocalName() method */
				final IResource resource = node.getResource();
				IFileStore store = ((Resource) resource).getStore();
				String key = store.fetchInfo().getName();
				if (node.existsInFileSystem())
					assertEquals("1.0", key, node.getLocalName());
				assertEquals("1.1", store, node.getStore());

				/* force children to be added to the queue */
				node.getChildren();

				/* skip some resources */
				if (resource.getName().startsWith("fsFolder"))
					return false;

				/* remove from the hash table the resource we're visiting */
				set.remove(resource.getLocation().toOSString());
				return true;
			}
		};

		/**/
		int initialSize = set.size();

		/* instantiate a unified tree and use the visitor */
		UnifiedTree tree = new UnifiedTree(folder);
		tree.accept(visitor);

		/* if the hash table is empty, we walked through all resources */
		assertTrue("2.0", !set.isEmpty());
		assertTrue("2.1", set.size() != initialSize);
	}

	/**
	 * Creates some resources in the file system and some in the workspace. After that,
	 * makes sure the visitor is going to walk through all of them.
	 */
	public void testTraverseMechanismInProject() throws Throwable {
		/* create common objects */
		IProject project = projects[0];

		/* Create a hash table to hold all resources the tree should visit.
		 The resources are going to be removed from the hash table as
		 the visitor visits it. */
		final Hashtable set = new Hashtable();

		/* create some workspace structure */
		createResourcesInWorkspace(project, set);

		/* create some file system structure */
		createResourcesInFileSystem(((Resource) project).getStore(), set);

		/* create a visitor */
		IUnifiedTreeVisitor visitor = new IUnifiedTreeVisitor() {
			public boolean visit(UnifiedTreeNode node) {
				/* test the node.getLocalName() method */
				final IResource resource = node.getResource();
				IFileStore store = ((Resource) resource).getStore();
				if (node.existsInFileSystem())
					assertEquals("1.0", store.fetchInfo().getName(), node.getLocalName());
				assertEquals("1.1", store, node.getStore());
				/* remove from the hash table the resource we're visiting */
				set.remove(resource.getLocation().toOSString());
				return true;
			}
		};

		/* instantiate a unified tree and use the visitor */
		UnifiedTree tree = new UnifiedTree(project);
		tree.accept(visitor);

		/* if the hash table is empty, we walked through all resources */
		assertTrue("2.0", set.isEmpty());
	}

	/**
	 * Regression test for 342968 - Resource layers asks IFileTree for info of linked resources
	 */
	public void test342968() throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
		ensureExistsInWorkspace(project, true);
		project.open(getMonitor());

		IProjectDescription description = project.getDescription();
		URI projectLocation = Test342968FileSystem.getTestUriFor(EFS.getLocalFileSystem().fromLocalFile(new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), "test")).toURI());
		description.setLocationURI(projectLocation);

		project.delete(false, false, null);

		project.create(description, IResource.NONE, null);
		project.open(getMonitor());

		assertTrue(project.getLocationURI().equals(projectLocation));

		IFolder link = project.getFolder("link");

		File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile(), "link");
		file.mkdir();

		link.createLink(EFS.getLocalFileSystem().fromLocalFile(file).toURI(), IResource.NONE, null);

		IFile rf = link.getFile("fileTest342968.txt");
		rf.create(new ByteArrayInputStream("test342968".getBytes()), false, null);
		assertTrue("1.0", rf.exists());

		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		assertTrue("2.0", rf.exists());
	}

	public void test368376() throws CoreException, IOException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getUniqueString());
		ensureExistsInWorkspace(project, true);

		String filePath = "a/b/c/file.txt";
		File javaFile = new File(project.getLocation().toFile(), filePath);
		assertTrue(javaFile.getParentFile().mkdirs());
		assertTrue(javaFile.createNewFile());

		IFolder folder = project.getFolder("a");
		IFile file = project.getFile(filePath);
		assertFalse(folder.exists());
		assertFalse(file.exists());

		file.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());

		assertTrue(folder.exists());
		assertTrue(file.exists());
		assertTrue(folder.isSynchronized(IResource.DEPTH_INFINITE));

		project.delete(true, getMonitor());
	}
}
