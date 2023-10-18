/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourcesDescriptor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceDescriptor;
import org.eclipse.ltk.core.refactoring.tests.FileSystemHelper;
import org.eclipse.ltk.core.refactoring.tests.RefactoringCoreTestPlugin;
import org.eclipse.ltk.core.refactoring.tests.participants.ElementRenameProcessor;
import org.eclipse.ltk.core.refactoring.tests.participants.ElementRenameRefactoring;
import org.eclipse.ltk.core.refactoring.tests.util.SimpleTestProject;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;

public class ResourceRefactoringUndoTests {
	private static final String TEST_NEWPROJECT_NAME= "projectTestNew";
	private static final String TEST_FOLDER_NAME= "test";
	private static final String TEST_NEWFOLDER_NAME= "testNew";
	private static final String TEST_FILE_NAME= "myFile.txt";
	private static final String TEST_NEWFILE_NAME= "newFile.txt";
	private static final String TEST_LINKEDFOLDER_NAME= "linkedFolder";
	private static final String TEST_LINKEDFILE_NAME= "linkedFile.txt";
	private static final String TEST_SUBFOLDER_NAME= "subFolder";
	private static List<String> fileNameExcludes= new ArrayList<>();

	static {
		fileNameExcludes.add(".project");
	}

	private static final String CONTENT= "hello";
	private SimpleTestProject fProject;
	private IFolder testFolder;
	private IFile testFile;
	private IOperationHistory history;
	private IUndoContext context;
	private final Set<IFileStore> storesToDelete= new HashSet<>();
	private IFolder testLinkedFolder;
	private IFile testLinkedFile;
	private IFolder testSubFolder;

	@Before
	public void setUp() throws Exception {
		fProject= new SimpleTestProject();

		testFolder= fProject.createFolder(TEST_FOLDER_NAME);
		testFile= fProject.createFile(testFolder, TEST_FILE_NAME, CONTENT);
		// Create links by first creating the backing content...
		IFileStore folderStore= getTempStore();
		IFileStore fileStore= getTempStore();
		IPath folderLocation= URIUtil.toPath(folderStore.toURI());
		IPath fileLocation= URIUtil.toPath(fileStore.toURI());
		folderStore.mkdir(EFS.NONE, getMonitor());
		fileStore.openOutputStream(EFS.NONE, getMonitor()).close();
		// Then create the workspace objects
		testLinkedFolder= testFolder.getFolder(TEST_LINKEDFOLDER_NAME);
		testLinkedFolder.createLink(folderLocation, IResource.NONE, getMonitor());
		assertTrue(testLinkedFolder.exists());
		testLinkedFile= testFolder.getFile(TEST_LINKEDFILE_NAME);
		testLinkedFile.createLink(fileLocation, IResource.NONE, getMonitor());

		// subfolder
		testSubFolder= testFolder.getFolder(TEST_SUBFOLDER_NAME);
		testSubFolder.create(true, true, getMonitor());

		history= OperationHistoryFactory.getOperationHistory();
		context= RefactoringCorePlugin.getUndoContext();
	}

	@After
	public void tearDown() throws Exception {
		fProject.delete();
		final IFileStore[] toDelete= storesToDelete.toArray(new IFileStore[storesToDelete.size()]);
		storesToDelete.clear();
		for (IFileStore d : toDelete) {
			clear(d);
		}
	}


	@Test
	public void testFileRenameUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(RenameResourceDescriptor.ID);
		RenameResourceDescriptor desc= (RenameResourceDescriptor) renameContribution.createDescriptor();
		desc.setResourcePath(testFile.getFullPath());
		desc.setNewName(TEST_NEWFILE_NAME);
		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		FileSnapshot snap= new FileSnapshot(testFile);
		execute(op);

		IFile renamedFile= testFolder.getFile(TEST_NEWFILE_NAME);
		assertTrue("File rename failed", renamedFile.exists());
		snap.name= TEST_NEWFILE_NAME;
		assertTrue("File CONTENT was altered on rename", snap.isValid(testFolder));

		undo();
		snap.name= TEST_FILE_NAME;
		assertTrue("File CONTENT was altered on undo rename", snap.isValid(testFolder));
		assertFalse("Undo rename failed", renamedFile.exists());

