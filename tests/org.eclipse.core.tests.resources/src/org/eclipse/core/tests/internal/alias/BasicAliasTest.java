/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.alias;

import java.util.Arrays;
import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

/**
 * Tests basic API methods in the face of aliased resources, and ensures that
 * nothing is ever out of sync.
 */
public class BasicAliasTest extends EclipseWorkspaceTest {
	//resource handles (p=project, f=folder, l=file)
	private IProject pNoOverlap;
	private IProject pOverlap;
	private IProject pLinked;
	private IFolder fOverlap;
	private IFile lChildOverlap;
	private IFile lOverlap;
	private IFolder fLinked;
	private IFile lLinked;
	private IFile lChildLinked;
	
	public static Test suite() {
		return new TestSuite(BasicAliasTest.class);
	}
	public BasicAliasTest() {
		super();
	}
	public BasicAliasTest(String name) {
		super(name);
	}
	/**
	 * Asserts that the two given resources are duplicates in the file system.
	 * Asserts that both have same location, and same members.  Also asserts
	 * that both resources are in sync with the file system.  The resource names
	 * in the tree may be different.
	 */
	public void assertOverlap(String message, IResource resource1, IResource resource2) {
		String errMsg = message + resource1.getFullPath().toString();
		assertEquals(errMsg + "(location)", resource1.getLocation(), resource2.getLocation());
		assertTrue(errMsg + "(sync)", resource1.isSynchronized(IResource.DEPTH_ZERO));
		assertTrue(errMsg + "(sync)", resource2.isSynchronized(IResource.DEPTH_ZERO));

		IResource[] children1 = null;
		IResource[] children2 = null;
		try {
			children1 = getSortedChildren(resource1);
			children2 = getSortedChildren(resource2);
		} catch (CoreException e) {
			fail(errMsg, e);
		}
		assertEquals(errMsg + "(child count)", children1.length, children2.length);
		for (int i = 0; i < children2.length; i++) {
			assertOverlap(message, children1[i], children2[i]);
		}
	}
	/**
	 * Returns the children of the given resource, sorted in a consistent
	 * alphabetical order.
	 */
	private IResource[] getSortedChildren(IResource resource) throws CoreException {
		if (!(resource instanceof IContainer))
			return new IResource[0];
		IResource[] children = ((IContainer)resource).members();
		Arrays.sort(children, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				return ((IResource)arg0).getFullPath().toString().compareTo(((IResource)arg1).getFullPath().toString());
			}
		});
		return children;
	}
	protected void setUp() throws Exception {
		super.setUp();
		IWorkspaceRoot root = getWorkspace().getRoot();
		//project with no overlap
		pNoOverlap = root.getProject("NoOverlap");
		ensureExistsInWorkspace(pNoOverlap, true);
		ensureExistsInWorkspace(buildResources(pNoOverlap, new String[] {"/1/", "/1/1", "/1/2", "/2/", "/2/1"}), true);

		//project with overlap		
		pOverlap= root.getProject("Overlap");
		ensureExistsInWorkspace(pOverlap, true);
		fOverlap = pOverlap.getFolder("fOverlap");
		IFolder f2 = pOverlap.getFolder("F2");
		lOverlap = f2.getFile("lOverlap");
		lChildOverlap = fOverlap.getFile("lChildOverlap");
		ensureExistsInWorkspace(new IResource[] {fOverlap, f2, lOverlap, lChildOverlap}, true);
		//create some other random child elements
		ensureExistsInWorkspace(buildResources(pOverlap, new String[] {"/1/", "/1/1", "/1/2"}), true);
		ensureExistsInWorkspace(buildResources(f2, new String[] {"/1/", "/1/1", "/1/2"}), true);
		ensureExistsInWorkspace(buildResources(fOverlap, new String[] {"/1/", "/1/1", "/1/2"}), true);
		
		//create links
		pLinked= root.getProject("LinkProject");
		ensureExistsInWorkspace(pLinked, true);
		fLinked = pLinked.getFolder("LinkedFolder");
		lLinked = pLinked.getFile("LinkedFile");
		lChildLinked = fLinked.getFile(lChildOverlap.getName());
		fLinked.createLink(fOverlap.getLocation(), IResource.NONE, null);
		lLinked.createLink(lOverlap.getLocation(), IResource.NONE, null);
		ensureExistsInWorkspace(lChildLinked, true);
		ensureExistsInWorkspace(buildResources(pLinked, new String[] {"/a/", "/a/a", "/a/b"}), true);
		ensureExistsInWorkspace(buildResources(fLinked, new String[] {"/a/", "/a/a", "/a/b"}), true);
	}
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public void testCloseOpenProject() {
	}
	/**
	 * Tests adding a file to a duplicate region by copying.
	 */
	public void testCopyFile() {
		IFile sourceFile = pNoOverlap.getFile("CopySource");
		ensureExistsInWorkspace(sourceFile, true);
		
		//file in linked folder
		try {
			IFile linkDest = fLinked.getFile("CopyDestination");
			IFile overlapDest = fOverlap.getFile(linkDest.getName());
			
			sourceFile.copy(linkDest.getFullPath(), IResource.NONE, getMonitor());
			assertTrue("1.1", linkDest.exists());
			assertTrue("1.2", overlapDest.exists());
			assertOverlap("1.3", linkDest, overlapDest);

			linkDest.delete(IResource.NONE, getMonitor());
			assertTrue("1.4", !linkDest.exists());
			assertTrue("1.5", !overlapDest.exists());
			assertOverlap("1.6", linkDest, overlapDest);
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//duplicate file
		try {
			IFile linkDest = lLinked;
			IFile overlapDest = lOverlap;
			//first delete the file, then copy it back
			overlapDest.delete(IResource.NONE, getMonitor());
			//the link will still exist, but the location won't
			assertTrue("2.1", linkDest.exists());
			assertTrue("2.2", !overlapDest.exists());
			assertTrue("2.3", !linkDest.getLocation().toFile().exists());
			assertOverlap("2.4", linkDest, overlapDest);

			sourceFile.copy(overlapDest.getFullPath(), IResource.NONE, getMonitor());
			assertTrue("2.4", linkDest.exists());
			assertTrue("2.5", overlapDest.exists());
			assertOverlap("2.6", linkDest, overlapDest);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//file in duplicate folder
		try {
			IFile linkDest = fLinked.getFile("CopyDestination");
			IFile overlapDest = fOverlap.getFile(linkDest.getName());

			sourceFile.copy(overlapDest.getFullPath(), IResource.NONE, getMonitor());
			assertTrue("3.1", linkDest.exists());
			assertTrue("3.2", overlapDest.exists());
			assertOverlap("3.3", linkDest, overlapDest);

			overlapDest.delete(IResource.NONE, getMonitor());
			assertTrue("3.4", !linkDest.exists());
			assertTrue("3.5", !overlapDest.exists());
			assertOverlap("3.6", linkDest, overlapDest);
		} catch (CoreException e) {
			fail("3.99", e);
		}
	}
	public void testCopyFolder() {
	}
	public void testCreateDeleteFile() {
		//file in linked folder
		try {
			lChildLinked.delete(IResource.NONE, getMonitor());
			assertOverlap("1.1", lChildLinked, lChildOverlap);
			lChildLinked.create(getRandomContents(), IResource.NONE, getMonitor());
			assertOverlap("1.2", lChildLinked, lChildOverlap);
		} catch (CoreException e) {
			fail("1.99", e);
		}
		//duplicate file
		try {
			lOverlap.delete(IResource.NONE, getMonitor());
			assertOverlap("2.1", lLinked, lOverlap);
			//now the linked resource will still exist but its local contents won't
			assertTrue("2.2", lLinked.exists());
			assertTrue("2.3", !lLinked.getLocation().toFile().exists());
			try {
				lLinked.setContents(getRandomContents(), IResource.NONE, getMonitor());
				//should fail
				fail("2.4");
			} catch (CoreException e) {
				//should fail
			}
			lOverlap.create(getRandomContents(), IResource.NONE, getMonitor());
			assertOverlap("2.5", lLinked, lOverlap);
		} catch (CoreException e) {
			fail("2.99", e);
		}
		//file in duplicate folder
		try {
			lChildOverlap.delete(IResource.NONE, getMonitor());
			assertOverlap("1.1", lChildLinked, lChildOverlap);
			lChildOverlap.create(getRandomContents(), IResource.NONE, getMonitor());
			assertOverlap("1.2", lChildLinked, lChildOverlap);
		} catch (CoreException e) {
			fail("3.0", e);
		}
	}
	public void testCreateDeleteFolder() {
	}
	public void testCreateDeleteLink() {
	}
	public void testCreateOpenProject() {
	}
	public void testDeepCopyLink() {
	}
	public void testDeepMoveLink() {
	}
	public void testDeleteLink() {
	}
	public void testDeleteProject() {
	}
	public void testDeleteProjectContents() {
	}
	public void testFileAppendContents() {
		//linked file
		try {
			lLinked.appendContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertOverlap("1.1", lLinked, lOverlap);

		//file in linked folder
		try {
			lChildLinked.appendContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertOverlap("2.1", lChildLinked, lChildOverlap);
		//duplicate file
		try {
			lOverlap.appendContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertOverlap("3.1", lLinked, lOverlap);
		//file in duplicate folder
		try {
			lChildOverlap.appendContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertOverlap("3.1", lChildLinked, lChildOverlap);
	}
	public void testFileSetContents() {
		//linked file
		try {
			lLinked.setContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("1.0", e);
		}
		assertOverlap("1.1", lLinked, lOverlap);
		
		//file in linked folder
		try {
			lChildLinked.setContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("2.0", e);
		}
		assertOverlap("2.1", lChildLinked, lChildOverlap);
		//duplicate file
		try {
			lOverlap.setContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertOverlap("3.1", lLinked, lOverlap);
		//file in duplicate folder
		try {
			lChildOverlap.setContents(getRandomContents(), IResource.NONE, getMonitor());
		} catch (CoreException e) {
			fail("3.0", e);
		}
		assertOverlap("3.1", lChildLinked, lChildOverlap);
	}
	public void testMoveFile() {
	}
	public void testMoveFolder() {
	}
	public void testShallowCopyLink() {
	}
	public void testShallowMoveLink() {
	}
	public void testShallowDeleteProject() {
	}
}