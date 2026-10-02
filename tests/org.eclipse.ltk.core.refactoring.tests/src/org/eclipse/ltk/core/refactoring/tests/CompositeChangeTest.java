/*******************************************************************************
 * Copyright (c) 2026 Carsten Hammer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.tests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.text.Document;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;

public class CompositeChangeTest {

	@Test
	public void testEmptyChanges() {
		assertEquals(0, composite().getFilenumber());
		assertEquals(0, composite(composite()).getFilenumber());
	}

	@Test
	public void testDirectFileChanges() {
		assertEquals(2, composite(textChange("/project/A.txt"), textChange("/project/B.txt")).getFilenumber());
	}

	@Test
	public void testAllSiblingGroupsAreCounted() {
		assertEquals(3, composite(
				composite(textChange("/project/A.txt")),
				composite(textChange("/project/B.txt"), textChange("/project/C.txt"))).getFilenumber());
		assertEquals(3, composite(
				composite(textChange("/project/B.txt"), textChange("/project/C.txt")),
				composite(textChange("/project/A.txt"))).getFilenumber());
	}

	@Test
	public void testMixedAndDeeplyNestedChanges() {
		CompositeChange nested= composite(composite(composite(textChange("/project/B.txt"))));
		nested.markAsSynthetic();
		assertEquals(3, composite(textChange("/project/A.txt"), nested,
				composite(), textChange("/project/C.txt")).getFilenumber());
	}

	@Test
	public void testSeveralChangesToTheSameFileAreCountedOnce() {
		assertEquals(1, composite(composite(textChange("/project/A.txt"), textChange("/project/A.txt")),
				textChange("/project/A.txt")).getFilenumber());
	}

	@Test
	public void testEqualNamesInDifferentProjectsAreDifferentFiles() {
		assertEquals(2, composite(textChange("/one/A.txt"), textChange("/two/A.txt")).getFilenumber());
	}

	@Test
	public void testOpaqueChangeCanAffectSeveralFiles() {
		IFile first= file("/project/A.txt");
		IFile second= file("/project/B.txt");
		Change atomic= change(null, first, second, first);
		TextFileChange duplicate= textChange("/project/B.txt");
		CompositeChange root= composite(atomic, duplicate);
		assertEquals(2, root.getFilenumber());
		assertArrayEquals(new Change[] { atomic, duplicate }, root.getChildren());
		assertSame(root, atomic.getParent());
		assertTrue(atomic.isEnabled());
	}

	@Test
	public void testUnknownAffectedObjectsDoNotHideSiblingFiles() {
		assertEquals(2, composite(change(null, (Object[]) null), textChange("/project/A.txt"),
				change(file("/project/B.txt"), (Object[]) null)).getFilenumber());
	}

	@Test
	public void testResourceAdaptableElements() {
		IFile first= file("/project/A.txt");
		IAdaptable element= adaptable(first, IResource.class);
		assertEquals(2, composite(change(element, (Object[]) null),
				change(null, element, adaptable(file("/project/B.txt"), IResource.class))).getFilenumber());
	}

	@Test
	public void testFileAdaptableElements() {
		IAdaptable element= adaptable(file("/project/A.txt"), IFile.class);
		assertEquals(1, composite(change(null, element), change(element, (Object[]) null)).getFilenumber());
	}

	@Test
	public void testNonFileElementsAreNotCountedAsFiles() {
		var project= ResourcesPlugin.getWorkspace().getRoot().getProject("project");
		assertEquals(0, composite(new NullChange(), change(null, project, project.getFolder("folder"),
				new Document("text"), null), change(null, (Object[]) null)).getFilenumber());
	}

	@Test
	public void testExplicitlyEmptyAffectedObjects() {
		assertEquals(0, composite(change(file("/project/A.txt"))).getFilenumber());
	}

	@Test
	public void testCountDoesNotChangeSelectionOrTree() {
		TextFileChange first= textChange("/project/A.txt");
		TextFileChange second= textChange("/project/B.txt");
		CompositeChange nested= composite(second);
		nested.setEnabled(false);
		CompositeChange root= composite(first, nested);
		Change[] originalChildren= root.getChildren();
		assertEquals(2, root.getFilenumber());
		assertEquals(2, root.getFilenumber());
		assertArrayEquals(originalChildren, root.getChildren());
		assertSame(root, first.getParent());
		assertSame(nested, second.getParent());
		assertTrue(first.isEnabled());
		assertFalse(nested.isEnabled());
		assertFalse(second.isEnabled());
	}

	private static CompositeChange composite(Change... children) {
		return new CompositeChange("changes", children);
	}

	private static IFile file(String path) {
		// Handles suffice: counting must not read or change workspace contents.
		return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
	}

	private static TextFileChange textChange(String path) {
		IFile file= file(path);
		return new TextFileChange(file.getName(), file);
	}

	private static IAdaptable adaptable(IFile file, Class<? extends IResource> type) {
		return new IAdaptable() {
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				return adapter == type ? adapter.cast(file) : null;
			}
		};
	}

	private static Change change(Object modified, Object... affected) {
		return new Change() {
			@Override
			public String getName() {
				return "test change";
			}

			@Override
			public Object getModifiedElement() {
				return modified;
			}

			@Override
			public Object[] getAffectedObjects() {
				return affected;
			}

			@Override
			public void initializeValidationData(IProgressMonitor pm) {
				throw new AssertionError("Counting must not initialize a change");
			}

			@Override
			public RefactoringStatus isValid(IProgressMonitor pm) {
				throw new AssertionError("Counting must not validate a change");
			}

			@Override
			public Change perform(IProgressMonitor pm) {
				throw new AssertionError("Counting must not perform a change");
			}
		};
	}
}