		redo();
		snap.name= TEST_NEWFILE_NAME;
		assertTrue("File CONTENT was altered on redo rename", snap.isValid(testFolder));
	}

	@Test
	public void testFolderRenameUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(RenameResourceDescriptor.ID);
		RenameResourceDescriptor desc= (RenameResourceDescriptor) renameContribution.createDescriptor();
		desc.setResourcePath(testFolder.getFullPath());
		desc.setNewName(TEST_NEWFOLDER_NAME);
		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		FolderSnapshot snap= new FolderSnapshot(testFolder);
		execute(op);
		IFolder renamedFolder= fProject.getProject().getFolder(TEST_NEWFOLDER_NAME);
		assertTrue("Project rename failed", renamedFolder.exists());
		snap.name= TEST_NEWFOLDER_NAME;
		assertTrue("Folder CONTENT was altered on rename", snap.isValid(fProject.getProject()));

		undo();
		snap.name= TEST_FOLDER_NAME;
		assertTrue("Folder CONTENT was altered on undo rename", snap.isValid(fProject.getProject()));
		assertFalse("Undo rename failed", renamedFolder.exists());

		redo();
		snap.name= TEST_NEWFOLDER_NAME;
		assertTrue("Folder CONTENT was altered on redo rename", snap.isValid(fProject.getProject()));
	}

	@Test
	public void testProjectRenameUndoRedoLTK() throws ExecutionException, CoreException {
		IProject renamedProject= null;
		try {
			RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(RenameResourceDescriptor.ID);
			RenameResourceDescriptor desc= (RenameResourceDescriptor) renameContribution.createDescriptor();
			desc.setResourcePath(fProject.getProject().getFullPath());
			desc.setNewName(TEST_NEWPROJECT_NAME);
			PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

			ProjectSnapshot snap= new ProjectSnapshot(fProject.getProject());
			execute(op);
			renamedProject= getWorkspaceRoot().getProject(TEST_NEWPROJECT_NAME);
			assertTrue("Project rename failed", renamedProject.exists());
			snap.name= TEST_NEWPROJECT_NAME;
			assertTrue("Project CONTENT was altered on rename", snap.isValid());
			undo();
			snap.name= SimpleTestProject.TEST_PROJECT_NAME;
			assertTrue("Project CONTENT was altered on undo rename", snap.isValid());
			assertFalse("Undo rename failed", renamedProject.exists());
			redo();
			snap.name= TEST_NEWPROJECT_NAME;
			assertTrue("Project CONTENT was altered on redo rename", snap.isValid());
		} finally {
			renamedProject.delete(true, true, null);
		}
	}

	@Test
	public void testFileDeleteUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) renameContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { testFile.getFullPath() });

		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		FileSnapshot snap= new FileSnapshot(testFile);

		execute(op);

		assertFalse("File delete failed", testFile.exists());
		undo();
		assertTrue("File recreation failed", testFile.exists());
		assertTrue("File CONTENT was altered on undo", snap.isValid(testFile.getParent()));
		redo();
		assertFalse("Redo delete failed", testFile.exists());
	}

	@Test
	public void testFileLinkedDeleteUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) renameContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { testLinkedFile.getFullPath() });

		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		FileSnapshot snap= new FileSnapshot(testLinkedFile);

		execute(op);

		assertFalse("File delete failed", testLinkedFile.exists());
		undo();
		assertTrue("File recreation failed", testLinkedFile.exists());
		assertTrue("File CONTENT was altered on undo", snap.isValid(testLinkedFile.getParent()));
		redo();
		assertFalse("Redo delete failed", testLinkedFile.exists());
	}

	@Test
	public void testFolderDeleteUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) renameContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { testSubFolder.getFullPath() });

		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		FolderSnapshot snap= new FolderSnapshot(testSubFolder);

		execute(op);

		assertFalse("Folder delete failed", testSubFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testSubFolder.exists());
		assertTrue("Folder CONTENT was altered on undo", snap.isValid(testSubFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testSubFolder.exists());
	}

	@Test
	public void testFolderDeleteLinkedUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) renameContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { testLinkedFolder.getFullPath() });

		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		FolderSnapshot snap= new FolderSnapshot(testLinkedFolder);
		execute(op);
		assertFalse("Folder delete failed", testLinkedFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testLinkedFolder.exists());
		assertTrue("Folder CONTENT was altered on undo", snap.isValid(testLinkedFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testLinkedFolder.exists());
	}

	@Test
	public void testFolderDeleteLinkedDeletedOnFilesystemUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution deleteContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) deleteContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { testLinkedFolder.getFullPath() });

		PerformRefactoringOperation op= new PerformRefactoringOperation(
				desc.createRefactoringContext(new RefactoringStatus()),
				CheckConditionsOperation.ALL_CONDITIONS);

		FolderSnapshot snap= new FolderSnapshot(testLinkedFolder);

		// Create a subfolder containing a file under the linked folder.
		IFolder subfolder= testLinkedFolder.getFolder("A");
		subfolder.create(true, true, getMonitor());
		IFile file= subfolder.getFile("test.txt");
		file.create(new ByteArrayInputStream("test contents".getBytes(StandardCharsets.UTF_8)), true, getMonitor());
		// Delete the target of the linked folder on the file system making the linked folder out of sync
		// with the file system.
		IFileStore folderStore= EFS.getStore(testLinkedFolder.getLocationURI());
		folderStore.delete(EFS.NONE, getMonitor());  // Delete the target folder on the file system.

		execute(op);
		assertFalse("Folder delete failed", testLinkedFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testLinkedFolder.exists());
		assertTrue("Folder CONTENT was altered on undo", snap.isValid(testLinkedFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testLinkedFolder.exists());
	}

	@Test
	public void testProjectDeleteUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) renameContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { fProject.getProject().getFullPath() });

		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

		execute(op);
		assertFalse("Project delete failed", fProject.getProject().exists());
		undo();
		assertTrue("Project recreation failed", fProject.getProject().exists());
