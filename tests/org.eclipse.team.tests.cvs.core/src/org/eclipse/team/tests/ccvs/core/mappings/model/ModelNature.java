/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.mappings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;

/**
 * Nature used to identify a model project
 */
public class ModelNature implements IProjectNature {

	public static final String NATURE_ID = "org.eclipse.team.tests.cvs.core.bug302163_ModelNature";

	private IProject project;

	public void configure() {
		// Nothing to do
	}

	public void deconfigure() {
		// Nothing to do
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
