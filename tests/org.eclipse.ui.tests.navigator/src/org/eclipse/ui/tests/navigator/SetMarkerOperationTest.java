/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.tests.navigator;

import static org.eclipse.ui.tests.navigator.BookmarkTestSupport.bookmarkCount;
import static org.eclipse.ui.tests.navigator.BookmarkTestSupport.toggle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.internal.navigator.resources.ResourceBookmarks;
import org.eclipse.ui.internal.navigator.resources.actions.SetMarkerOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SetMarkerOperation}, both directly and through the marker
 * changes that the toggle command applies with it.
 */
public class SetMarkerOperationTest {

	private IProject project;
	private IProject otherProject;
	private IFolder folder;
	private IFile file;
	private IFile otherFile;

	@BeforeEach
	public void setUp() throws CoreException {
		project = BookmarkTestSupport.createProject("SetMarkerOperationTest_");
		otherProject = BookmarkTestSupport.createProject("SetMarkerOperationTest_other_");
		folder = BookmarkTestSupport.createFolder(project, "folder");
		file = BookmarkTestSupport.createFile(project, "a.txt");
		otherFile = BookmarkTestSupport.createFile(project, "b.txt");
	}

	@AfterEach
	public void tearDown() throws CoreException {
		BookmarkTestSupport.deleteProjects(project, otherProject);
	}

	@Test
	public void testAddedMarkerHasTypeAndMessageOfResourceName() throws Exception {
		toggle(file);

		IMarker[] markers = file.findMarkers(ResourceBookmarks.MARKER_TYPE, false, IResource.DEPTH_ZERO);
		assertEquals(1, markers.length);
		assertEquals(file.getName(), markers[0].getAttribute(IMarker.MESSAGE));
	}

	@Test
	public void testMarkersAreAddedToFilesFoldersAndProjects() throws Exception {
		toggle(file, folder, project);

		assertEquals(1, bookmarkCount(file));
		assertEquals(1, bookmarkCount(folder));
		assertEquals(1, bookmarkCount(project));
	}

	@Test
	public void testRemoveDeletesAllMarkersOfTheResource() throws Exception {
		file.createMarker(ResourceBookmarks.MARKER_TYPE);
		file.createMarker(ResourceBookmarks.MARKER_TYPE);
		assertEquals(2, bookmarkCount(file));

		toggle(file);

		assertEquals(0, bookmarkCount(file));
	}

	@Test
	public void testRepeatedToggleDoesNotAccumulateMarkers() throws Exception {
		for (int i = 0; i < 4; i++) {
			toggle(file);
			assertEquals((i + 1) % 2, bookmarkCount(file), "after toggle " + (i + 1));
		}
	}

	@Test
	public void testMixedSelectionAddsAndRemovesInOneRun() throws Exception {
		otherFile.createMarker(ResourceBookmarks.MARKER_TYPE);

		toggle(file, otherFile);

		assertEquals(1, bookmarkCount(file));
		assertEquals(0, bookmarkCount(otherFile));
	}

	@Test
	public void testResourcesOutsideTheSelectionAreUntouched() throws Exception {
		otherFile.createMarker(ResourceBookmarks.MARKER_TYPE);

		toggle(file);

		assertEquals(1, bookmarkCount(file));
		assertEquals(1, bookmarkCount(otherFile));
		assertEquals(0, bookmarkCount(folder));
		assertEquals(0, bookmarkCount(project));
	}

	@Test
	public void testSelectionAcrossProjects() throws Exception {
		IFile fileInOtherProject = BookmarkTestSupport.createFile(otherProject, "c.txt");
		fileInOtherProject.createMarker(ResourceBookmarks.MARKER_TYPE);

		toggle(file, fileInOtherProject, otherProject);

		assertEquals(1, bookmarkCount(file));
		assertEquals(0, bookmarkCount(fileInOtherProject));
		assertEquals(1, bookmarkCount(otherProject));
	}

	@Test
	public void testOtherMarkerTypesAreKept() throws Exception {
		file.createMarker(IMarker.BOOKMARK);
		file.createMarker(IMarker.PROBLEM);

		toggle(file);
		assertEquals(1, bookmarkCount(file));
		assertEquals(1, file.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO).length);
		assertEquals(1, file.findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO).length);

		toggle(file);
		assertEquals(0, bookmarkCount(file));
		assertEquals(1, file.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO).length);
		assertEquals(1, file.findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO).length);
	}

	@Test
	public void testChildMarkersAreNotAffectedByTogglingTheParent() throws Exception {
		file.createMarker(ResourceBookmarks.MARKER_TYPE);

		toggle(project);

		assertEquals(1, bookmarkCount(project));
		assertEquals(1, bookmarkCount(file));
	}

	@Test
	public void testDirectRunDoesNotAddSecondMarkerToBookmarkedResource() throws Exception {
		file.createMarker(ResourceBookmarks.MARKER_TYPE);

		new SetMarkerOperation(List.of(file), List.of()).run(null);

		assertEquals(1, bookmarkCount(file));
	}

	@Test
	public void testDirectRunAddsMarkerWithMessage() throws Exception {
		new SetMarkerOperation(List.of(file), List.of()).run(null);

		IMarker[] markers = file.findMarkers(ResourceBookmarks.MARKER_TYPE, false, IResource.DEPTH_ZERO);
		assertEquals(1, markers.length);
		assertEquals(file.getName(), markers[0].getAttribute(IMarker.MESSAGE));
	}

	@Test
	public void testDirectRunRemovesMarkersOfResourceWithoutMarkers() throws Exception {
		new SetMarkerOperation(List.of(), List.of(file)).run(null);

		assertEquals(0, bookmarkCount(file));
	}

	@Test
	public void testResourceInBothListsEndsUpWithOneMarker() throws Exception {
		// removals run before additions
		file.createMarker(ResourceBookmarks.MARKER_TYPE);
		otherFile.createMarker(ResourceBookmarks.MARKER_TYPE);
		otherFile.createMarker(ResourceBookmarks.MARKER_TYPE);

		new SetMarkerOperation(List.of(file, otherFile), List.of(file, otherFile)).run(null);

		assertEquals(1, bookmarkCount(file));
		assertEquals(1, bookmarkCount(otherFile));
	}

	@Test
	public void testIsEmpty() {
		assertTrue(new SetMarkerOperation(List.of(), List.of()).isEmpty());
		assertFalse(new SetMarkerOperation(List.of(file), List.of()).isEmpty());
		assertFalse(new SetMarkerOperation(List.of(), List.of(file)).isEmpty());
		assertFalse(new SetMarkerOperation(List.of(file), List.of(otherFile)).isEmpty());
	}

	@Test
	public void testRuleOfEmptyOperationIsNull() {
		assertNull(new SetMarkerOperation(List.of(), List.of()).getRule());
	}

	@Test
	public void testRuleCoversMarkerRulesOfAllResources() {
		IFile fileInOtherProject = otherProject.getFile("c.txt");
		SetMarkerOperation operation = new SetMarkerOperation(List.of(file), List.of(fileInOtherProject));

		ISchedulingRule rule = operation.getRule();

		for (IResource resource : List.of(file, fileInOtherProject)) {
			ISchedulingRule markerRule = resource.getWorkspace().getRuleFactory().markerRule(resource);
			if (markerRule != null) {
				assertNotNull(rule, "rule missing for " + resource);
				assertTrue(rule.contains(markerRule), "rule does not cover " + resource);
			}
		}
	}
}
