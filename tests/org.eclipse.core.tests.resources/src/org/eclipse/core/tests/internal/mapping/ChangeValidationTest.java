/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.mapping;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests for change validation
 */
public class ChangeValidationTest extends ResourceTest {
	private IResourceChangeDescriptionFactory factory;
	private IProject project;

	public static Test suite() {
		return new TestSuite(ChangeValidationTest.class);
	}

	private void assertStatusEqual(IStatus status, String[] expectedMessages) {
		List actualMessages = new ArrayList();
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				String message = getModelMessage(children[i]);
				if (message != null)
					actualMessages.add(message);
			}
		} else {
			String message = getModelMessage(status);
			if (message != null)
				actualMessages.add(message);
		}
		if (expectedMessages.length < actualMessages.size()) {
			for (Iterator iter = actualMessages.iterator(); iter.hasNext();) {
				String actual = (String) iter.next();
				boolean found = false;
				for (int i = 0; i < expectedMessages.length; i++) {
					String expected = expectedMessages[i];
					if (actual.equals(expected))
						found = true;
					break;
				}
				if (!found)
					fail("Unexpected message returned: " + actual);
			}
		} else {
			for (int i = 0; i < expectedMessages.length; i++) {
				String string = expectedMessages[i];
				if (!actualMessages.contains(string)) {
					fail("Expect message missing: " + string);
				}
			}
		}
	}

	private IResourceChangeDescriptionFactory createEmptyChangeDescription() {
		return ResourceChangeValidator.getValidator().createDeltaFactory();
	}

	/*
	 * Only return the message of the status if it
	 * came from our test model provider
	 */
	private String getModelMessage(IStatus status) {
		if (status instanceof ModelStatus) {
			ModelStatus ms = (ModelStatus) status;
			String id = ms.getModelProviderId();
			if (id.equals(TestModelProvider.ID))
				return status.getMessage();
		}
		return null;
	}

	protected void setUp() throws Exception {
		TestModelProvider.enabled = true;
		super.setUp();
		project = getWorkspace().getRoot().getProject("Project");
		IResource[] before = buildResources(project, new String[] {"c/", "c/b/", "c/a/", "c/x", "c/b/y", "c/b/z"});
		ensureExistsInWorkspace(before, true);
		assertExistsInWorkspace(before);
		factory = createEmptyChangeDescription();
	}

	protected void tearDown() throws Exception {
		TestModelProvider.enabled = false;
		super.tearDown();
	}

	public void testCopyReplaceDeletedFolder() {
		// Copy folder to replace a deleted folder
		final IResource folder = project.findMember("c/b/");
		IFolder destination = project.getFolder("/c/a/");
		factory.delete(destination);
		factory.copy(folder, destination.getFullPath());
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, folder),});
	}

	public void testFileChanges() {
		factory.change((IFile) project.findMember("c/x"));
		factory.change((IFile) project.findMember("c/b/y"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.CHANGED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.CHANGED, project.findMember("c/b/y"))});
	}

	public void testFileCopy() {
		factory.copy(project.findMember("c/x"), new Path("c/x2"));
		factory.copy(project.findMember("c/b/y"), new Path("c/y"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.COPIED, project.findMember("c/b/y"))});
	}

	public void testFileCreate() {
		IFile file = project.getFile("file");
		factory.create(file);
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.ADDED, file)});
	}

	public void testFileInFolderCreate() {
		IFolder folder = project.getFolder("folder");
		IFile file = folder.getFile("file");
		factory.create(folder);
		factory.create(file);
		IStatus status = validateChange(factory);
		//this isn't very accurate, but ChangeDescription doesn't currently record recursive creates
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.ADDED, folder)});

	}

	public void testFileDeletion() {
		factory.delete(project.findMember("c/x"));
		factory.delete(project.findMember("c/b/y"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project.findMember("c/b/y"))});
	}

	public void testFileMoves() {
		factory.move(project.findMember("c/x"), new Path("c/x2"));
		factory.move(project.findMember("c/b/y"), new Path("c/y"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.MOVED, project.findMember("c/b/y"))});
	}

	public void testFolderCopy() {
		final IResource folder = project.findMember("c/b/");
		factory.copy(folder, new Path("c/d"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, folder),});
	}

	public void testFolderDeletion() {
		final IResource folder = project.findMember("c/b/");
		factory.delete(folder);
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project.findMember("c/b")),});
	}

	public void testFolderMove() {
		final IResource folder = project.findMember("c/b/");
		factory.move(folder, new Path("c/d"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, folder),});
	}

	public void testMoveReplaceDeletedFolder() {
		// Move to replace a deleted folder
		final IResource folder = project.findMember("c/b/");
		IFolder destination = project.getFolder("/c/a/");
		factory.delete(destination);
		factory.move(folder, destination.getFullPath());
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, folder),});
	}

	public void testProjectClose() {
		factory.close(project);
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.CLOSED, project)});
	}

	public void testProjectCopy() {
		// A project copy
		factory.copy(project, new Path("MovedProject"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, project)});
	}

	public void testProjectDeletion() {
		// A project deletion
		factory.delete(project);
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project)});
	}

	public void testProjectMove() {
		factory.move(project, new Path("MovedProject"));
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, project)});
	}

	private IStatus validateChange(IResourceChangeDescriptionFactory factory) {
		return ResourceChangeValidator.getValidator().validateChange(factory.getDelta(), getMonitor());
	}

}
