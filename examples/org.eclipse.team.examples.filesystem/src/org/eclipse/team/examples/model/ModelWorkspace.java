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

import org.eclipse.core.resources.ResourcesPlugin;

public class ModelWorkspace extends ModelContainer {

	protected ModelWorkspace() {
		super(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	public String getName() {
		return "Model Root";
	}
	
	public ModelObject getParent() {
		return null;
	}

}
