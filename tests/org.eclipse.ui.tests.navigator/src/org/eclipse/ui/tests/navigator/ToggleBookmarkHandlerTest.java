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
import static org.eclipse.ui.tests.navigator.BookmarkTestSupport.event;
import static org.eclipse.ui.tests.navigator.BookmarkTestSupport.execute;
import static org.eclipse.ui.tests.navigator.BookmarkTestSupport.toggle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.internal.navigator.resources.actions.ToggleBookmarkHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests how {@link ToggleBookmarkHandler} reads the selection of the execution
 * event and which elements it acts on.
 */
public class ToggleBookmarkHandlerTest {

	private IProject project;
	private IFile file;
	private IFile otherFile;

	@BeforeEach
	public void setUp() throws CoreException {
		project = BookmarkTestSupport.createProject("ToggleBookmarkHandlerTest_");
		file = BookmarkTestSupport.createFile(project, "a.txt");
		otherFile = BookmarkTestSupport.createFile(project, "b.txt");
	}

	@AfterEach
	public void tearDown() throws CoreException {
		BookmarkTestSupport.deleteProjects(project);
	}

	@Test
	public void testMenuSelectionIsToggled() throws Exception {
		toggle(file);

		assertEquals(1, bookmarkCount(file));
		assertEquals(0, bookmarkCount(otherFile));
	}

	@Test
	public void testExecuteReturnsNull() {
		assertNull(toggle(file));
	}

	@Test
	public void testFallsBackToCurrentSelectionWithoutMenuSelection() throws Exception {
		execute(event(null, new StructuredSelection(file)));

		assertEquals(1, bookmarkCount(file));
	}

	@Test
	public void testMenuSelectionWinsOverCurrentSelection() throws Exception {
		execute(event(new StructuredSelection(file), new StructuredSelection(otherFile)));

		assertEquals(1, bookmarkCount(file));
		assertEquals(0, bookmarkCount(otherFile));
	}

	@Test
	public void testNoSelectionDoesNothing() throws Exception {
		assertNull(execute(event(null, null)));

		assertEquals(0, bookmarkCount(file));
		assertEquals(0, bookmarkCount(project));
	}

	@Test
	public void testEmptySelectionDoesNothing() throws Exception {
		toggle();

		assertEquals(0, bookmarkCount(file));
	}

	@Test
	public void testNonStructuredMenuSelectionIsNotReplacedByCurrentSelection() throws Exception {
		ISelection nonStructured = () -> false;

		execute(event(nonStructured, new StructuredSelection(file)));

		assertEquals(0, bookmarkCount(file));
	}

	@Test
	public void testNonResourceElementsAreIgnored() throws Exception {
		toggle(new Object(), "text", file);

		assertEquals(1, bookmarkCount(file));
	}

	@Test
	public void testElementAdaptingToResourceIsToggled() throws Exception {
		IAdaptable adaptable = new IAdaptable() {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				return adapter == IResource.class ? adapter.cast(file) : null;
			}
		};

		toggle(adaptable);

		assertEquals(1, bookmarkCount(file));
	}

	@Test
	public void testResourceOfClosedProjectIsSkipped() throws Exception {
		project.close(null);

		toggle(file, project);

		project.open(null);
		assertEquals(0, bookmarkCount(file));
		assertEquals(0, bookmarkCount(project));
	}

	@Test
	public void testDeletedResourceIsSkippedAndOthersAreStillToggled() throws Exception {
		file.delete(true, null);

		toggle(file, otherFile);

		assertFalse(file.exists());
		assertEquals(1, bookmarkCount(otherFile));
	}
}
