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
	}

	protected void tearDown() throws Exception {
		TestModelProvider.enabled = false;
		super.tearDown();
	}

	public void testSimpleChanges() {
		IProject project = getWorkspace().getRoot().getProject("Project");
		IResource[] before = buildResources(project, new String[] {"c/", "c/b/", "c/x", "c/b/y", "c/b/z"});
		ensureExistsInWorkspace(before, true);
		assertExistsInWorkspace(before);

		// A project close
		IResourceChangeDescriptionFactory factory = createEmptyChangeDescription();
		factory.close(project);
		IStatus status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.CLOSED, project)});

		// A project deletion
		factory = createEmptyChangeDescription();
		factory.delete(project);
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project)});

		// Some file deletions
		factory = createEmptyChangeDescription();
		factory.delete(project.findMember("c/x"));
		factory.delete(project.findMember("c/b/y"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project.findMember("c/b/y"))});

		// A folder deletion
		factory = createEmptyChangeDescription();
		factory.delete(project.findMember("c/b/"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.REMOVED, project.findMember("c/b")),});

		// A project move
		factory = createEmptyChangeDescription();
		factory.move(project, new Path("MovedProject"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, project)});

		// some file moves
		factory = createEmptyChangeDescription();
		factory.move(project.findMember("c/x"), new Path("c/x2"));
		factory.move(project.findMember("c/b/y"), new Path("c/y"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.MOVED, project.findMember("c/b/y"))});

		// A folder move
		factory = createEmptyChangeDescription();
		factory.move(project.findMember("c/b/"), new Path("c/d"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.MOVED, project.findMember("c/b/")),});

		// A project copy
		factory = createEmptyChangeDescription();
		factory.copy(project, new Path("MovedProject"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, project)});

		// some file copy
		factory = createEmptyChangeDescription();
		factory.copy(project.findMember("c/x"), new Path("c/x2"));
		factory.copy(project.findMember("c/b/y"), new Path("c/y"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.COPIED, project.findMember("c/b/y"))});

		// A folder copy
		factory = createEmptyChangeDescription();
		factory.copy(project.findMember("c/b/"), new Path("c/d"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.COPIED, project.findMember("c/b/")),});

		// some file changes
		factory = createEmptyChangeDescription();
		factory.change((IFile) project.findMember("c/x"));
		factory.change((IFile) project.findMember("c/b/y"));
		status = validateChange(factory);
		assertStatusEqual(status, new String[] {ChangeDescription.getMessageFor(ChangeDescription.CHANGED, project.findMember("c/x")), ChangeDescription.getMessageFor(ChangeDescription.CHANGED, project.findMember("c/b/y"))});
	}

	private IStatus validateChange(IResourceChangeDescriptionFactory factory) {
		return ResourceChangeValidator.getValidator().validateChange(factory.getDelta(), getMonitor());
	}

}
