/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import org.eclipse.jface.examples.databinding.ModelObject;

public class Lodging extends ModelObject {

	private String name;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String string) {
		Object oldValue = description;
		description = string;
		firePropertyChange("description",oldValue,description);
	}

	public void setName(String string) {
		Object oldValue = name;
		name = string;
		firePropertyChange("name",oldValue,name);
	}

	public String getName() {
		return name;
	}

}
