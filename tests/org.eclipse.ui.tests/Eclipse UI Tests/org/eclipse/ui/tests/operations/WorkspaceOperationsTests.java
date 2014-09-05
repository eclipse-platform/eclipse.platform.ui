/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.harness.FileSystemHelper;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.AbstractWorkspaceOperation;
import org.eclipse.ui.ide.undo.CopyProjectOperation;
import org.eclipse.ui.ide.undo.CopyResourcesOperation;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.CreateFolderOperation;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;
import org.eclipse.ui.ide.undo.DeleteResourcesOperation;
import org.eclipse.ui.ide.undo.MoveProjectOperation;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;
import org.eclipse.ui.internal.operations.AdvancedValidationUserApprover;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests the undo of various workspace operations. Uses the following workspace
 * structure to perform the tests
 * 
 * <pre>
 *  TEST_PROJECT_NAME
 *  **TEST_FOLDER_NAME
 *  ****TEST_SUBFOLDER_NAME
 *  ******TEST_FILEINSUBFOLDER_NAME 
 *  ****TEST_EMPTYFILE_NAME
 *  ****TEST_RANDOMFILE_NAME
 *  ****TEST_LINKEDFILE_NAME (linked to random location)
 *  ****TEST_LINKEDFOLDER_NAME (linked to random location)
 *  **TEST_FILEINPROJECT_NAME 
 *  TEST_TARGETPROJECT_NAME
 * </pre>
 * 
 * @since 3.3
 */
public class WorkspaceOperationsTests extends UITestCase {

	IProject testProject, targetProject;

	IFolder testFolder, testSubFolder, testLinkedFolder;

	IFile emptyTestFile, testFileWithContent, testLinkedFile,
			testFileInSubFolder, testFileInProject;

	private final Set storesToDelete = new HashSet();

	IOperationHistory history;

	IUndoContext context;

	private static Map initialAttributes = new HashMap();
	static {
		initialAttributes.put("Attr1", "Attr1 1.0");
		initialAttributes.put("Attr2", "Attr2 1.0");
		initialAttributes.put("Attr3", "Attr3 1.0");
		initialAttributes.put("Attr4", "Attr4 1.0");
		initialAttributes.put("Attr5", "Attr5 1.0");
		initialAttributes.put("Attr6", "Attr6 1.0");
	};

	private static Map updatedAttributes = new HashMap();
	static {
		updatedAttributes.put("Attr1", "Attr1 1.1");
		updatedAttributes.put("Attr2", "Attr2 1.1");
		updatedAttributes.put("Attr3", "Attr3 1.1");
		updatedAttributes.put("Attr4", "Attr4 1.1");
		updatedAttributes.put("Attr5", "Attr5 1.1");
		updatedAttributes.put("Attr7", "Attr7 1.0");
	};

	private static Map mergedUpdatedAttributes = new HashMap();
	static {
		mergedUpdatedAttributes.put("Attr1", "Attr1 1.1");
		mergedUpdatedAttributes.put("Attr2", "Attr2 1.1");
		mergedUpdatedAttributes.put("Attr3", "Attr3 1.1");
		mergedUpdatedAttributes.put("Attr4", "Attr4 1.1");
		mergedUpdatedAttributes.put("Attr5", "Attr5 1.1");
		mergedUpdatedAttributes.put("Attr6", "Attr6 1.0");
		mergedUpdatedAttributes.put("Attr7", "Attr7 1.0");
	};

	private static List fileNameExcludes = new ArrayList();
	static {
		fileNameExcludes.add(".project");
	};

	private static String CUSTOM_TYPE = "TestMarkerType";

	private static String FILE_CONTENTS_EMPTY = "";

	private static String TEST_PROJECT_NAME = "WorkspaceOperationsTests_Project";

	private static String TEST_TARGET_PROJECT_NAME = "WorkspaceOperationsTests_MoveCopyTarget";

	private static String TEST_FOLDER_NAME = "WorkspaceOperationsTests_Folder";

	private static String TEST_SUBFOLDER_NAME = "WorkspaceOperationsTests_SubFolder";

	private static String TEST_LINKEDFOLDER_NAME = "WorkspaceOperationsTests_LinkedFolder";

	private static String TEST_LINKEDFILE_NAME = "WorkspaceOperationTests_LinkedFile";

	private static String TEST_EMPTYFILE_NAME = "WorkspaceOperationsTests_EmptyFile";

	private static String TEST_RANDOMFILE_NAME = "WorkspaceOperationsTests_RandomContentFile.txt";

	private static String TEST_FILEINPROJECT_NAME = "WorkspaceOperationsTests_FileInProject";

	private static String TEST_FILEINSUBFOLDER_NAME = "WorkspaceOperationsTests_FileInSubFolder";

	private static String TEST_NEWPROJECT_NAME = "WorkspaceOperationTests_NewProject";

	private static String TEST_NEWFOLDER_NAME = "WorkspaceOperationTests_NewFolder";

	private static String TEST_NEWFILE_NAME = "WorkspaceOperationTests_NewFile";
	
	private static String TEST_NESTEDFOLDER_ROOT_PARENT_NAME = "scooby";
	
	private static String TEST_NESTEDFOLDER_PARENT_NAME = "scooby/dooby/doo";

	private static String TEST_NEWNESTEDFOLDER_NAME = "scooby/dooby/doo/WorkspaceOperationTests_NewFolder";

	private static String TEST_NEWNESTEDFILE_NAME = "scooby/dooby/doo/WorkspaceOperationTests_NewFile";

	// Insider knowledge of WorkspaceUndoMonitor's change threshhold
	private static int NUM_CHANGES = 10;

	class FileSnapshot extends ResourceSnapshot {
		String content;

		URI location;

		MarkerSnapshot[] markerSnapshots;

		FileSnapshot(IFile file) throws CoreException {
			content = readContent(file);
			name = file.getName();
			if (file.isLinked()) {
				location = file.getLocationURI();
			}
			IMarker[] markers = file.findMarkers(null, true,
					IResource.DEPTH_INFINITE);
			markerSnapshots = new MarkerSnapshot[markers.length];
			for (int i = 0; i < markers.length; i++) {
				markerSnapshots[i] = new MarkerSnapshot(markers[i]);
			}
		}

