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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.core.filesystem.EFS;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;

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
import org.eclipse.ltk.core.refactoring.participants.ReorgExecutionLog;
import org.eclipse.ltk.core.refactoring.resource.CopyProjectDescriptor;
import org.eclipse.ltk.core.refactoring.resource.CopyResourceChange;
import org.eclipse.ltk.core.refactoring.resource.CopyResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveRenameResourceDescriptor;
import org.eclipse.ltk.core.refactoring.resource.MoveResourceChange;
import org.eclipse.ltk.core.refactoring.resource.MoveResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;

public class ResourceRefactoringTests {
	private SimpleTestProject fProject;

	@BeforeEach
	public void setUp() throws Exception {
		fProject= new SimpleTestProject();
	}

	@AfterEach
	public void tearDown() throws Exception {
		InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES).remove(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH);
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
	void testCopyChangeFile() throws Exception {
		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);
		IFolder destination= fProject.createFolder("dest");
		ReorgExecutionLog log = new ReorgExecutionLog();

		Change undoChange= perform(new CopyResourceChange(file, log, destination));

		IResource copiedResource= assertCopy(file, log, destination);

		perform(undoChange);

		assertFalse(copiedResource.exists());
	}

	@Test
	void testCopyChangeFolder() throws Exception {
		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);
		IFolder destination= fProject.createFolder("dest");
		ReorgExecutionLog log = new ReorgExecutionLog();

		Change undoChange= perform(new CopyResourceChange(testFolder, log, destination));

		IFolder copiedResource= (IFolder) assertCopy(testFolder, log, destination);
		assertTrue(copiedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertFalse(copiedResource.exists());
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	@Test
	void testCopyChangeOverwrite() throws Exception {
		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		ReorgExecutionLog log = new ReorgExecutionLog();

		Change undoChange= perform(new CopyResourceChange(file1, log, destination));

		assertCopy(file1, log, destination);

		perform(undoChange);

		assertEquals(content2, fProject.getContent(file2));
	}

	@Test
	void testCopyRefactoringFile() throws Exception {
		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();

		descriptor.setResources(new IResource[] { file });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("myFile.txt") });

		Change undoChange= perform(descriptor);

		IResource copiedResource= assertCopy(file, log, destination);

		perform(undoChange);

		assertFalse(copiedResource.exists());
	}

	@Test
	void testCopyRefactoringFolder() throws Exception {
		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);
		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();

		descriptor.setResources(new IResource[] { testFolder });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("test") });

		Change undoChange= perform(descriptor);

		IFolder copiedResource= (IFolder) assertCopy(testFolder, log, destination);
		assertTrue(copiedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertFalse(copiedResource.exists());
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	@Test
	void testCopyRefactoringOverwrite() throws Exception {
		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();

		descriptor.setResources(new IResource[] { file1 });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("myFile.txt") });

		Change undoChange= perform(descriptor);

		assertCopy(file1, log, destination);

		perform(undoChange);

		assertEquals(content2, fProject.getContent(file2));
	}

	@Test
	void testCopyRenameRefactoringFile() throws Exception {
		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", content);

		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();
		log.setNewName(file, "newNameFile.txt");

		descriptor.setResources(new IResource[] { file });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("newNameFile.txt") });

		Change undoChange= perform(descriptor);

		IResource copiedResource= assertCopy(file, log, destination);

		perform(undoChange);

		assertFalse(copiedResource.exists());
	}

	@Test
	void testCopyRenameRefactoringFolder() throws Exception {
		String content= "hello";

		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", content);
		IFolder destination= fProject.createFolder("dest");

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();
		log.setNewName(testFolder, "newNameFolder");

		descriptor.setResources(new IResource[] { testFolder });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("newNameFolder") });

		Change undoChange= perform(descriptor);

		IFolder copiedResource= (IFolder) assertCopy(testFolder, log, destination);
		assertTrue(copiedResource.getFile("myFile.txt").exists());

		perform(undoChange);

		assertFalse(copiedResource.exists());
		assertTrue(testFolder.getFile("myFile.txt").exists());
	}

	@Test
	void testCopyRenameRefactoringOverwrite() throws Exception {
		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile2.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();
		log.setNewName(file1, "myFile2.txt");

		descriptor.setResources(new IResource[] { file1 });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("myFile2.txt") });

		Change undoChange= perform(descriptor);

		assertCopy(file1, log, destination);

		perform(undoChange);

		assertEquals(content2, fProject.getContent(file2));
	}

	@Test
	void testCopyRefactoringMergeFolder() throws Exception {
		// merge 2 files (1 new copy, 1 existing)
		String content1= "hello";
		String content2= "world";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile2.txt", content2);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();

		descriptor.setResources(new IResource[] { file1 });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("myFile.txt") });

		Change undoChange= perform(descriptor);

		IResource copiedResource= assertCopy(file1, log, destination);
		assertTrue(destination.getFile("myFile2.txt").exists());
		assertEquals(content2, fProject.getContent(file2));

		perform(undoChange);

		assertTrue(destination.getFile("myFile2.txt").exists());
		assertFalse(copiedResource.exists());
	}

	@Test
	void testCopyRefactoringOverwriteAndMergeFolder() throws Exception {
		// merge 2 files (1 new copy that overwrites, 1 existing)
		String content1= "hello";
		String content2= "world";
		String content3= "!";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1); // copy

		IFolder destination= fProject.createFolder("dest");
		IFile file2= fProject.createFile(destination, "myFile2.txt", content2); // keep
		fProject.createFile(destination, "myFile.txt", content3); // overwrite

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyResourcesDescriptor.ID);
		CopyResourcesDescriptor descriptor= (CopyResourcesDescriptor) contribution.createDescriptor();
		ReorgExecutionLog log = new ReorgExecutionLog();

		descriptor.setResources(new IResource[] { file1 });
		descriptor.setDestinationPaths(new IPath[] { destination.getFullPath().append("myFile.txt") });

		Change undoChange= perform(descriptor);

		assertCopy(file1, log, destination);
		assertTrue(destination.getFile("myFile2.txt").exists());
		assertEquals(content2, fProject.getContent(file2));

		perform(undoChange);

		assertTrue(destination.getFile("myFile2.txt").exists());
		assertEquals(content3, fProject.getContent(destination.getFile("myFile.txt")));
	}

	@Test
	public void testDeleteRefactoring1_bug343584() throws Exception {
		IFolder testFolder= fProject.createFolder("test");
		fProject.createFile(testFolder, "myFile.txt", "hello");

		IProject testProject2= ResourcesPlugin.getWorkspace().getRoot().getProject(fProject.getProject().getName() + "2");
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

	@Test
	public void testDeleteRefactoringOutOfSync_noAutoRefresh() throws Exception {
		setLightweightAutoRefresh(false);
		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", "hello");
		modifyExternally(file);

		RefactoringContext context= createDeleteRefactoringContext(file);
		try {
			RefactoringStatus status= context.getRefactoring().checkAllConditions(new NullProgressMonitor());
			assertTrue(status.hasWarning(), "expected an out-of-sync warning");
		} finally {
			context.dispose();
		}
	}

	@Test
	public void testDeleteRefactoringOutOfSync_autoRefresh() throws Exception {
		setLightweightAutoRefresh(true);
		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", "hello");
		File localFile= modifyExternally(file);

		RefactoringContext context= createDeleteRefactoringContext(file);
		try {
			Refactoring refactoring= context.getRefactoring();
			RefactoringStatus status= refactoring.checkAllConditions(new NullProgressMonitor());
			assertTrue(status.isOK(), () -> "expected no warning but was: " + status);

			perform(refactoring.createChange(new NullProgressMonitor()));

			assertFalse(file.exists());
			assertFalse(localFile.exists());
		} finally {
			context.dispose();
		}
	}

	@Test
	public void testDeleteRefactoringDeletedExternally_autoRefresh() throws Exception {
		setLightweightAutoRefresh(true);
		IFolder testFolder= fProject.createFolder("test");
		IFile file= fProject.createFile(testFolder, "myFile.txt", "hello");
		Files.delete(file.getLocation().toFile().toPath());

		RefactoringContext context= createDeleteRefactoringContext(file);
		try {
			Refactoring refactoring= context.getRefactoring();
			RefactoringStatus status= refactoring.checkAllConditions(new NullProgressMonitor());
			assertTrue(status.isOK(), () -> "expected no warning but was: " + status);

			perform(refactoring.createChange(new NullProgressMonitor()));

			assertFalse(file.exists());
		} finally {
			context.dispose();
		}
	}

	@Test
	public void testCopyProjectRefactoring() throws Exception {
		String content1= "hello";

		IFolder testFolder= fProject.createFolder("test");
		IFile file1= fProject.createFile(testFolder, "myFile.txt", content1);

		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(CopyProjectDescriptor.ID);
		CopyProjectDescriptor descriptor= (CopyProjectDescriptor) contribution.createDescriptor();

		descriptor.setResourcePath(fProject.getProject().getFullPath());
		descriptor.setNewName("project2");
		descriptor.setNewLocation(fProject.getProject().getParent().getFullPath());

		Change undoChange= perform(descriptor);

		IProject targetProject= ResourcesPlugin.getWorkspace().getRoot().getProject("project2");

		assertTrue(targetProject.exists());

		assertMoveRename(file1, targetProject.getFolder("test"), "myFile.txt", content1);

		perform(undoChange);

		assertFalse(targetProject.exists());
	}

	private void setLightweightAutoRefresh(boolean enabled) {
		InstanceScope.INSTANCE.getNode(ResourcesPlugin.PI_RESOURCES).putBoolean(ResourcesPlugin.PREF_LIGHTWEIGHT_AUTO_REFRESH, enabled);
	}

	/**
	 * Modifies the file in the local file system without notifying the workspace, so the file is
	 * out of sync afterwards.
	 */
	private File modifyExternally(IFile file) throws IOException {
		File localFile= file.getLocation().toFile();
		Files.writeString(localFile.toPath(), "external change");
		assertTrue(localFile.setLastModified(localFile.lastModified() + 5000));
		assertFalse(file.isSynchronized(IResource.DEPTH_ZERO));
		return localFile;
	}

	private RefactoringContext createDeleteRefactoringContext(IResource... resources) throws CoreException {
		RefactoringContribution contribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor descriptor= (DeleteResourcesDescriptor) contribution.createDescriptor();
		descriptor.setResources(resources);
		RefactoringStatus status= new RefactoringStatus();
		RefactoringContext context= descriptor.createRefactoringContext(status);
		assertTrue(status.isOK());
		return context;
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

	private IResource assertCopy(IResource source, ReorgExecutionLog log, IContainer destination) throws CoreException, IOException {
		String newName = log.getNewName(source);
		if (newName == null) {
			newName = source.getName();
		}

		IResource res= destination.findMember(newName);

		assertTrue(source.exists());
		assertNotNull(res);
		assertEquals(res.getType(), source.getType());

		if (source instanceof IFile file) {
			assertEquals(fProject.getContent(file), fProject.getContent((IFile) res));
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