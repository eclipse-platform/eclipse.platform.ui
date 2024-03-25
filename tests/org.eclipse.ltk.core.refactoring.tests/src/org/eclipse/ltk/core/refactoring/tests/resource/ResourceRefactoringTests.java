/*******************************************************************************
 * Copyright (c) 2008, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin <eclipse.sprigogin@gmail.com> - [refactoring] Provide a way to implement refactorings that depend on resources that have to be explicitly released - https://bugs.eclipse.org/347599
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveRenameResourceDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class ResourceRefactoringTests {
	private SimpleTestProject fProject;

	@Before
	public void setUp() throws Exception {
		fProject= new SimpleTestProject();
	}

	@After
	public void tearDown() throws Exception {
		fProject.delete();
	}

	@Test
	public void testMoveChange1() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		Change undoChange= perform(new MoveResourceChange(file, destination));

		IResource movedResource= assertMove(file, destination, content);

		perform(undoChange);

		assertMove(movedResource, file.getParent(), content);
	}

	@Test
	public void testMoveChange2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		Change undoChange= perform(new MoveResourceChange(testFolder, destination));


		IFolder movedResource= (IFolder) assertMove(testFolder, destination, null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(movedResource, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	@Test
	public void testMoveChange3() throws Exception {
		// move with overwrite

		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		Change undoChange= perform(new MoveResourceChange(file1, destination));

		IResource movedResource= assertMove(file1, destination, content1);

		perform(undoChange);

		assertMove(movedResource, file1.getParent(), content1);

		assertEquals(content2, fProject.getContent(file2));
	}

	@Test
	public void testMoveRefactoring1() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { file });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IResource movedResource= assertMove(file, destination, content);

		perform(undoChange);

		assertMove(movedResource, file.getParent(), content);
	}

	@Test
	public void testMoveRefactoring2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { testFolder });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IFolder movedResource= (IFolder) assertMove(testFolder, destination, null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(movedResource, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	@Test
	public void testMoveRefactoring3() throws Exception {
		// move with overwrite

		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveResourcesDescriptor.ID);
		MoveResourcesDescriptor descriptor= (MoveResourcesDescriptor) contribution.createDescriptor();

		descriptor.setResourcesToMove(new IResource[] { file1 });
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IResource movedResource= assertMove(file1, destination, content1);

		perform(undoChange);

		assertMove(movedResource, file1.getParent(), content1);
		assertEquals(content2, fProject.getContent(file2));
	}

	@Test
	public void testMoveRenameRefactoring1() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveRenameResourceDescriptor.ID);
		MoveRenameResourceDescriptor descriptor= (MoveRenameResourceDescriptor) contribution.createDescriptor();

		descriptor.setResourcePath(file.getFullPath());
		descriptor.setNewName("myFile2.txt");
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		assertMoveRename(file, destination, "myFile2.txt", content);

		perform(undoChange);

		assertMove(file, file.getParent(), content);
	}

	@Test
	public void testMoveRenameRefactoring2() throws Exception {

		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveRenameResourceDescriptor.ID);
		MoveRenameResourceDescriptor descriptor= (MoveRenameResourceDescriptor) contribution.createDescriptor();

		descriptor.setResourcePath(testFolder.getFullPath());
		descriptor.setNewName("test2");
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		IFolder movedResource= (IFolder) assertMoveRename(testFolder, destination, "test2", null);
		assertTrue(movedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertMove(testFolder, testFolder.getParent(), null);
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	@Test
	public void testMoveRenameRefactoring3() throws Exception {
		// move with overwrite

		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile2.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(MoveRenameResourceDescriptor.ID);
		MoveRenameResourceDescriptor descriptor= (MoveRenameResourceDescriptor) contribution.createDescriptor();

		descriptor.setResourcePath(file1.getFullPath());
		descriptor.setNewName("myFile2.txt");
		descriptor.setDestination(destination);

		Change undoChange= perform(descriptor);

		assertMoveRename(file1, destination, "myFile2.txt", content1);

		perform(undoChange);

		assertMove(file1, file1.getParent(), content1);
		assertEquals(content2, fProject.getContent(file2));
	}

	@Test
	public void testDeleteRefactoring1_bug343584() throws Exception {
		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", "hello");

		IProject testProject2= ResourcesPlugin.getWorkspace().getRoot().getProject(SimpleTestProject.TEST_PROJECT_NAME + "2");
		try {
			testProject2.create(null);
			testProject2.open(null);

			RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
			DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor)contribution.createDescriptor();

			descriptor.setDeleteContents(true);
			descriptor.setResources(new IResource[] { fProject.getProject(), testProject2 });

			perform(descriptor);

			assertFalse(fProject.getProject().exists());
			assertFalse(testProject2.exists());
		} finally {
			testProject2.delete(true, true, null);
		}
	}

	@Test
	public void testDeleteRefactoring2_bug343584() throws Exception {
		IPath location= fProject.getProject().getLocation();
		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", "hello");

		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		String p2Name= "p2";
		IProjectDescription p2Description= workspace.newProjectDescription(p2Name);
		p2Description.setLocation(location.append(p2Name));
		IProject p2= workspace.getRoot().getProject(p2Name);
		p2.create(p2Description, null);
		p2.open(null);
		IPath p2Location= p2.getLocation();

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor) contribution.createDescriptor();

		descriptor.setDeleteContents(true);
		descriptor.setResources(new IResource[] { fProject.getProject(), p2 });

		perform(descriptor);

		assertFalse(fProject.getProject().exists());
		assertFalse(p2.exists());

		assertFalse(location.toFile().exists());
		assertFalse(p2Location.toFile().exists());
	}

	@Test
	public void testDeleteRefactoring3_bug343584() throws Exception {
		IPath location= fProject.getProject().getLocation();
		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", "hello");
		IPath fileLocation= file.getLocation();

		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		String p2Name= "p2";
		IProjectDescription p2Description= workspace.newProjectDescription(p2Name);
		p2Description.setLocation(location.append(p2Name));
		IProject p2= workspace.getRoot().getProject(p2Name);
		p2.create(p2Description, null);
		p2.open(null);
		IPath p2Location= p2.getLocation();

		try {
			RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
			DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor) contribution.createDescriptor();

			descriptor.setDeleteContents(false);
			descriptor.setResources(new IResource[] { fProject.getProject(), p2 });

			perform(descriptor);

			assertFalse(fProject.getProject().exists());
			assertFalse(p2.exists());

			assertTrue(location.toFile().exists());
			assertTrue(fileLocation.toFile().exists());
			assertTrue(p2Location.toFile().exists());

		} finally {
			EFS.getLocalFileSystem().getStore(location).delete(EFS.NONE, null);
			EFS.getLocalFileSystem().getStore(p2Location).delete(EFS.NONE, null);
		}
	}

	private Change perform(Change change) throws CoreException {
		PerformChangeOperation op= new PerformChangeOperation(change);
		op.run(null);
		assertTrue(op.changeExecuted());
		return op.getUndoChange();
	}

	private Change perform(RefactoringDescriptor descriptor) throws CoreException {
		RefactoringStatus status= new RefactoringStatus();
		final RefactoringContext context= descriptor.createRefactoringContext(status);
		try {
			final Refactoring refactoring= context != null ? context.getRefactoring() : null;
			assertTrue(status.isOK());

			PerformRefactoringOperation op= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
			op.run(null);
			RefactoringStatus validationStatus= op.getValidationStatus();
			assertFalse(validationStatus.hasFatalError());
			assertFalse(validationStatus.hasError());
			return op.getUndoChange();
		} finally {
			if (context != null)
				context.dispose();
		}
	}

	private IResource assertMove(IResource source, IContainer destination, String content) throws CoreException, IOException {
		IResource res= destination.findMember(source.getName());

		assertNotNull(res);
		assertEquals(res.getType(), source.getType());

		if (res instanceof IFile) {
			assertEquals(content, fProject.getContent((IFile) res));
		}
		return res;
	}

	private IResource assertMoveRename(IResource source, IContainer destination, String newName, String content) throws CoreException, IOException {
		IResource res= destination.findMember(newName);

		assertNotNull(res);
		assertEquals(res.getType(), source.getType());

		if (res instanceof IFile) {
			assertEquals(content, fProject.getContent((IFile) res));
		}
		return res;
	}
}
