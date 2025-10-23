/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
package org.eclipse.search.tests.filesearch;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.search.tests.ResourceHelper;

public class JUnitSourceSetup implements BeforeAllCallback, AfterAllCallback {

	public static final String STANDARD_PROJECT_NAME= "JUnitSource";

	private IProject fProject= null;
	private final String fProjectName;

	public IProject getStandardProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(STANDARD_PROJECT_NAME);
	}

	public JUnitSourceSetup() {
		this(STANDARD_PROJECT_NAME);
	}

	public JUnitSourceSetup(String projectName) {
		fProjectName= projectName;
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(fProjectName);
		if (!project.exists()) { // allow nesting of JUnitSetups
			fProject= ResourceHelper.createJUnitSourceProject(fProjectName);
		}
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		if (fProject != null) { // delete only by the setup who created the project
			ResourceHelper.deleteProject(fProjectName);
			fProject= null;
		}
	}

}