// Ideally we could run this test everytime, but it fails intermittently
// because opening the recreated project occurs in the background, and
// the creation of the workspace representation for the disk contents
// may not have happened yet. This test always passes under debug where
// timing can be controlled.
// ***********
// assertTrue("Project CONTENT was altered on undo", snap.isValid());
// ************
		redo();
		assertFalse("Redo delete failed", fProject.getProject().exists());
// We undo again so that the project will exist during teardown and
// get cleaned up. Otherwise some CONTENT is left on disk.
		undo();
	}

	@Test
	public void testProjectClosedDeleteUndoRedoLTK() throws ExecutionException, CoreException {
		fProject.getProject().close(getMonitor());
		testProjectDeleteUndoRedoLTK();
	}

	@Test
	public void testProjectDeleteWithContentUndoRedoLTK() throws ExecutionException, CoreException {
		RefactoringContribution renameContribution= RefactoringCore.getRefactoringContribution(DeleteResourcesDescriptor.ID);
		DeleteResourcesDescriptor desc= (DeleteResourcesDescriptor) renameContribution.createDescriptor();
		desc.setResourcePaths(new IPath[] { fProject.getProject().getFullPath() });
		desc.setDeleteContents(true);

		PerformRefactoringOperation op= new PerformRefactoringOperation(desc.createRefactoringContext(new RefactoringStatus()), CheckConditionsOperation.ALL_CONDITIONS);

// we don't snapshot since CONTENT will be deleted
		execute(op);
		assertFalse("Project delete failed", fProject.getProject().exists());
		undo();
		assertTrue("Project was recreated", fProject.getProject().exists());
		redo();
		assertFalse("Redo delete failed", fProject.getProject().exists());
	}

	@Test
	public void testProjectClosedDeleteWithContentUndoRedoLTK() throws ExecutionException, CoreException {
		fProject.getProject().close(getMonitor());
		testProjectDeleteWithContentUndoRedoLTK();
	}

	@Test
	public void testPreChangeUndoRedoLTK() throws ExecutionException, CoreException {
		Refactoring ref= new ElementRenameRefactoring(ElementRenameRefactoring.WORKING | ElementRenameRefactoring.ALWAYS_ENABLED | ElementRenameRefactoring.PRE_CHANGE);

		PerformRefactoringOperation op= new PerformRefactoringOperation(ref, CheckConditionsOperation.ALL_CONDITIONS);

		execute(op);

		List<String> h= ElementRenameProcessor.fHistory;
		// Strictly speaking, the execution of these things does not need to be in exactly this
		// order, but it's an easy way to check.  Of course there are some dependencies on the
		// order (participant-pre-exec, main-exec, participant-exec)
		int i= 0;
		assertEquals(ElementRenameProcessor.MAIN_CREATE, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKING_CREATE, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKINGPRE_CREATEPRE, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKINGPRE_CREATE, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKINGPRE_EXECPRE, h.get(i++));
		assertEquals(ElementRenameProcessor.MAIN_EXEC, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKING_EXEC, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKINGPRE_EXEC, h.get(i++));

		undo();
		assertEquals(ElementRenameProcessor.WORKINGPRE_EXEC_UNDO, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKING_EXEC_UNDO, h.get(i++));
		assertEquals(ElementRenameProcessor.MAIN_EXEC_UNDO, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKINGPRE_EXECPRE_UNDO, h.get(i++));

		redo();
		assertEquals(ElementRenameProcessor.WORKINGPRE_EXECPRE, h.get(i++));
		assertEquals(ElementRenameProcessor.MAIN_EXEC, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKING_EXEC, h.get(i++));
		assertEquals(ElementRenameProcessor.WORKINGPRE_EXEC, h.get(i++));
	}

	private void execute(PerformRefactoringOperation op) throws CoreException {
		ResourcesPlugin.getWorkspace().run(op, getMonitor());
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private void undo() throws ExecutionException {
		assertTrue("Operation can be undone", history.canUndo(context));
		IStatus status= history.undo(context, getMonitor(), null);
		assertTrue("Undo should be OK status", status.isOK());
	}

	private void redo() throws ExecutionException {
		assertTrue("Operation can be redone", history.canRedo(context));
		IStatus status= history.redo(context, getMonitor(), null);
		assertTrue("Redo should be OK status", status.isOK());
	}

	private IProgressMonitor getMonitor() {
		return null;
	}

	private ResourceSnapshot snapshotFromResource(IResource resource) throws CoreException {
		if (resource instanceof IFile)
			return new FileSnapshot((IFile) resource);
		if (resource instanceof IFolder)
			return new FolderSnapshot((IFolder) resource);
		if (resource instanceof IProject)
			return new ProjectSnapshot((IProject) resource);
		fail("Unknown resource type");
// making compiler happy
		return new FileSnapshot((IFile) resource);
	}

	/*
	 * reads testFile CONTENT and returns string
	 */
	private String readContent(IFile file) throws CoreException {
		InputStream is= file.getContents();
		String encoding= file.getCharset();
		if (is == null)
			return null;
		try (BufferedReader reader= new BufferedReader(new InputStreamReader(is, encoding))) {
			StringBuilder buffer= new StringBuilder();
			char[] part= new char[2048];
			int read= 0;

			while ((read= reader.read(part)) != -1)
				buffer.append(part, 0, read);

			return buffer.toString();

		} catch (IOException ex) {
			throw new CoreException(new Status(IStatus.ERROR, RefactoringCoreTestPlugin.getPluginId(), ex.getMessage()));
		}
	}

	/**
	 * Returns a FileStore instance backed by storage in a temporary location. The returned store
	 * will not exist, but will belong to an existing parent. The tearDown method in this class will
	 * ensure the location is deleted after the test is completed.
	 *
	 * @return The temp filestore to use
	 */
	private IFileStore getTempStore() {
		IFileStore store= EFS.getLocalFileSystem().getStore(FileSystemHelper.getRandomLocation(FileSystemHelper.getTempDir()));
		storesToDelete.add(store);
		return store;
	}

	private void clear(IFileStore store) {
		try {
			store.delete(EFS.NONE, null);
		} catch (CoreException e) {
		}
	}

	class FileSnapshot extends ResourceSnapshot {
		String content;

		URI location;

		MarkerSnapshot[] markerSnapshots;

		FileSnapshot(IFile file) throws CoreException {
			content= readContent(file);
			name= file.getName();
			if (file.isLinked()) {
				location= file.getLocationURI();
			}
			IMarker[] markers= file.findMarkers(null, true, IResource.DEPTH_INFINITE);
			markerSnapshots= new MarkerSnapshot[markers.length];
			for (int i= 0; i < markers.length; i++) {
				markerSnapshots[i]= new MarkerSnapshot(markers[i]);
			}
		}

		@Override
		boolean isValid(IResource parent) throws CoreException {
			IResource resource= getWorkspaceRoot().findMember(parent.getFullPath().append(name));
			if (resource == null || !(resource instanceof IFile)) {
				return false;
			}
			IFile file= (IFile) resource;
			boolean contentMatch= readContent(file).equals(content);
			if (file.isLinked()) {
				contentMatch= contentMatch && file.getLocationURI().equals(location);
			}
			if (!contentMatch) {
				return false;
			}
			for (MarkerSnapshot markerSnapshot : markerSnapshots) {
				if (!markerSnapshot.existsOn(resource)) {
					return false;
				}
			}
			return true;
		}
	}

	class FolderSnapshot extends ResourceSnapshot {
		URI location;

		ResourceSnapshot[] memberSnapshots;

		FolderSnapshot(IFolder folder) throws CoreException {
			name= folder.getName();
			if (folder.isLinked()) {
				location= folder.getLocationURI();
			}
			IResource[] members= folder.members();
			memberSnapshots= new ResourceSnapshot[members.length];
			for (int i= 0; i < members.length; i++) {
				memberSnapshots[i]= snapshotFromResource(members[i]);
			}
		}

		@Override
		boolean isValid(IResource parent) throws CoreException {
			IResource resource= getWorkspaceRoot().findMember(parent.getFullPath().append(name));
			if (resource == null || !(resource instanceof IFolder)) {
				return false;
			}
			IFolder folder= (IFolder) resource;
			if (folder.isLinked()) {
				if (!folder.getLocationURI().equals(location)) {
					return false;
				}
			}
			for (ResourceSnapshot memberSnapshot : memberSnapshots) {
				if (!fileNameExcludes.contains(memberSnapshot.name)) {
					if (!memberSnapshot.isValid(folder)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	static class MarkerSnapshot {
		String type;

		Map<String, Object> attributes;

		MarkerSnapshot(IMarker marker) throws CoreException {
			type= marker.getType();
			attributes= marker.getAttributes();
		}

		boolean existsOn(IResource resource) throws CoreException {
			// comparison is based on equality of attributes, since id will
			// change on create/delete/recreate sequence
			for (IMarker marker : resource.findMarkers(type, false, IResource.DEPTH_ZERO)) {
				if (marker.getAttributes().equals(attributes)) {
					return true;
				}
			}
			return false;
		}
	}

	class ProjectSnapshot extends ResourceSnapshot {
		ResourceSnapshot[] memberSnapshots;

		ProjectSnapshot(IProject project) throws CoreException {
			name= project.getName();
			boolean open= project.isOpen();
			if (!open) {
				project.open(null);
			}
			IResource[] members= project.members();
			memberSnapshots= new ResourceSnapshot[members.length];
			for (int i= 0; i < members.length; i++) {
				memberSnapshots[i]= snapshotFromResource(members[i]);
			}
			if (!open) {
				project.close(null);
			}

		}

		@Override
		boolean isValid(IResource parent) throws CoreException {
			IResource resource= getWorkspaceRoot().findMember(parent.getFullPath().append(name));
			if (resource == null || !(resource instanceof IProject)) {
				return false;
			}
			IProject project= (IProject) resource;
			// Must open it to validate the CONTENT
			boolean open= project.isOpen();
			if (!open) {
				project.open(null);
			}

			for (ResourceSnapshot memberSnapshot : memberSnapshots) {
				if (!fileNameExcludes.contains(memberSnapshot.name)) {
					if (!memberSnapshot.isValid(resource)) {
						return false;
					}
				}
			}

			if (!open) {
				project.close(null);
			}

			return true;
		}

		boolean isValid() throws CoreException {
			return isValid(getWorkspaceRoot());
		}
	}

	abstract static class ResourceSnapshot {
		String name;

		abstract boolean isValid(IResource parent) throws CoreException;

		IWorkspaceRoot getWorkspaceRoot() {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}
}
