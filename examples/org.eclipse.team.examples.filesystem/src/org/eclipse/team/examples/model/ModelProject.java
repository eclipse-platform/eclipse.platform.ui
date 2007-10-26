/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ModelProject extends ModelContainer {

	public static boolean isModProject(IProject project) throws CoreException {
		if (! project.isOpen())
			return false;
		IProjectDescription description = project.getDescription();
		return description.hasNature(ModelNature.NATURE_ID);
	}
	
	public static void makeModProject(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natureIds = description.getNatureIds();
		List result = new ArrayList();
		for (int i = 0; i < natureIds.length; i++) {
			result.add(natureIds[i]);
		}
		result.add(ModelNature.NATURE_ID);
		description.setNatureIds((String[]) result.toArray(new String[result.size()]));
		project.setDescription(description, monitor);
	}
	
	public ModelProject(IProject project) {
		super(project);
	}

}
