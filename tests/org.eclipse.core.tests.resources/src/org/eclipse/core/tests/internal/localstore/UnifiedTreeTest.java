package org.eclipse.core.tests.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Policy;
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
protected void createFiles(File folder, Hashtable set) throws CoreException {
	FileSystemStore store = new FileSystemStore();
	for (int i = 0; i < limit; i++) {
		File child = new File(folder, "fsFile" + i);
		InputStream input = new ByteArrayInputStream("contents".getBytes());
		store.write(child, input, false, Policy.monitorFor(null));
		String location = child.getAbsolutePath();
		set.put(location, "");
	}
}
protected void createFiles(final IContainer target, final Hashtable set) throws CoreException {
	final Workspace workspace = (Workspace) this.getWorkspace();
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
protected void createResourcesInFileSystem(File folder, Hashtable set) throws CoreException {
	createFiles(folder, set);
	for (int i = 0; i < limit; i++) {
		File child = new File(folder, "fsFolder" + i);
		child.mkdirs();
		String location = child.getAbsolutePath();
		set.put(location, "");
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

	/* Create a hashtable to hold all resources the tree should visit.
	   The resources are going to be removed from the hashtable as
	   the visitor visits it. */
	final Hashtable set = new Hashtable();

	/* create some workspace structure */
	createResourcesInWorkspace(folder, set);

	/* create some file system structure */
	createResourcesInFileSystem(folder.getLocation().toFile(), set);

	/* create a visitor */
	IUnifiedTreeVisitor visitor = new IUnifiedTreeVisitor() {
		public boolean visit(UnifiedTreeNode node) throws CoreException {

			/* test the node.getLocalLocation() method */
			String key = node.getLocalLocation();
			assertEquals("1.0", node.getResource().getLocation().toOSString(), key);

			/* remove from the hashtable the resource we're visiting */
			set.remove(key);
			return true;
		}
	};

	/* instantiate a unified tree and use the visitor */
	UnifiedTree tree = new UnifiedTree(folder);
	tree.accept(visitor);

	/* if the hashtable is empty, we walked through all resources */
	assertTrue("2.0", set.isEmpty());
}
/**
 * Creates some resources in the file system and some in the workspace. After that,
 * makes sure the visitor is going to walk through some of them.
 */
public void testTraverseMechanismInFolderSkipingSomeChildren() throws Throwable {
	/* create common objects */
	IProject project = projects[0];
	IFolder folder = project.getFolder("root");
	folder.create(true, true, null);

	/* Create a hashtable to hold all resources the tree should visit.
	   The resources are going to be removed from the hashtable as
	   the visitor visits it. */
	final Hashtable set = new Hashtable();

	/* create some workspace structure */
	createResourcesInWorkspace(folder, set);

	/* create some file system structure */
	createResourcesInFileSystem(folder.getLocation().toFile(), set);

	/* create a visitor */
	IUnifiedTreeVisitor visitor = new IUnifiedTreeVisitor() {
		public boolean visit(UnifiedTreeNode node) throws CoreException {

			/* test the node.getLocalLocation() method */
			String key = node.getLocalLocation();
			assertEquals("1.0", node.getResource().getLocation().toOSString(), key);;

			/* force children to be added to the queue */
			node.getChildren();

			/* skip some resources */
			if (node.getResource().getName().startsWith("fsFolder"))
				return false;

			/* remove from the hashtable the resource we're visiting */
			set.remove(key);
			return true;
		}
	};


	/**/
	int initialSize = set.size();

	/* instantiate a unified tree and use the visitor */
	UnifiedTree tree = new UnifiedTree(folder);
	tree.accept(visitor);

	/* if the hashtable is empty, we walked through all resources */
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

	/* Create a hashtable to hold all resources the tree should visit.
	   The resources are going to be removed from the hashtable as
	   the visitor visits it. */
	final Hashtable set = new Hashtable();

	/* create some workspace structure */
	createResourcesInWorkspace(project, set);

	/* create some file system structure */
	createResourcesInFileSystem(project.getLocation().toFile(), set);

	/* create a visitor */
	IUnifiedTreeVisitor visitor = new IUnifiedTreeVisitor() {
		public boolean visit(UnifiedTreeNode node) throws CoreException {

			/* test the node.getLocalLocation() method */
			String key = node.getLocalLocation();
			assertEquals("1.0", node.getResource().getLocation().toOSString(), key);
			/* remove from the hashtable the resource we're visiting */
			set.remove(key);
			return true;
		}
	};

	/* instantiate a unified tree and use the visitor */
	UnifiedTree tree = new UnifiedTree(project);
	tree.accept(visitor);

	/* if the hashtable is empty, we walked through all resources */
	assertTrue("2.0", set.isEmpty());
}
}
