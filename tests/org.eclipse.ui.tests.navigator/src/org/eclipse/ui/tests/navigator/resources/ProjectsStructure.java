/*******************************************************************************
 * Copyright (c) 2023 ArSysOp
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nikifor Fedorov (ArSysOp) - Initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

abstract class ProjectsStructure {

	abstract void create(String first, String second, String third) throws CoreException;

	final void clear() throws CoreException {
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			project.delete(true, true, new NullProgressMonitor());
		}
	}

	protected final IProject createInner(String name, IProject parent) throws CoreException {
		IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(name);
		description.setLocation(parent.getLocation().append(name));
		return createAndOpen(description);
	}

	protected final IProject createAndOpen(IProjectDescription description) throws CoreException {
		IProject handle = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
		handle.create(description, new NullProgressMonitor());
		handle.open(new NullProgressMonitor());
		return handle;
	}

	protected final void deleteLeavingContents(String name) throws CoreException {
		ResourcesPlugin.getWorkspace().getRoot().getProject(name).delete(false, true, new NullProgressMonitor());
	}

	static final class Imported extends ProjectsStructure {

		@Override
		void create(String first, String second, String third) throws CoreException {
			IProject base = createAndOpen(ResourcesPlugin.getWorkspace().newProjectDescription(first));
			createInner(second, base);
			createInner(third, base);
		}

	}

	static final class NotImported extends ProjectsStructure {

		@Override
		void create(String first, String second, String third) throws CoreException {
			new Imported().create(first, second, third);
			deleteLeavingContents(second);
			deleteLeavingContents(third);
		}

	}

}
