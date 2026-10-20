/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.ide;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.tests.harness.util.UITestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OpenResourceAction}.
 */
public class OpenResourceActionTest {

	private final List<IProject> projects = new ArrayList<>();

	@AfterEach
	public void deleteProjects() throws Exception {
		for (IProject project : projects) {
			project.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
		}
		projects.clear();
	}

	/**
	 * A project whose .project file is gone must not prevent the other selected
	 * projects from being opened.
	 */
	@Test
	public void testOpenSkipsProjectWithMissingDescription() throws Exception {
		IProject broken = createClosedProject("openResourceActionTest_broken");
		IProject healthy = createClosedProject("openResourceActionTest_healthy");
		Files.delete(broken.getLocation().append(IProjectDescription.DESCRIPTION_FILE_NAME).toFile().toPath());

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		OpenResourceAction action = new OpenResourceAction(() -> shell);
		action.selectionChanged(new StructuredSelection(new Object[] { broken, healthy }));
		action.run();
		UITestUtil.processEventsUntil(healthy::isOpen, 30000);

		assertFalse(broken.isOpen(), "Project with missing .project file should stay closed");
		assertTrue(healthy.isOpen(), "Remaining project should have been opened");
	}

	private IProject createClosedProject(String name) throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		project.create(null);
		project.open(null);
		project.close(null);
		projects.add(project);
		return project;
	}
}