		@Override
		boolean isValid(IResource parent) throws CoreException {
			IResource resource = getWorkspaceRoot().findMember(
					parent.getFullPath().append(name));
			if (resource == null || !(resource instanceof IFile)) {
				return false;
			}
			IFile file = (IFile) resource;
			boolean contentMatch = readContent(file).equals(content);
			if (file.isLinked()) {
				contentMatch = contentMatch
						&& file.getLocationURI().equals(location);
			}
			if (!contentMatch) {
				return false;
			}
			for (int i = 0; i < markerSnapshots.length; i++) {
				if (!markerSnapshots[i].existsOn(resource)) {
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
			name = folder.getName();
			if (folder.isLinked()) {
				location = folder.getLocationURI();
			}
			IResource[] members = folder.members();
			memberSnapshots = new ResourceSnapshot[members.length];
			for (int i = 0; i < members.length; i++) {
				memberSnapshots[i] = snapshotFromResource(members[i]);
			}
		}

		@Override
		boolean isValid(IResource parent) throws CoreException {
			IResource resource = getWorkspaceRoot().findMember(
					parent.getFullPath().append(name));
			if (resource == null || !(resource instanceof IFolder)) {
				return false;
			}
			IFolder folder = (IFolder) resource;
			if (folder.isLinked()) {
				if (!folder.getLocationURI().equals(location)) {
					return false;
				}
			}
			for (int i = 0; i < memberSnapshots.length; i++) {
				if (!fileNameExcludes.contains(memberSnapshots[i].name)) {
					if (!memberSnapshots[i].isValid(folder)) {
						return false;
					}
				}
			}
			return true;
		}
	}

	class MarkerSnapshot {
		String type;

		Map attributes;

		MarkerSnapshot(IMarker marker) throws CoreException {
			type = marker.getType();
			attributes = marker.getAttributes();
		}

		boolean existsOn(IResource resource) throws CoreException {
			// comparison is based on equality of attributes, since id will
			// change on create/delete/recreate sequence
			IMarker[] markers = resource.findMarkers(type, false,
					IResource.DEPTH_ZERO);
			for (int i = 0; i < markers.length; i++) {
				if (markers[i].getAttributes().equals(attributes)) {
					return true;
				}
			}
			return false;
		}
	}

	class ProjectSnapshot extends ResourceSnapshot {
		ResourceSnapshot[] memberSnapshots;

		ProjectSnapshot(IProject project) throws CoreException {
			name = project.getName();
			boolean open = project.isOpen();
			if (!open) {
				project.open(null);
			}
			IResource[] members = project.members();
			memberSnapshots = new ResourceSnapshot[members.length];
			for (int i = 0; i < members.length; i++) {
				memberSnapshots[i] = snapshotFromResource(members[i]);
			}
			if (!open) {
				project.close(null);
			}

		}

		@Override
		boolean isValid(IResource parent) throws CoreException {
			IResource resource = getWorkspaceRoot().findMember(
					parent.getFullPath().append(name));
			if (resource == null || !(resource instanceof IProject)) {
				return false;
			}
			IProject project = (IProject) resource;
			// Must open it to validate the content
			boolean open = project.isOpen();
			if (!open) {
				project.open(null);
			}

			for (int i = 0; i < memberSnapshots.length; i++) {
				if (!fileNameExcludes.contains(memberSnapshots[i].name)) {
					if (!memberSnapshots[i].isValid(resource)) {
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

	abstract class ResourceSnapshot {
		String name;

		abstract boolean isValid(IResource parent) throws CoreException;

		IWorkspaceRoot getWorkspaceRoot() {
			return ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	/**
	 * @param testName
	 */
	public WorkspaceOperationsTests(String name) {
		super(name);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		// Suppress validation UI
		AdvancedValidationUserApprover.AUTOMATED_MODE = true;
		// Project
		testProject = getWorkspace().getRoot().getProject(TEST_PROJECT_NAME);
		testProject.create(getMonitor());
		testProject.open(getMonitor());
		assertTrue(testProject.exists());
		// Project for move/copy target location
		targetProject = getWorkspace().getRoot().getProject(
				TEST_TARGET_PROJECT_NAME);
		targetProject.create(getMonitor());
		targetProject.open(getMonitor());
		assertTrue(targetProject.exists());
		// Folder in Project
		testFolder = testProject.getFolder(TEST_FOLDER_NAME);
		testFolder.create(true, true, getMonitor());
		assertTrue(testFolder.exists());
		// File in Project
		testFileInProject = testProject.getFile(TEST_FILEINPROJECT_NAME);
		testFileInProject.create(getContents(getRandomString()), true,
				getMonitor());
		assertTrue(testFileInProject.exists());
		// Subfolder in top level folder
		testSubFolder = testFolder.getFolder(TEST_SUBFOLDER_NAME);
		testSubFolder.create(true, true, getMonitor());
		assertTrue(testSubFolder.exists());
		// Files in top level folder
		emptyTestFile = testFolder.getFile(TEST_EMPTYFILE_NAME);
		emptyTestFile.create(getContents(FILE_CONTENTS_EMPTY), true,
				getMonitor());
		testFileWithContent = testFolder.getFile(TEST_RANDOMFILE_NAME);
		testFileWithContent.create(getContents(getRandomString()), true,
				getMonitor());
		// File in subfolder
		testFileInSubFolder = testSubFolder.getFile(TEST_FILEINSUBFOLDER_NAME);
		testFileInSubFolder.create(getContents(getRandomString()), true,
				getMonitor());
		assertTrue(testFileInProject.exists());

		// Create links by first creating the backing content...
		IFileStore folderStore = getTempStore();
		IFileStore fileStore = getTempStore();
		IPath folderLocation = URIUtil.toPath(folderStore.toURI());
		IPath fileLocation = URIUtil.toPath(fileStore.toURI());
		folderStore.mkdir(EFS.NONE, getMonitor());
		fileStore.openOutputStream(EFS.NONE, getMonitor()).close();
		// Then create the workspace objects
		testLinkedFolder = testFolder.getFolder(TEST_LINKEDFOLDER_NAME);
		testLinkedFolder.createLink(folderLocation, IResource.NONE,
				getMonitor());
		assertTrue(testLinkedFolder.exists());
		testLinkedFile = testFolder.getFile(TEST_LINKEDFILE_NAME);
		testLinkedFile.createLink(fileLocation, IResource.NONE, getMonitor());

		history = PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
		context = PlatformUI.getWorkbench().getOperationSupport()
				.getUndoContext();

	}

	@Override
	protected void doTearDown() throws Exception {
		testProject = (IProject) getWorkspaceRoot().findMember(
				TEST_PROJECT_NAME);
		if (testProject != null) {
			testProject.close(getMonitor());
			testProject.delete(true, true, getMonitor());
		}
		targetProject = (IProject) getWorkspaceRoot().findMember(
				TEST_TARGET_PROJECT_NAME);
		if (targetProject != null) {
			targetProject.close(getMonitor());
			targetProject.delete(true, true, getMonitor());
		}
		IProject newProject = (IProject) getWorkspaceRoot().findMember(
				TEST_NEWPROJECT_NAME);
		if (newProject != null) {
			newProject.close(getMonitor());
			newProject.delete(true, true, getMonitor());
		}
		final IFileStore[] toDelete = (IFileStore[]) storesToDelete
				.toArray(new IFileStore[storesToDelete.size()]);
		storesToDelete.clear();
		for (int i = 0; i < toDelete.length; i++) {
			clear(toDelete[i]);
		}
		AdvancedValidationUserApprover.AUTOMATED_MODE = false;

		testProject = null;
		targetProject = null;
		testFolder = null;
		testSubFolder = null;
		testLinkedFolder = null;
		emptyTestFile = null;
		testFileWithContent = null;
		testLinkedFile = null;
		testFileInSubFolder = null;
		testFileInProject = null;

		super.doTearDown();
	}

	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/*
	 * reads file content and returns string
	 */
	private String readContent(IFile file) throws CoreException {
		InputStream is = file.getContents();
		String encoding = file.getCharset();
		if (is == null)
			return null;
		BufferedReader reader = null;
		try {
			StringBuffer buffer = new StringBuffer();
			char[] part = new char[2048];
			int read = 0;
			reader = new BufferedReader(new InputStreamReader(is, encoding));

			while ((read = reader.read(part)) != -1)
				buffer.append(part, 0, read);

			return buffer.toString();

		} catch (IOException ex) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}

	private ResourceSnapshot snapshotFromResource(IResource resource)
			throws CoreException {
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

	private IProgressMonitor getMonitor() {
		return null;
	}

	private String getRandomString() {
		switch ((int) Math.round(Math.random() * 10)) {
		case 0:
			return "este e' o meu conteudo (portuguese)";
		case 1:
			return "ho ho ho";
		case 2:
			return "I'll be back";
		case 3:
			return "don't worry, be happy";
		case 4:
			return "there is no imagination for more sentences";
		case 5:
			return "customize yours";
		case 6:
			return "foo";
		case 7:
			return "bar";
		case 8:
			return "foobar";
		case 9:
			return "case 9";
		default:
			return "these are my contents";
		}
	}

	/**
	 * Returns a FileStore instance backed by storage in a temporary location.
	 * The returned store will not exist, but will belong to an existing parent.
	 * The tearDown method in this class will ensure the location is deleted
	 * after the test is completed.
	 */
	private IFileStore getTempStore() {
		IFileStore store = EFS.getLocalFileSystem().getStore(
				FileSystemHelper.getRandomLocation(FileSystemHelper
						.getTempDir()));
		storesToDelete.add(store);
		return store;
	}

	/**
	 * Returns the URI for a unique, existent folder backed by storage in a
	 * temporary location. The tearDown method in this class will ensure the
	 * location is deleted after the test is completed.
	 */
	private URI getTempProjectDir() throws CoreException {
		IFileStore store = getTempStore();
		store.mkdir(EFS.NONE, getMonitor());
		return store.toURI();
	}

	private void clear(IFileStore store) {
		try {
			store.delete(EFS.NONE, null);
		} catch (CoreException e) {
		}
	}

	private InputStream getContents(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}

	private Map getInitialMarkerAttributes() {
		HashMap map = new HashMap();
		map.putAll(initialAttributes);
		return map;
	}

	private Map getUpdatedMarkerAttributes() {
		HashMap map = new HashMap();
		map.putAll(updatedAttributes);
		return map;
	}

	private IProjectDescription getNewProjectDescription() {
		return getWorkspace().newProjectDescription(TEST_NEWPROJECT_NAME);
	}

	private void execute(AbstractWorkspaceOperation operation)
			throws ExecutionException {
		operation.setQuietCompute(true);
		assertTrue("Operation can be executed", operation.canExecute());
		IStatus status = history.execute(operation, getMonitor(), null);
		assertTrue("Execution should be OK status", status.isOK());
	}

	private void executeExpectFail(AbstractWorkspaceOperation operation)
			throws ExecutionException {
		operation.setQuietCompute(true);
		IStatus status = history.execute(operation, getMonitor(), null);
		assertFalse("Execution should not have OK status", status.isOK());
	}

	private void undo() throws ExecutionException {
		assertTrue("Operation can be undone", history.canUndo(context));
		IStatus status = history.undo(context, getMonitor(), null);
		assertTrue("Undo should be OK status", status.isOK());
	}

	private void undoExpectFail(AbstractWorkspaceOperation operation)
			throws ExecutionException {
		operation.setQuietCompute(true);
		IStatus status = history.undo(context, getMonitor(), null);
		assertFalse("Undo should not have OK status", status.isOK());
	}

	private void redo() throws ExecutionException {
		assertTrue("Operation can be redone", history.canRedo(context));
		IStatus status = history.redo(context, getMonitor(), null);
		assertTrue("Redo should be OK status", status.isOK());
	}

	private void validateCreatedMarkers(int expectedCount, IMarker[] markers,
			Map[] expectedAttributes, String[] expectedTypes)
			throws CoreException {

		assertTrue(MessageFormat.format("{0} markers should have been created",
				new Object[] { new Integer(expectedCount) }),
				markers.length == expectedCount);

		for (int i = 0; i < markers.length; i++) {
			IMarker createdMarker = markers[i];
			assertTrue("Marker should exist", createdMarker.exists());
			assertTrue("Marker should have expected attributes", createdMarker
					.getAttributes().equals(expectedAttributes[i]));
			assertTrue("Marker should have expected type", createdMarker
					.getType().equals(expectedTypes[i]));
		}
	}

	public void testCreateSingleMarkerUndoRedo() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK };
		Map[] attrs = new Map[] { getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(
				IMarker.BOOKMARK, getInitialMarkerAttributes(), emptyTestFile,
				"Create Single Marker Test");
		execute(op);
		IMarker[] markers = op.getMarkers();
		validateCreatedMarkers(1, markers, attrs, types);
		undo();
		assertFalse("Marker should no longer exist", markers[0].exists());
		redo();
		markers = op.getMarkers();
		validateCreatedMarkers(1, markers, attrs, types);
	}

	public void testCreateMultipleMarkersSingleTypeUndoRedo()
			throws ExecutionException, CoreException {
		String[] types = new String[] { CUSTOM_TYPE, CUSTOM_TYPE, CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getUpdatedMarkerAttributes(), getInitialMarkerAttributes() };

		CreateMarkersOperation op = new CreateMarkersOperation(CUSTOM_TYPE,
				attrs, new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Single Type Test");
		execute(op);
		IMarker[] markers = op.getMarkers();
		validateCreatedMarkers(3, markers, attrs, types);
		undo();
		for (int i = 0; i < markers.length; i++) {
			IMarker createdMarker = markers[i];
			assertFalse("Marker should no longer exist", createdMarker.exists());
		}
		redo();
		markers = op.getMarkers();
		validateCreatedMarkers(3, markers, attrs, types);
	}

	public void testCreateMultipleMarkerTypesUndoRedo()
			throws ExecutionException, CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getUpdatedMarkerAttributes(), getInitialMarkerAttributes() };

		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile }, "Create Multiple Marker Types Test");
		execute(op);
		IMarker[] markers = op.getMarkers();
		validateCreatedMarkers(3, markers, attrs, types);
		undo();
		for (int i = 0; i < markers.length; i++) {
			IMarker createdMarker = markers[i];
			assertFalse("Marker should no longer exist", createdMarker.exists());
		}
		redo();
		markers = op.getMarkers();
		validateCreatedMarkers(3, markers, attrs, types);
	}

	public void testUpdateSingleMarkerUndoRedo() throws ExecutionException,
			CoreException {
		CreateMarkersOperation op = new CreateMarkersOperation(
				IMarker.BOOKMARK, getInitialMarkerAttributes(),
				testFileWithContent, "Create Marker Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers()[0], getUpdatedMarkerAttributes(),
				"Update Single Marker", false);
		execute(updateOp);
		validateCreatedMarkers(1, updateOp.getMarkers(),
				new Map[] { getUpdatedMarkerAttributes() },
				new String[] { IMarker.BOOKMARK });
		undo();
		validateCreatedMarkers(1, updateOp.getMarkers(),
				new Map[] { getInitialMarkerAttributes() },
				new String[] { IMarker.BOOKMARK });
		redo();
		validateCreatedMarkers(1, updateOp.getMarkers(),
				new Map[] { getUpdatedMarkerAttributes() },
				new String[] { IMarker.BOOKMARK });

	}

	public void testUpdateMultipleMarkerUndoRedo() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getInitialMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers(), getUpdatedMarkerAttributes(),
				"Update Multiple Markers", false);
		execute(updateOp);
		validateCreatedMarkers(3, updateOp.getMarkers(), new Map[] {
				getUpdatedMarkerAttributes(), getUpdatedMarkerAttributes(),
				getUpdatedMarkerAttributes() }, types);
		undo();
		validateCreatedMarkers(3, updateOp.getMarkers(), attrs, types);
		redo();
		validateCreatedMarkers(3, updateOp.getMarkers(), new Map[] {
				getUpdatedMarkerAttributes(), getUpdatedMarkerAttributes(),
				getUpdatedMarkerAttributes() }, types);

	}

	public void testUpdateAndMergeSingleMarkerUndoRedo()
			throws ExecutionException, CoreException {
		CreateMarkersOperation op = new CreateMarkersOperation(
				IMarker.BOOKMARK, getInitialMarkerAttributes(), testLinkedFile,
				"Create Marker Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers()[0], getUpdatedMarkerAttributes(),
				"Update And Merge Single Marker", true);
		execute(updateOp);
		validateCreatedMarkers(1, updateOp.getMarkers(),
				new Map[] { mergedUpdatedAttributes },
				new String[] { IMarker.BOOKMARK });
		undo();
		validateCreatedMarkers(1, updateOp.getMarkers(),
				new Map[] { getInitialMarkerAttributes() },
				new String[] { IMarker.BOOKMARK });
		redo();
		validateCreatedMarkers(1, updateOp.getMarkers(),
				new Map[] { mergedUpdatedAttributes },
				new String[] { IMarker.BOOKMARK });

	}

	public void testUpdateAndMergeMultipleMarkerUndoRedo()
			throws ExecutionException, CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getInitialMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers(), getUpdatedMarkerAttributes(),
				"Update and Merge Multiple Markers", true);
		execute(updateOp);
		validateCreatedMarkers(3, updateOp.getMarkers(), new Map[] {
				mergedUpdatedAttributes, mergedUpdatedAttributes,
				mergedUpdatedAttributes }, types);
		undo();
		validateCreatedMarkers(3, updateOp.getMarkers(), attrs, types);
		redo();
		validateCreatedMarkers(3, updateOp.getMarkers(), new Map[] {
				mergedUpdatedAttributes, mergedUpdatedAttributes,
				mergedUpdatedAttributes }, types);
	}

	public void testDeleteMarkersUndoRedo() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getUpdatedMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		IMarker[] markers = op.getMarkers();
		DeleteMarkersOperation deleteOp = new DeleteMarkersOperation(markers,
				"Delete Markers Test");
		execute(deleteOp);
		for (int i = 0; i < markers.length; i++) {
			IMarker createdMarker = markers[i];
			assertFalse("Marker should no longer exist", createdMarker.exists());
		}
		undo();
		markers = deleteOp.getMarkers();
		validateCreatedMarkers(3, markers, attrs, types);
		redo();
		for (int i = 0; i < markers.length; i++) {
			IMarker createdMarker = markers[i];
			assertFalse("Marker should no longer exist", createdMarker.exists());
		}
	}

