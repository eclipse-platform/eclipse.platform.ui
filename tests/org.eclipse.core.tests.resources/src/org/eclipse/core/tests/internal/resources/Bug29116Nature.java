/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.resources;

import java.util.HashMap;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.internal.builders.SortBuilder;

/**
 * Nature for testing regression of bug 29116.
 */
public class Bug29116Nature extends TestNature {
	/**
	 * Constructor for Bug29116Nature.
	 */
	public Bug29116Nature() {
		super();
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		//install the snow builder
		IProject project = getProject();
		IProjectDescription desc = project.getDescription();
		ICommand[] oldSpec = desc.getBuildSpec();
		ICommand[] newSpec = new ICommand[oldSpec.length + 1];
		System.arraycopy(oldSpec, 0, newSpec, 0, oldSpec.length);
		ICommand newCommand = desc.newCommand();
		newCommand.setBuilderName(SortBuilder.BUILDER_NAME);
		HashMap args = new HashMap(20);
		newCommand.setArguments(args);
		newSpec[oldSpec.length] = newCommand;
		desc.setBuildSpec(newSpec);
		project.setDescription(desc, IResource.FORCE | IResource.KEEP_HISTORY, null);

		//run the builder (this should cause the error)
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, SortBuilder.BUILDER_NAME, args, null);
	}

	/**
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		super.setProject(project);
	}

}
