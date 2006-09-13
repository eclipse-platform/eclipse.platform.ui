/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;

import junit.framework.TestCase;

/**
 * Tests the undo of various workspace operations.
 * 
 * @since 3.3
 */
public class WorkspaceOperationsTests extends TestCase {

	IProject testProject;

	IFolder testFolder;

	IFile testFile1, testFile2, testFile3;

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

	static String CUSTOM_TYPE = "TestMarkerType";

	public WorkspaceOperationsTests() {
		super();
	}

	/**
	 * @param testName
	 */
	public WorkspaceOperationsTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		testProject = getWorkspace().getRoot().getProject("UndoTestProject");
		testProject.create(getMonitor());
		testProject.open(getMonitor());
		assertTrue(testProject.exists());
		testFolder = testProject.getFolder("UndoTestFolder");
		testFolder.create(true, true, getMonitor());
		assertTrue(testFolder.exists());
		testFile1 = testFolder.getFile("UndoTestFile1.txt");
		testFile1.create(getContents("Test File Content"), true, getMonitor());
		testFile2 = testFolder.getFile("UndoTestFile2.txt");
		testFile2.create(getContents("Test File Content"), true, getMonitor());
		testFile3 = testFolder.getFile("UndoTestFile3.txt");
		testFile3.create(getContents("Test File Content"), true, getMonitor());

		history = PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
		context = PlatformUI.getWorkbench().getOperationSupport()
				.getUndoContext();

	}

	protected void tearDown() throws Exception {
		testFile1.delete(true, getMonitor());
		testFile2.delete(true, getMonitor());
		testFile3.delete(true, getMonitor());
		testFolder.delete(true, getMonitor());
		testProject.close(getMonitor());
		testProject.delete(true, true, getMonitor());
	}

	private IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private IProgressMonitor getMonitor() {
		return null;
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

	private void execute(IUndoableOperation operation)
			throws ExecutionException {
		assertTrue("Operation can be executed", operation.canExecute());
		assertTrue("Execution should be OK status", history.execute(operation,
				getMonitor(), null).equals(Status.OK_STATUS));
	}

	private void undo() throws ExecutionException {
		assertTrue("Operation can be undone", history.canUndo(context));
		assertTrue("Undo should be OK status", history.undo(context,
				getMonitor(), null).equals(Status.OK_STATUS));
	}

	private void redo() throws ExecutionException {
		assertTrue("Operation can be redone", history.canRedo(context));
		assertTrue("Redo should be OK status", history.redo(context,
				getMonitor(), null).equals(Status.OK_STATUS));
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
				IMarker.BOOKMARK, getInitialMarkerAttributes(), testFile1,
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
				attrs, new IFile[] { testFile1, testFile2, testFile3 },
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
				new IFile[] { testFile1, testFile2, testFile3 },
				"Create Multiple Marker Types Test");
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
				IMarker.BOOKMARK, getInitialMarkerAttributes(), testFile1,
				"Create Marker Test");
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
				new IFile[] { testFile1, testFile2, testFile3 },
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
				IMarker.BOOKMARK, getInitialMarkerAttributes(), testFile1,
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
				new IFile[] { testFile1, testFile2, testFile3 },
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
				new IFile[] { testFile1, testFile2, testFile3 },
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
				new IFile[] { testFile1, testFile2, testFile3 },
				"Create Multiple Markers Same Type Test");
		execute(op);
		IMarker[] markers = op.getMarkers();
		markers[1].delete();
		// Must compute status first because we don't perform expensive
		// validations in canUndo().  However we should remember the validity
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
				new IFile[] { testFile1, testFile2, testFile3 },
				"Create Multiple Markers Same Type Test");
		execute(op);
		testFile1.delete(true, getMonitor());
		// Must compute status first because we don't perform expensive
		// validations in canUndo().  However we should remember the validity
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
				new IFile[] { testFile1, testFile2, testFile3 },
				"Create Multiple Markers Same Type Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers(), getUpdatedMarkerAttributes(),
				"Update and Merge Multiple Markers", true);
		execute(updateOp);
		IMarker[] markers = updateOp.getMarkers();
		markers[0].delete();
		// Must compute status first because we don't perform expensive
		// validations in canUndo().  However we should remember the validity
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
				new IFile[] { testFile1, testFile2, testFile3 },
				"Create Multiple Markers Same Type Test");
		execute(op);
		UpdateMarkersOperation updateOp = new UpdateMarkersOperation(op
				.getMarkers(), getUpdatedMarkerAttributes(),
				"Update and Merge Multiple Markers", true);
		execute(updateOp);
		testFile3.delete(true, getMonitor());
		// Must compute status first because we don't perform expensive
		// validations in canUndo().  However we should remember the validity
		// once we've computed the status.
		updateOp.computeUndoableStatus(null);

		assertFalse("Undo should be invalid, marker no longer exists", updateOp
				.canUndo());
	}
}