	/*
	 * Test that the undo is invalid because one of the markers was deleted.
	 */
	public void testCreateMarkerUndoInvalid() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getUpdatedMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		IMarker[] markers = op.getMarkers();
		markers[1].delete();
		// Must compute status first because we don't perform expensive
		// validations in canUndo(). However we should remember the validity
		// once we've computed the status.
		op.computeUndoableStatus(null);
		assertFalse("Undo should be invalid, marker no longer exists", op
				.canUndo());
	}

	/*
	 * Test that the undo is invalid because one of the resources was deleted.
	 */
	public void testCreateMarkerUndoInvalid2() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getUpdatedMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		emptyTestFile.delete(true, getMonitor());
		// Must compute status first because we don't perform expensive
		// validations in canUndo(). However we should remember the validity
		// once we've computed the status.
		op.computeUndoableStatus(null);
		assertFalse("Undo should be invalid, resource no longer exists", op
				.canUndo());
	}

	public void testUpdateMarkersInvalid() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getInitialMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers(), getUpdatedMarkerAttributes(),
				"Update and Merge Multiple Markers", true);
		execute(updateOp);
		IMarker[] markers = updateOp.getMarkers();
		markers[0].delete();
		// Must compute status first because we don't perform expensive
		// validations in canUndo(). However we should remember the validity
		// once we've computed the status.
		updateOp.computeUndoableStatus(null);

		assertFalse("Undo should be invalid, marker no longer exists", updateOp
				.canUndo());
	}

	public void testUpdateMarkersInvalid2() throws ExecutionException,
			CoreException {
		String[] types = new String[] { IMarker.BOOKMARK, IMarker.TASK,
				CUSTOM_TYPE };
		Map[] attrs = new Map[] { getInitialMarkerAttributes(),
				getInitialMarkerAttributes(), getInitialMarkerAttributes() };
		CreateMarkersOperation op = new CreateMarkersOperation(types, attrs,
				new IFile[] { emptyTestFile, testFileWithContent,
						testLinkedFile },
				"Create Multiple Markers Same Type Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers(), getUpdatedMarkerAttributes(),
				"Update and Merge Multiple Markers", true);
		execute(updateOp);
		testFileWithContent.delete(true, getMonitor());
		// Must compute status first because we don't perform expensive
		// validations in canUndo(). However we should remember the validity
		// once we've computed the status.
		updateOp.computeUndoableStatus(null);

		assertFalse("Undo should be invalid, marker no longer exists", updateOp
				.canUndo());
	}

	public void testProjectCreateUndoRedo() throws ExecutionException,
			CoreException {
		CreateProjectOperation op = new CreateProjectOperation(
				getNewProjectDescription(), "testProjectCreate");
		execute(op);
		IProject project = getWorkspaceRoot().getProject(TEST_NEWPROJECT_NAME);
		assertTrue("Project creation failed", project.exists());
		ProjectSnapshot snap = new ProjectSnapshot(project);
		undo();
		assertFalse("Project deletion failed", project.exists());
		redo();
		assertTrue("Project recreation failed", project.exists());
		assertTrue("Project not restored properly", snap.isValid());
	}

	public void testProjectMoveUndoRedo() throws ExecutionException,
			CoreException {
		URI projectTargetLocation = URIUtil.toURI(URIUtil.toPath(
				getTempProjectDir()).append(TEST_PROJECT_NAME));
		MoveProjectOperation op = new MoveProjectOperation(testProject,
				projectTargetLocation, "testProjectMove");
		ProjectSnapshot snap = new ProjectSnapshot(testProject);
		execute(op);
		assertNotNull("Project move failed", testProject.getDescription()
				.getLocationURI());
		assertTrue("Project contents were altered", snap.isValid());
		undo();
		assertNull("Project move undo failed", testProject.getDescription()
				.getLocationURI());
		assertTrue("Project contents were altered", snap.isValid());
		redo();
		assertEquals("Project move redo failed", testProject.getDescription()
				.getLocationURI(), projectTargetLocation);
		assertTrue("Project contents were altered", snap.isValid());
	}

	public void testProjectMoveInvalidLocationUndoRedo()
			throws ExecutionException {
		// invalid target - already used by another project
		MoveProjectOperation op = new MoveProjectOperation(testProject,
				targetProject.getLocationURI(),
				"testProjectMoveInvalidLocation");
		executeExpectFail(op);
	}

	public void testProjectCopyUndoRedo() throws ExecutionException,
			CoreException {
		CopyProjectOperation op = new CopyProjectOperation(testProject,
				TEST_NEWPROJECT_NAME, null, "testProjectCopy");
		ProjectSnapshot snap = new ProjectSnapshot(testProject);
		execute(op);
		IProject copiedProject = getWorkspaceRoot().getProject(
				TEST_NEWPROJECT_NAME);
		assertTrue("Project copy failed", copiedProject.exists());
		assertTrue("Source project was altered", snap.isValid());
		snap.name = TEST_NEWPROJECT_NAME;
		assertTrue("Project copy does not match", snap.isValid());
		undo();
		assertFalse("Copy undo failed", copiedProject.exists());
		redo();
		assertTrue("Project not restored properly on redo", snap.isValid());
		snap.name = TEST_PROJECT_NAME;
		assertTrue("Source project was altered", snap.isValid());
	}

	public void testProjectClosedCopyUndoRedo() throws ExecutionException,
			CoreException {
		testProject.close(getMonitor());
		testProjectCopyUndoRedo();
	}

	public void testProjectCopyAndChangeLocationUndoRedo()
			throws ExecutionException, CoreException {
		URI projectTargetLocation = URIUtil.toURI(URIUtil.toPath(
				getTempProjectDir()).append(TEST_PROJECT_NAME));
		CopyProjectOperation op = new CopyProjectOperation(testProject,
				TEST_NEWPROJECT_NAME, projectTargetLocation,
				"testProjectCopyToNewLocation");
		ProjectSnapshot snap = new ProjectSnapshot(testProject);
		execute(op);
		IProject copiedProject = getWorkspaceRoot().getProject(
				TEST_NEWPROJECT_NAME);
		assertTrue("Project copy failed", copiedProject.exists());
		assertEquals("Project location copy failed", copiedProject
				.getDescription().getLocationURI(), projectTargetLocation);
		assertTrue("Source project was altered", snap.isValid());
		snap.name = TEST_NEWPROJECT_NAME;
		assertTrue("Project copy does not match", snap.isValid());
		undo();
		assertFalse("Copy undo failed", copiedProject.exists());
		redo();
		assertTrue("Project not restored properly on redo", snap.isValid());
		assertEquals("Project location not restored properly", copiedProject
				.getDescription().getLocationURI(), projectTargetLocation);
		snap.name = TEST_PROJECT_NAME;
		assertTrue("Source project was altered", snap.isValid());
	}

	public void testProjectClosedCopyAndChangeLocationUndoRedo()
			throws ExecutionException, CoreException {
		testProject.close(getMonitor());
		testProjectCopyAndChangeLocationUndoRedo();
	}

	public void testProjectCopyAndChangeToInvalidLocationUndoRedo()
			throws ExecutionException {
		// invalid target - already used by another project
		CopyProjectOperation op = new CopyProjectOperation(testProject,
				TEST_NEWPROJECT_NAME, targetProject.getLocationURI(),
				"testProjectCopyInvalidLocation");
		executeExpectFail(op);
	}

	public void testProjectRenameUndoRedo() throws ExecutionException,
			CoreException {
		MoveResourcesOperation op = new MoveResourcesOperation(testProject,
				new Path(TEST_NEWPROJECT_NAME), "testProjectRename");
		ProjectSnapshot snap = new ProjectSnapshot(testProject);
		execute(op);
		IProject renamedProject = getWorkspaceRoot().getProject(
				TEST_NEWPROJECT_NAME);
		assertTrue("Project rename failed", renamedProject.exists());
		snap.name = TEST_NEWPROJECT_NAME;
		assertTrue("Project content was altered on rename", snap.isValid());
		undo();
		snap.name = TEST_PROJECT_NAME;
		assertTrue("Project content was altered on undo rename", snap.isValid());
		assertFalse("Undo rename failed", renamedProject.exists());
		redo();
		snap.name = TEST_NEWPROJECT_NAME;
		assertTrue("Project content was altered on redo rename", snap.isValid());
	}

	public void testProjectDeleteUndoRedo() throws ExecutionException, CoreException {
		ProjectSnapshot snap = new ProjectSnapshot(testProject);
		
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testProject }, "testProjectDelete", false);
		execute(op);
		assertFalse("Project delete failed", testProject.exists());
		undo();
		assertTrue("Project recreation failed", testProject.exists());
		// force a refresh so that the project is in sync with the file system.  Normally the
		// project opens in the background, so without this refresh we may or may not have
		// the contents yet.
		testProject.refreshLocal(IResource.DEPTH_INFINITE, getMonitor());
		assertTrue("Project content was altered on undo", snap.isValid());
		redo();
		assertFalse("Redo delete failed", testProject.exists());
		// We undo again so that the project will exist during teardown and
		// get cleaned up. Otherwise some content is left on disk.
		undo();
	}
	
	public void test223956() throws ExecutionException, CoreException {
		// put a marker on a file contained in the test project
		Map[] attrs = new Map[] { getInitialMarkerAttributes()};
		CreateMarkersOperation markerCreate = new CreateMarkersOperation(new String[] { IMarker.BOOKMARK }, attrs,
				new IFile[] { testFileWithContent},
				"Test bug 223956");
		execute(markerCreate);
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testProject }, "testProjectDelete", false);
		execute(op);
		assertFalse("Project delete failed", testProject.exists());
		undo();
		assertTrue("Project recreation failed", testProject.exists());
		assertTrue("Marker should not exist at project level", testProject.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO).length == 0);
		assertTrue("Marker should have been restored in child file", testFileWithContent.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO).length == 1);
	}
	
	public void test201441() throws ExecutionException, CoreException {
		String utf8 = "UTF-8";
		// set the charset on the project explicitly
		testProject.setDefaultCharset(utf8, getMonitor());
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testProject }, "testProjectDelete", false);
		execute(op);
		assertFalse("Project delete failed", testProject.exists());
		undo();
		assertTrue("Project recreation failed", testProject.exists());
		assertEquals("Character set not restored", testProject.getDefaultCharset(), utf8);
	}

	public void testProjectClosedDeleteUndoRedo() throws ExecutionException,
			CoreException {
		testProject.close(getMonitor());
		testProjectDeleteUndoRedo();
	}

	public void testProjectDeleteWithContentUndoRedo()
			throws ExecutionException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testProject }, "testProjectDelete", true);
		// we don't snapshot since content will be deleted
		execute(op);
		assertFalse("Project delete failed", testProject.exists());
		undo();
		assertTrue("Project was recreated", testProject.exists());
		redo();
		assertFalse("Redo delete failed", testProject.exists());
	}

	public void testProjectClosedDeleteWithContentUndoRedo()
			throws ExecutionException, CoreException {
		testProject.close(getMonitor());
		testProjectDeleteWithContentUndoRedo();
	}

	public void testFolderCreateLeafUndoRedo() throws ExecutionException {
		IFolder folder = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(TEST_NEWFOLDER_NAME));
		CreateFolderOperation op = new CreateFolderOperation(folder, null,
				"testFolderCreateLeaf");
		assertFalse("Folder should not exist before test is run", folder
				.exists());
		execute(op);
		assertTrue("Folder creation failed", folder.exists());
		undo();
		assertFalse("Folder deletion failed", folder.exists());
		redo();
		assertTrue("Folder recreation failed", folder.exists());
	}

	public void testFolderCreateNestedInProjectUndoRedo()
			throws ExecutionException {
		// uses a nested path to force creation of nonexistent parents
		IFolder folder = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(TEST_NEWNESTEDFOLDER_NAME));
		CreateFolderOperation op = new CreateFolderOperation(folder, null,
				"testFolderCreateNested");
		assertFalse("Folder should not exist before test is run", folder
				.exists());
		execute(op);
		assertTrue("Folder creation failed", folder.exists());
		undo();
		assertFalse("Folder deletion failed", folder.exists());
		// Ensure all created parents are gone, too
		IPath path = new Path(TEST_NEWNESTEDFOLDER_NAME);
		path.removeLastSegments(path.segmentCount() - 1);
		IFolder parent = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(path));
		assertFalse("Deletion of created parents failed", parent.exists());

		redo();
		assertTrue("Folder recreation failed", folder.exists());
	}

	public void testFolderCreateNestedInFolderUndoRedo()
			throws ExecutionException {
		// Uses a nested path to force creation of nonexistent parents.
		// Parent is a folder, not a project.
		IFolder folder = getWorkspaceRoot().getFolder(
				testFolder.getFullPath().append(TEST_NEWNESTEDFOLDER_NAME));
		CreateFolderOperation op = new CreateFolderOperation(folder, null,
				"testFolderCreateNestedInFolder");
		assertFalse("Folder should not exist before test is run", folder
				.exists());
		execute(op);
		assertTrue("Folder creation failed", folder.exists());

		undo();
		assertFalse("Folder deletion failed", folder.exists());
		// Ensure all created parents are gone, too
		IPath path = new Path(TEST_NEWNESTEDFOLDER_NAME);
		path.removeLastSegments(path.segmentCount() - 1);
		IFolder parent = getWorkspaceRoot().getFolder(
				testFolder.getFullPath().append(path));
		assertFalse("Deletion of created parents failed", parent.exists());

		redo();
		assertTrue("Folder recreation failed", folder.exists());
	}

	public void testDeleteNestedResourcesUndoRedo()
			throws ExecutionException {
		// Creates nested folders and then tests that mass deletion of these records only the
		// deepest.
		IFolder folder = getWorkspaceRoot().getFolder(
				testFolder.getFullPath().append(TEST_NEWNESTEDFOLDER_NAME));
		IFolder parent = getWorkspaceRoot().getFolder(testFolder.getFullPath().append(TEST_NESTEDFOLDER_PARENT_NAME));
		IFolder root = getWorkspaceRoot().getFolder(testFolder.getFullPath().append(TEST_NESTEDFOLDER_ROOT_PARENT_NAME));

		AbstractWorkspaceOperation op = new CreateFolderOperation(folder, null,
				"testFolderCreateNestedInFolder");
		execute(op);
		assertTrue("Folder creation failed", folder.exists());
		assertTrue("Folder creation failed", parent.exists());
		assertTrue("Folder creation failed", root.exists());

		op = new DeleteResourcesOperation(new IResource[] {folder, parent, root}, "testDeleteNestedResourcesUndoRedo", true);
		execute(op);
		assertFalse("Folder deletion failed", folder.exists());
		assertFalse("Folder deletion failed", parent.exists());
		assertFalse("Folder deletion failed", root.exists());
		
		undo();
		assertTrue("Folder creation failed", folder.exists());
		assertTrue("Folder creation failed", parent.exists());
		assertTrue("Folder creation failed", root.exists());

	}

	public void testFolderCreateLinkedUndoRedo() throws ExecutionException {
		IFolder folder = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(TEST_NEWFOLDER_NAME));
		CreateFolderOperation op = new CreateFolderOperation(folder,
				testSubFolder.getLocationURI(), "testFolderCreateLink");
		assertFalse("Folder should not exist before test is run", folder
				.exists());
		execute(op);
		assertTrue("Folder creation failed", folder.exists());
		assertTrue("Folder was not created as a link", folder.isLinked());
		undo();
		assertFalse("Folder deletion failed", folder.exists());
		redo();
		assertTrue("Folder recreation failed", folder.exists());
		assertTrue("Folder was not recreated as a link", folder.isLinked());
	}

	public void testFolderCreateLinkedNestedUndoRedo()
			throws ExecutionException {
		// Use nested name with uncreated parents
		IFolder folder = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(TEST_NEWNESTEDFOLDER_NAME));
		CreateFolderOperation op = new CreateFolderOperation(folder,
				testSubFolder.getLocationURI(), "testFolderCreateNestedLink");
		assertFalse("Folder should not exist before test is run", folder
				.exists());
		execute(op);
		assertTrue("Folder creation failed", folder.exists());
		assertTrue("Folder was not created as a link", folder.isLinked());
		undo();
		assertFalse("Folder deletion failed", folder.exists());
		// Ensure all created parents are gone, too
		IPath path = new Path(TEST_NEWNESTEDFOLDER_NAME);
		path.removeLastSegments(path.segmentCount() - 1);
		IFolder parent = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(path));
		assertFalse("Parents should have been removed", parent.exists());
		// Redo
		redo();
		assertTrue("Folder recreation failed", folder.exists());
		assertTrue("Folder was not recreated as a link", folder.isLinked());
	}

	public void testFolderMoveUndoRedo() throws ExecutionException,
			CoreException {
		IPath targetPath = targetProject.getFullPath().append(
				testFolder.getName());
		MoveResourcesOperation op = new MoveResourcesOperation(testFolder,
				targetPath, "testFolderMove");
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		IFolder movedFolder = getWorkspaceRoot().getFolder(targetPath);
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snap.isValid(targetProject));

		undo();
		movedFolder = getWorkspaceRoot().getFolder(targetPath);
		assertFalse("Move undo failed", movedFolder.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testProject));

		redo();
		movedFolder = getWorkspaceRoot().getFolder(targetPath);
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snap.isValid(targetProject));
	}

	public void testRedundantSubFolderMoveUndoRedo() throws ExecutionException,
			CoreException {
		IPath targetPath = targetProject.getFullPath();
		IPath targetPathWithName = targetPath.append(testFolder.getName());
		MoveResourcesOperation op = new MoveResourcesOperation(new IResource[] {
				testFolder, testSubFolder }, targetPath,
				"testRedundantSubFolderMove");
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		IFolder movedFolder = getWorkspaceRoot().getFolder(targetPathWithName);
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snap.isValid(targetProject));

		undo();
		movedFolder = getWorkspaceRoot().getFolder(targetPathWithName);
		assertFalse("Move undo failed", movedFolder.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testProject));

		redo();
		movedFolder = getWorkspaceRoot().getFolder(targetPathWithName);
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snap.isValid(targetProject));
	}

	public void testRedundantFolderFileMoveUndoRedo()
			throws ExecutionException, CoreException {
		IPath targetPath = targetProject.getFullPath();
		IPath targetPathWithName = targetPath.append(testFolder.getName());
		MoveResourcesOperation op = new MoveResourcesOperation(new IResource[] {
				testFolder, testFileWithContent }, targetPath,
				"testRedundantFolderFileMove");
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		IFolder movedFolder = getWorkspaceRoot().getFolder(targetPathWithName);
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snap.isValid(targetProject));

		undo();
		movedFolder = getWorkspaceRoot().getFolder(targetPathWithName);
		assertFalse("Move undo failed", movedFolder.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testProject));

		redo();
		movedFolder = getWorkspaceRoot().getFolder(targetPathWithName);
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snap.isValid(targetProject));
	}

	public void testFolderCopyUndoRedo() throws ExecutionException,
			CoreException {
		// copying with same name to a new project
		CopyResourcesOperation op = new CopyResourcesOperation(
				new IResource[] { testFolder }, targetProject.getFullPath(),
				"testFolderCopy");
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		IFolder copiedFolder = targetProject.getFolder(testFolder.getName());
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snap.isValid(testProject));
		assertTrue("Folder copy does not match", snap.isValid(targetProject));

		undo();
		assertFalse("Copy undo failed", copiedFolder.exists());

		redo();
		assertTrue("Folder not restored properly on redo", snap
				.isValid(targetProject));
		assertTrue("Source folder was altered", snap.isValid(testProject));
	}

	public void testFolderCopyLinkUndoRedo() throws ExecutionException,
			CoreException {
		// copying with same name to a new project
		CopyResourcesOperation op = new CopyResourcesOperation(
				new IResource[] { testLinkedFolder }, targetProject
						.getFullPath(), "testLinkedFolderCopy");
		FolderSnapshot snap = new FolderSnapshot(testLinkedFolder);
		execute(op);
		IFolder copiedFolder = targetProject.getFolder(testLinkedFolder
				.getName());
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snap.isValid(testFolder));
		assertTrue("Folder copy does not match", snap.isValid(targetProject));

		undo();
		assertFalse("Copy undo failed", copiedFolder.exists());

		redo();
		assertTrue("Folder not restored properly on redo", snap
				.isValid(targetProject));
		assertTrue("Source folder was altered", snap.isValid(testFolder));
	}

	public void testFolderCopyRenameUndoRedo() throws ExecutionException,
			CoreException {
		// copying with a different name to the same project
		CopyResourcesOperation op = new CopyResourcesOperation(testFolder,
				testProject.getFullPath().append(TEST_NEWFOLDER_NAME),
				"testFolderCopyRename");
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);

		IFolder copiedFolder = testProject.getFolder(TEST_NEWFOLDER_NAME);
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snap.isValid(testProject));
		snap.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder copy does not match", snap.isValid(testProject));

		undo();
		assertFalse("Copy undo failed", copiedFolder.exists());

		redo();
		assertTrue("Folder not restored properly on redo", snap
				.isValid(testProject));
		snap.name = TEST_FOLDER_NAME;
		assertTrue("Source folder was altered", snap.isValid(testProject));

	}

	public void testFolderRenameUndoRedo() throws ExecutionException,
			CoreException {
		MoveResourcesOperation op = new MoveResourcesOperation(testFolder,
				testProject.getFullPath().append(TEST_NEWFOLDER_NAME),
				"testFolderRename");
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		IFolder renamedFolder = testProject.getFolder(TEST_NEWFOLDER_NAME);
		assertTrue("Project rename failed", renamedFolder.exists());
		snap.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder content was altered on rename", snap
				.isValid(testProject));

		undo();
		snap.name = TEST_FOLDER_NAME;
		assertTrue("Folder content was altered on undo rename", snap
				.isValid(testProject));
		assertFalse("Undo rename failed", renamedFolder.exists());

		redo();
		snap.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder content was altered on redo rename", snap
				.isValid(testProject));
	}

	public void testFolderDeleteUndoRedo() throws ExecutionException,
			CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testSubFolder }, "testFolderDelete", false);
		FolderSnapshot snap = new FolderSnapshot(testSubFolder);
		execute(op);
		assertFalse("Folder delete failed", testSubFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testSubFolder.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testSubFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testSubFolder.exists());
	}

	public void testNestedRedundantFolderDeleteUndoRedo()
			throws ExecutionException, CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testFolder, testSubFolder },
				"testNestedRedundantFolderDelete", false);
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		assertFalse("Folder delete failed", testFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testFolder.exists());
		assertTrue("SubFolder recreation failed", testSubFolder.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testFolder.exists());
	}

	public void testNestedRedundantFileDeleteUndoRedo()
			throws ExecutionException, CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testFolder, testFileWithContent },
				"testNestedRedundantFileDelete", false);
		FolderSnapshot snap = new FolderSnapshot(testFolder);
		execute(op);
		assertFalse("Folder delete failed", testFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testFolder.exists());
		assertTrue("SubFolder recreation failed", testSubFolder.exists());
		assertTrue("File recreation failed", testFileWithContent.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testFolder.exists());
	}

	public void testFolderDeleteLinkedUndoRedo() throws ExecutionException,
			CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testLinkedFolder }, "testFolderDeleteLinked",
				false);
		FolderSnapshot snap = new FolderSnapshot(testLinkedFolder);
		execute(op);
		assertFalse("Folder delete failed", testLinkedFolder.exists());
		undo();
		assertTrue("Folder recreation failed", testLinkedFolder.exists());
		assertTrue("Folder content was altered on undo", snap
				.isValid(testLinkedFolder.getParent()));
		redo();
		assertFalse("Redo delete failed", testLinkedFolder.exists());
	}

	public void testFileCreateLeafUndoRedo() throws ExecutionException,
			CoreException {
		IFile file = getWorkspaceRoot().getFile(
				testProject.getFullPath().append(TEST_NEWFILE_NAME));
		CreateFileOperation op = new CreateFileOperation(file, null,
				getContents(getRandomString()), "testFileCreateLeaf");
		assertFalse("File should not exist before test is run", file.exists());
		execute(op);
		assertTrue("File creation failed", file.exists());
		FileSnapshot snapshot = new FileSnapshot(file);
		undo();
		assertFalse("File deletion failed", file.exists());
		redo();
		assertTrue("File recreation failed", file.exists());
		assertTrue("File content improperly restored", snapshot.isValid(file
				.getParent()));
	}

	public void testFileCreateNestedInProjectUndoRedo()
			throws ExecutionException, CoreException {
		// Uses file name with non-existent folder parents
		IFile file = getWorkspaceRoot().getFile(
				testProject.getFullPath().append(TEST_NEWNESTEDFILE_NAME));
		CreateFileOperation op = new CreateFileOperation(file, null,
				getContents(getRandomString()), "testFileCreateNestedInProject");
		assertFalse("File should not exist before test is run", file.exists());
		execute(op);
		assertTrue("File creation failed", file.exists());
		FileSnapshot snapshot = new FileSnapshot(file);

		undo();
		assertFalse("File deletion failed", file.exists());
		// Ensure all created parents are gone, too
		IPath path = new Path(TEST_NEWNESTEDFILE_NAME);
		path.removeLastSegments(path.segmentCount() - 1);
		IFolder parent = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(path));
		assertFalse("Deletion of created parents failed", parent.exists());

		redo();
		assertTrue("File recreation failed", file.exists());
		assertTrue("File content improperly restored", snapshot.isValid(file
				.getParent()));
	}

	public void testFileCreateNestedInFolderUndoRedo()
			throws ExecutionException, CoreException {
		// Uses file name with non-existent folder parents.
		// Uses subfolder as a parent
		IFile file = getWorkspaceRoot().getFile(
				testSubFolder.getFullPath().append(TEST_NEWNESTEDFILE_NAME));
		CreateFileOperation op = new CreateFileOperation(file, null,
				getContents(getRandomString()),
				"testFileCreateNestedInSubfolder");
		assertFalse("File should not exist before test is run", file.exists());
		execute(op);
		assertTrue("File creation failed", file.exists());
		FileSnapshot snapshot = new FileSnapshot(file);

		undo();
		assertFalse("File deletion failed", file.exists());
		// Ensure all created parents are gone, too
		IPath path = new Path(TEST_NEWNESTEDFILE_NAME);
		path.removeLastSegments(path.segmentCount() - 1);
		IFolder parent = getWorkspaceRoot().getFolder(
				testSubFolder.getFullPath().append(path));
		assertFalse("Deletion of created parents failed", parent.exists());

		redo();
		assertTrue("File recreation failed", file.exists());
		assertTrue("File content improperly restored", snapshot.isValid(file
				.getParent()));
	}

	public void testFileCreateLinkedUndoRedo() throws ExecutionException,
			CoreException {
		IFile file = getWorkspaceRoot().getFile(
				testProject.getFullPath().append(TEST_NEWFILE_NAME));
		CreateFileOperation op = new CreateFileOperation(file,
				testFileWithContent.getLocationURI(), null,
				"testFileCreateLink");
		assertFalse("File should not exist before test is run", file.exists());

		execute(op);
		assertTrue("File creation failed", file.exists());
		assertTrue("File was not created as link", file.isLinked());
		assertEquals("Linked content not equal", readContent(file),
				readContent(testFileWithContent));

		undo();
		assertFalse("File deletion failed", file.exists());

		redo();
		assertTrue("File was not created as link", file.isLinked());
		assertEquals("Linked content not equal", readContent(file),
				readContent(testFileWithContent));
	}

	public void testFileCreateLinkedNestedUndoRedo() throws ExecutionException,
			CoreException {
		IFile file = getWorkspaceRoot().getFile(
				testProject.getFullPath().append(TEST_NEWNESTEDFILE_NAME));
		CreateFileOperation op = new CreateFileOperation(file,
				testFileWithContent.getLocationURI(), null,
				"testFileCreateLinkNested");
		assertFalse("File should not exist before test is run", file.exists());

		execute(op);
		assertTrue("File creation failed", file.exists());
		assertTrue("File was not created as link", file.isLinked());
		assertEquals("Linked content not equal", readContent(file),
				readContent(testFileWithContent));

		undo();
		assertFalse("File deletion failed", file.exists());
		// Ensure all created parents are gone, too
		IPath path = new Path(TEST_NEWNESTEDFILE_NAME);
		path.removeLastSegments(path.segmentCount() - 1);
		IFolder parent = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(path));
		assertFalse("Deletion of created parents failed", parent.exists());

		redo();
		assertTrue("File was not created as link", file.isLinked());
		assertEquals("Linked content not equal", readContent(file),
				readContent(testFileWithContent));
	}

	public void testFileMoveUndoRedo() throws ExecutionException, CoreException {
		// Moving from a folder in one project to the top level of another
		// project
		IPath targetPath = targetProject.getFullPath().append(
				testFileWithContent.getName());
		MoveResourcesOperation op = new MoveResourcesOperation(
				testFileWithContent, targetPath, "testFileMove");
		FileSnapshot snap = new FileSnapshot(testFileWithContent);
		execute(op);
		IFile movedFile = getWorkspaceRoot().getFile(targetPath);
		assertTrue("File move failed", movedFile.exists());
		assertTrue("File content was altered", snap.isValid(targetProject));

		undo();
		movedFile = getWorkspaceRoot().getFile(targetPath);
		assertFalse("Move undo failed", movedFile.exists());
		assertTrue("File content was altered on undo", snap.isValid(testFolder));

		redo();
		movedFile = getWorkspaceRoot().getFile(targetPath);
		assertTrue("File move failed", movedFile.exists());
		assertTrue("File content was altered", snap.isValid(targetProject));
	}

	public void testFileMoveAndOverwriteUndoRedo() throws ExecutionException,
			CoreException {
		// Moving a file from a folder inside that the same folder on top of an
		// existent file
		IPath sourcePath = emptyTestFile.getFullPath();
		IPath targetPath = testFileWithContent.getFullPath();
		MoveResourcesOperation op = new MoveResourcesOperation(emptyTestFile,
				targetPath, "testFileMoveOverwrite");
		FileSnapshot source = new FileSnapshot(emptyTestFile);
		FileSnapshot overwritten = new FileSnapshot(testFileWithContent);
		execute(op);
		IFile sourceFile = getWorkspaceRoot().getFile(sourcePath);
		assertFalse("File move failed", sourceFile.exists());
		source.name = TEST_RANDOMFILE_NAME;
		assertTrue("Source content was altered at target", source
				.isValid(testFolder));

		undo();
		assertTrue("File restore failed", sourceFile.exists());
		assertTrue("Overwritten file was not restored", overwritten
				.isValid(testFolder));
		source.name = TEST_EMPTYFILE_NAME;
		assertTrue("Source file content was not restored", source
				.isValid(testFolder));

		redo();
		sourceFile = getWorkspaceRoot().getFile(sourcePath);
		assertFalse("File move failed", sourceFile.exists());
		source.name = TEST_RANDOMFILE_NAME;
		assertTrue("Source content was altered at target", source
				.isValid(testFolder));
	}

	public void testFileCopyUndoRedo() throws ExecutionException, CoreException {
		// copying with same name to a new project
		CopyResourcesOperation op = new CopyResourcesOperation(
				new IResource[] { testFileWithContent }, targetProject
						.getFullPath(), "testFileCopy");
		FileSnapshot snap = new FileSnapshot(testFileWithContent);
		execute(op);
		IFile copiedFile = targetProject.getFile(testFileWithContent.getName());
		assertTrue("File copy failed", copiedFile.exists());
		assertTrue("Source file was altered", snap.isValid(testFolder));
		assertTrue("File copy does not match", snap.isValid(targetProject));

		undo();
		assertFalse("Copy undo failed", copiedFile.exists());

		redo();
		assertTrue("File not restored properly on redo", snap
				.isValid(targetProject));
		assertTrue("Source file was altered", snap.isValid(testFolder));
	}

	public void testFileCopyLinkUndoRedo() throws ExecutionException,
			CoreException {
		// copying with same name to a new project
		CopyResourcesOperation op = new CopyResourcesOperation(
				new IResource[] { testLinkedFile },
				targetProject.getFullPath(), "testFileLinkCopy");
		FileSnapshot snap = new FileSnapshot(testLinkedFile);
		execute(op);
		IFile copiedFile = targetProject.getFile(testLinkedFile.getName());
		assertTrue("File copy failed", copiedFile.exists());
		assertTrue("Source file was altered", snap.isValid(testFolder));
		assertTrue("File copy does not match", snap.isValid(targetProject));

		undo();
		assertFalse("Copy undo failed", copiedFile.exists());

		redo();
		assertTrue("File not restored properly on redo", snap
				.isValid(targetProject));
		assertTrue("Source file was altered", snap.isValid(testFolder));
	}

	public void testFileCopyRenameUndoRedo() throws ExecutionException,
			CoreException {
		// copying with a different name to the same project
		CopyResourcesOperation op = new CopyResourcesOperation(
				testFileWithContent, testProject.getFullPath().append(
						TEST_NEWFILE_NAME), "testFileCopyRename");
		FileSnapshot snap = new FileSnapshot(testFileWithContent);
		execute(op);

		IFile copiedFile = testProject.getFile(TEST_NEWFILE_NAME);
		assertTrue("File copy failed", copiedFile.exists());
		assertTrue("Source folder was altered", snap.isValid(testFolder));
		snap.name = TEST_NEWFILE_NAME;
		assertTrue("File copy does not match", snap.isValid(testProject));

		undo();
		assertFalse("Copy undo failed", copiedFile.exists());

		redo();
		assertTrue("File not restored properly on redo", snap
				.isValid(testProject));
		snap.name = TEST_RANDOMFILE_NAME;
		assertTrue("Source folder was altered", snap.isValid(testFolder));
	}

	public void testFileCopyAndOverwriteUndoRedo() throws ExecutionException,
			CoreException {
		// Copying from a file in a folder to the same folder on top of an
		// existent file
		IPath targetPath = testFileWithContent.getFullPath();
		CopyResourcesOperation op = new CopyResourcesOperation(emptyTestFile,
				targetPath, "testFileMoveOverwrite");
		FileSnapshot source = new FileSnapshot(emptyTestFile);
		FileSnapshot overwritten = new FileSnapshot(testFileWithContent);
		execute(op);
		assertTrue("Source content was altered", source.isValid(testFolder));
		source.name = TEST_RANDOMFILE_NAME;
		assertTrue("Source content was altered at target", source
				.isValid(testFolder));

		undo();
		assertTrue("Overwritten file was not restored", overwritten
				.isValid(testFolder));
		source.name = TEST_EMPTYFILE_NAME;
		assertTrue("Source file content was not restored", source
				.isValid(testFolder));

		redo();
		source.name = TEST_RANDOMFILE_NAME;
		assertTrue("Source content was altered at target", source
				.isValid(testFolder));
	}

	public void testFileRenameUndoRedo() throws ExecutionException,
			CoreException {
		MoveResourcesOperation op = new MoveResourcesOperation(
				testFileInProject, testProject.getFullPath().append(
						TEST_NEWFILE_NAME), "testFileRename");
		FileSnapshot snap = new FileSnapshot(testFileInProject);
		execute(op);
		IFile renamedFile = testProject.getFile(TEST_NEWFILE_NAME);
		assertTrue("File rename failed", renamedFile.exists());
		snap.name = TEST_NEWFILE_NAME;
		assertTrue("File content was altered on rename", snap
				.isValid(testProject));

		undo();
		snap.name = TEST_FILEINPROJECT_NAME;
		assertTrue("File content was altered on undo rename", snap
				.isValid(testProject));
		assertFalse("Undo rename failed", renamedFile.exists());

		redo();
		snap.name = TEST_NEWFILE_NAME;
		assertTrue("File content was altered on redo rename", snap
				.isValid(testProject));
	}

	public void testFileDeleteUndoRedo() throws ExecutionException,
			CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testFileWithContent }, "testFileDelete",
				false);
		FileSnapshot snap = new FileSnapshot(testFileWithContent);
		execute(op);
		assertFalse("File delete failed", testFileWithContent.exists());
		undo();
		assertTrue("File recreation failed", testFileWithContent.exists());
		assertTrue("File content was altered on undo", snap
				.isValid(testFileWithContent.getParent()));
		redo();
		assertFalse("Redo delete failed", testFileWithContent.exists());
	}

	public void testFileLinkedDeleteUndoRedo() throws ExecutionException,
			CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testLinkedFile }, "testLinkedFileDelete",
				false);
		FileSnapshot snap = new FileSnapshot(testLinkedFile);
		execute(op);
		assertFalse("File delete failed", testLinkedFile.exists());
		undo();
		assertTrue("File recreation failed", testLinkedFile.exists());
		assertTrue("File content was altered on undo", snap
				.isValid(testLinkedFile.getParent()));
		redo();
		assertFalse("Redo delete failed", testLinkedFile.exists());
	}

	public void testFileAndFolderMoveSameDests() throws ExecutionException,
			CoreException {
		IPath targetPath = targetProject.getFullPath();
		MoveResourcesOperation op = new MoveResourcesOperation(new IResource[] {
				testSubFolder, testFileWithContent }, targetPath,
				"testFileAndFolderMove");
		FolderSnapshot snapFolder = new FolderSnapshot(testSubFolder);
		FileSnapshot snapFile = new FileSnapshot(testFileWithContent);
		execute(op);
		IFolder movedFolder = getWorkspaceRoot().getFolder(
				targetPath.append(TEST_SUBFOLDER_NAME));
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snapFolder
				.isValid(targetProject));
		IFile movedFile = getWorkspaceRoot().getFile(
				targetPath.append(TEST_RANDOMFILE_NAME));
		assertTrue("File move failed", movedFile.exists());
		assertTrue("File content was altered", snapFile.isValid(targetProject));

		undo();
		movedFolder = getWorkspaceRoot().getFolder(
				targetPath.append(TEST_SUBFOLDER_NAME));
		assertFalse("Move undo failed", movedFolder.exists());
		assertTrue("Folder content was altered on undo", snapFolder
				.isValid(testFolder));
		movedFile = getWorkspaceRoot().getFile(
				targetPath.append(TEST_RANDOMFILE_NAME));
		assertFalse("Move undo failed", movedFolder.exists());
		assertTrue("File content was altered on undo", snapFile
				.isValid(testFolder));

		redo();
		movedFolder = getWorkspaceRoot().getFolder(
				targetPath.append(TEST_SUBFOLDER_NAME));
		assertTrue("Folder move failed", movedFolder.exists());
		assertTrue("Folder content was altered", snapFolder
				.isValid(targetProject));
		movedFile = getWorkspaceRoot().getFile(
				targetPath.append(TEST_RANDOMFILE_NAME));
		assertTrue("File move failed", movedFile.exists());
		assertTrue("File content was altered", snapFile.isValid(targetProject));
	}

	public void testFileAndFolderCopyDifferentDests()
			throws ExecutionException, CoreException {
		// copying a file and folder to different destination projects,
		// assigning new names to a new project
		// The folder gets a new name, the file retains the old name
		CopyResourcesOperation op = new CopyResourcesOperation(new IResource[] {
				testSubFolder, testFileWithContent }, new IPath[] {
				testProject.getFullPath().append(TEST_NEWFOLDER_NAME),
				targetProject.getFullPath().append(
						testFileWithContent.getName()) },
				"testFileAndFolderDifferentDests");
		FolderSnapshot snapFolder = new FolderSnapshot(testSubFolder);
		FileSnapshot snapFile = new FileSnapshot(testFileWithContent);
		execute(op);
		IFolder copiedFolder = testProject.getFolder(TEST_NEWFOLDER_NAME);
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snapFolder.isValid(testFolder));
		snapFolder.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder copy does not match", snapFolder
				.isValid(testProject));
		IFile copiedFile = targetProject.getFile(testFileWithContent.getName());
		assertTrue("File copy failed", copiedFile.exists());
		assertTrue("Source file was altered", snapFile.isValid(testFolder));
		assertTrue("File copy does not match", snapFile.isValid(targetProject));

		undo();
		assertFalse("Copy folder undo failed", copiedFolder.exists());
		assertFalse("Copy file undo failed", copiedFile.exists());
		snapFolder.name = testSubFolder.getName();
		assertTrue("Source file was altered during undo", snapFile
				.isValid(testFolder));
		assertTrue("Source folder was altered during undo", snapFolder
				.isValid(testFolder));

		redo();
		assertTrue("Source folder was altered during redo", snapFolder
				.isValid(testFolder));
		snapFolder.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder copy does not match on redo", snapFolder
				.isValid(testProject));
		assertTrue("Source file was altered during redo", snapFile
				.isValid(testFolder));
		assertTrue("File copy does not match on redo", snapFile
				.isValid(targetProject));
	}

	public void testFileAndFolderCopyDifferentNames()
			throws ExecutionException, CoreException {
		// copying a file and folder to a new project, assigning new names to a
		// new project
		CopyResourcesOperation op = new CopyResourcesOperation(new IResource[] {
				testSubFolder, testFileWithContent }, new IPath[] {
				targetProject.getFullPath().append(TEST_NEWFOLDER_NAME),
				targetProject.getFullPath().append(TEST_NEWFILE_NAME) },
				"testFileAndFolderDifferentNames");
		FolderSnapshot snapFolder = new FolderSnapshot(testSubFolder);
		FileSnapshot snapFile = new FileSnapshot(testFileWithContent);
		execute(op);
		IFolder copiedFolder = targetProject.getFolder(TEST_NEWFOLDER_NAME);
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snapFolder.isValid(testFolder));
		snapFolder.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder copy does not match", snapFolder
				.isValid(targetProject));
		IFile copiedFile = targetProject.getFile(TEST_NEWFILE_NAME);
		assertTrue("File copy failed", copiedFile.exists());
		assertTrue("Source file was altered", snapFile.isValid(testFolder));
		snapFile.name = TEST_NEWFILE_NAME;
		assertTrue("File copy does not match", snapFile.isValid(targetProject));

		undo();
		assertFalse("Copy folder undo failed", copiedFolder.exists());
		assertFalse("Copy file undo failed", copiedFile.exists());
		snapFolder.name = testSubFolder.getName();
		assertTrue("Source file was altered during undo", snapFolder
				.isValid(testFolder));
		snapFile.name = testFileWithContent.getName();
		assertTrue("Source folder was altered during undo", snapFile
				.isValid(testFolder));

		redo();
		assertTrue("Source folder was altered during redo", snapFolder
				.isValid(testFolder));
		snapFolder.name = TEST_NEWFOLDER_NAME;
		assertTrue("Folder copy does not match on redo", snapFolder
				.isValid(targetProject));
		assertTrue("Source file was altered during redo", snapFile
				.isValid(testFolder));
		snapFile.name = TEST_NEWFILE_NAME;
		assertTrue("File copy does not match on redo", snapFile
				.isValid(targetProject));
	}

	public void testRedundantFileAndFolderCopy() throws CoreException,
			ExecutionException {
		// copying a file which is a child of a folder, keeping same name to a
		// new project
		CopyResourcesOperation op = new CopyResourcesOperation(new IResource[] {
				testFolder, testFileWithContent }, targetProject.getFullPath(),
				"testRedundantFileAndFolderCopy");
		FolderSnapshot snapFolder = new FolderSnapshot(testFolder);
		FileSnapshot snapFile = new FileSnapshot(testFileWithContent);
		execute(op);
		IFolder copiedFolder = targetProject.getFolder(testFolder.getName());
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snapFolder.isValid(testProject));
		assertTrue("Folder copy does not match", snapFolder
				.isValid(targetProject));
		IFile copiedFile = targetProject.getFile(testFileWithContent.getName());
		assertFalse("Nested file should not have been copied to new location",
				copiedFile.exists());
		copiedFile = testFolder.getFile(testFileWithContent.getName());
		assertTrue("Nested file should have been copied to existing parent",
				copiedFile.exists());
		assertTrue("Source file was altered", snapFile.isValid(testFolder));

		undo();
		assertFalse("Copy folder undo failed", copiedFolder.exists());
		assertTrue("Source file was altered during undo", snapFile
				.isValid(testFolder));
		assertTrue("Source folder was altered during undo", snapFolder
				.isValid(testProject));

		redo();
		assertTrue("Source folder was altered during redo", snapFolder
				.isValid(testProject));
		assertTrue("Folder copy does not match on redo", snapFolder
				.isValid(targetProject));
		assertTrue("Source file was altered during redo", snapFile
				.isValid(testFolder));
	}

	public void testFileAndFolderCopySameDests() throws ExecutionException,
			CoreException {
		// copying a file and folder, keeping same name to a new project
		CopyResourcesOperation op = new CopyResourcesOperation(new IResource[] {
				testSubFolder, testFileWithContent }, targetProject
				.getFullPath(), "testFileAndFolderCopy");
		FolderSnapshot snapFolder = new FolderSnapshot(testSubFolder);
		FileSnapshot snapFile = new FileSnapshot(testFileWithContent);
		execute(op);
		IFolder copiedFolder = targetProject.getFolder(testSubFolder.getName());
		assertTrue("Folder copy failed", copiedFolder.exists());
		assertTrue("Source folder was altered", snapFolder.isValid(testFolder));
		assertTrue("Folder copy does not match", snapFolder
				.isValid(targetProject));
		IFile copiedFile = targetProject.getFile(testFileWithContent.getName());
		assertTrue("File copy failed", copiedFile.exists());
		assertTrue("Source file was altered", snapFile.isValid(testFolder));
		assertTrue("File copy does not match", snapFile.isValid(targetProject));

		undo();
		assertFalse("Copy folder undo failed", copiedFolder.exists());
		assertFalse("Copy file undo failed", copiedFile.exists());
		assertTrue("Source file was altered during undo", snapFile
				.isValid(testFolder));
		assertTrue("Source folder was altered during undo", snapFolder
				.isValid(testFolder));

		redo();
		assertTrue("Source folder was altered during redo", snapFolder
				.isValid(testFolder));
		assertTrue("Folder copy does not match on redo", snapFolder
				.isValid(targetProject));
		assertTrue("Source file was altered during redo", snapFile
				.isValid(testFolder));
		assertTrue("File copy does not match on redo", snapFile
				.isValid(targetProject));
	}

	public void testWorkspaceUndoMonitor() throws ExecutionException,
			CoreException {
		// First we copy the project to the target location
		// This gives us lots of stuff to delete in order to manufacture some
		// workspace changes
		CopyProjectOperation op = new CopyProjectOperation(testProject,
				TEST_NEWPROJECT_NAME, null, "testProjectCopy");
		execute(op);
		// Now we are going to create a new file
		IFile file = getWorkspaceRoot().getFile(
				testProject.getFullPath().append(TEST_NEWFILE_NAME));
		CreateFileOperation op2 = new CreateFileOperation(file, null,
				getContents(getRandomString()), "testFileCreateLeaf");
		execute(op2);
		assertTrue("Operation should be valid", op2.canUndo());
		int changes = 0;
		// back door delete the new file
		file.delete(true, getMonitor());
		changes++;
		// op still doesn't know it's invalid because undo monitor hasn't
		// had changes to force checking it.
		assertTrue("Operation should be valid", op2.canUndo());

		// Now perform a bunch of changes
		emptyTestFile.delete(true, getMonitor());
		changes++;
		testFileInProject.delete(true, getMonitor());
		changes++;
		testFileInSubFolder.delete(true, getMonitor());
		changes++;
		testFileWithContent.delete(true, getMonitor());
		changes++;
		testLinkedFile.delete(true, getMonitor());
		changes++;
		testLinkedFolder.delete(true, getMonitor());
		changes++;
		testSubFolder.delete(true, getMonitor());
		changes++;
		testFolder.delete(true, getMonitor());
		changes++;
		testFolder = testProject.getFolder(TEST_FOLDER_NAME);
		testFolder.create(true, true, getMonitor());
		changes++;
		testFileInProject = testProject.getFile(TEST_FILEINPROJECT_NAME);
		testFileInProject.create(getContents(getRandomString()), true,
				getMonitor());
		changes++;
		testSubFolder = testFolder.getFolder(TEST_SUBFOLDER_NAME);
		testSubFolder.create(true, true, getMonitor());
		changes++;
		emptyTestFile = testFolder.getFile(TEST_EMPTYFILE_NAME);
		emptyTestFile.create(getContents(FILE_CONTENTS_EMPTY), true,
				getMonitor());
		changes++;

		assertTrue("Need to make at least the minimum number of changes",
				changes >= NUM_CHANGES);
		assertFalse("Operation should be invalid", op2.canUndo());
	}

	public void testProjectCopyUndoInvalid() throws ExecutionException,
			CoreException {
		// Create a new copy of a project
		CopyProjectOperation op = new CopyProjectOperation(testProject,
				TEST_NEWPROJECT_NAME, null, "testProjectCopyUndoInvalid");
		execute(op);
		// Now we "back door" delete one of the files in the source project
		emptyTestFile.delete(true, getMonitor());
		// The operation should know that undoing is dangerous
		undoExpectFail(op);
	}

	public void test162655() throws ExecutionException, CoreException {
		DeleteResourcesOperation op = new DeleteResourcesOperation(
				new IResource[] { testProject }, "testProjectDelete", false);
		execute(op);
		assertFalse("Project delete failed", testProject.exists());

		// recreate outside the scope of undo
		testProject = getWorkspace().getRoot().getProject(TEST_PROJECT_NAME);
		testProject.create(getMonitor());
		testProject.open(getMonitor());
		assertTrue("Project creation failed", testProject.exists());

		// Now that project exists again, the undo should fail.
		undoExpectFail(op);
	}
	
	public void test250125() throws ExecutionException {
		IFolder folder = getWorkspaceRoot().getFolder(
				testProject.getFullPath().append(TEST_NEWFOLDER_NAME));
		CreateFolderOperation op = new CreateFolderOperation(folder, null,
				"testFolderCreateLeaf");
		assertFalse("Folder should not exist before test is run", folder
				.exists());
		IFile file = getWorkspaceRoot().getFile(folder.getFullPath().append(TEST_NEWFILE_NAME));
		CreateFileOperation fileOp = new CreateFileOperation(file, null, null, "file operation");
		assertFalse("File should not exist yet", file.exists());
		assertTrue("op state is valid", op.computeExecutionStatus(getMonitor()).isOK());
		assertTrue("op state is valid", fileOp.computeExecutionStatus(getMonitor()).isOK());
		execute(op);
		assertTrue("Folder should exist", folder.exists());
		// Now that the folder is created, the file op which also was to create the container
		// should be invalid
		assertFalse("op state should be invalid", fileOp.computeExecutionStatus(getMonitor()).isOK());
	}
}
