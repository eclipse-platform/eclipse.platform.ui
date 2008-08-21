/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Oberhuber (Wind River) - initial API and implementation for [232426]
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 */
public class SymlinkResourceTest extends LocalStoreTest {

	public SymlinkResourceTest() {
		super();
	}

	public SymlinkResourceTest(String name) {
		super(name);
	}

	public static Test suite() {
		//TestSuite suite = new TestSuite();
		//suite.addTest(new SymlinkResourceTest("testBug232426"));
		//return suite;
		return new TestSuite(SymlinkResourceTest.class);
	}
	
	protected void mkLink(IFileStore dir, String src, String tgt, boolean isDir) {
		try {
			createSymLink(dir.toLocalFile(EFS.NONE, getMonitor()), src, tgt, isDir);
		} catch (CoreException e) {
			fail("mkLink", e);
		}
	}

	protected void createBug232426Structure(IFileStore rootDir) throws CoreException {
		IFileStore folderA = rootDir.getChild("a");
		IFileStore folderB = rootDir.getChild("b");
		IFileStore folderC = rootDir.getChild("c");
		folderA.mkdir(EFS.NONE,getMonitor());
		folderB.mkdir(EFS.NONE,getMonitor());
		folderC.mkdir(EFS.NONE,getMonitor());
		
		/* create symbolic links */
		mkLink(folderA, "link", "../b", true);
		mkLink(folderB, "linkA", "../a", true);
		mkLink(folderB, "linkC", "../c", true);
		mkLink(folderC, "link", "../b", true);
	}

	/**
	 * Test a very specific case of mutually recursive symbolic links:
	 * <pre>
	 *   a/link  -> ../b
	 *   b/link1 -> ../a, b/link2 -> ../c
	 *   c/link  -> ../b
	 * </pre>
	 * In the specific bug, the two links in b were followed in an alternated
	 * fashion while walking down the tree. A correct implementation should
	 * stop following symbolic links as soon as a node is reached that has
	 * been visited before.
	 * See <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=232426">bug 232426</a> 
	 */
	public void testBug232426() throws Exception {
		/* Only run the test if EFS supports symbolic links on this Platform */
		if ( (EFS.getLocalFileSystem().attributes() & EFS.ATTRIBUTE_SYMLINK) == 0) {
			return;
		}
		
		/* Re-use projects which are cleaned up automatically */ 
		final IProject project = projects[0];
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				/* delete open project because we must re-open with BACKGROUND_REFRESH */
				project.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, getMonitor());
				project.create(null);
				createBug232426Structure(EFS.getStore(project.getLocationURI()));
				//Bug only happens with BACKGROUND_REFRESH.
				project.open(IResource.BACKGROUND_REFRESH, getMonitor());
			}
		}, null);
		
		//wait for BACKGROUND_REFRESH to complete.
		waitForRefresh();
		project.accept(new IResourceVisitor() {
			int resourceCount = 0;
			public boolean visit(IResource resource) {
				resourceCount++;
				//We have 1 root + 3 folders + 4 elements --> 8 elements to visit at most
				assertTrue(resourceCount <= 8);
				return true;
			}
		});
	}
	
}
