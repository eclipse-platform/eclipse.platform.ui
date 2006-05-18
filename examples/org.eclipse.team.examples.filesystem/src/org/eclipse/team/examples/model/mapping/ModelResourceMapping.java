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
package org.eclipse.team.examples.model.mapping;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.team.examples.model.*;

public abstract class ModelResourceMapping extends ResourceMapping {

	private final ModelObject object;

	public static ResourceMapping create(ModelObject object) {
		if (object instanceof ModelContainer) {
			return new ModelContainerResourceMapping((ModelContainer) object);
		}
		if (object instanceof ModelObjectDefinitionFile) {
			return new ModResourceMapping((ModelObjectDefinitionFile) object);
		}
		if (object instanceof ModelObjectElementFile) {
			return new MoeResourceMapping((ModelObjectElementFile) object);
		}
		return null;
	}
	
	protected ModelResourceMapping(ModelObject object) {
		this.object = object;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getModelObject()
	 */
	public Object getModelObject() {
		return object;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getModelProviderId()
	 */
	public String getModelProviderId() {
		return ExampleModelProvider.ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.mapping.ResourceMapping#getProjects()
	 */
	public IProject[] getProjects() {
		return new IProject[] { (IProject)object.getProject().getResource() };
	}

}
