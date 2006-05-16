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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public abstract class ModelContainer extends ModelResource {

	protected ModelContainer(IContainer container) {
		super(container);
	}
	
	protected IContainer getContainer() {
		return (IContainer)getResource();
	}
	
	public ModelObject[] getChildren() throws CoreException {
		IResource[] members = getContainer().members();
		List result = new ArrayList();
		for (int i = 0; i < members.length; i++) {
			IResource resource = members[i];
			if (resource instanceof IFolder) {
				result.add(new ModelFolder((IFolder) resource));
			} else if (ModelObjectDefinitionFile.isModFile(resource)) {
				result.add(new ModelObjectDefinitionFile((IFile)resource));
			} else if (resource instanceof IProject && ModelProject.isModProject((IProject) resource)) {
				result.add(new ModelProject((IProject) resource));
			}
		}
		return (ModelObject[]) result.toArray(new ModelObject[result.size()]);
	}

}
