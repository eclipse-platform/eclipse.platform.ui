/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

public class Category extends ModelObject {

	private String name;

	private Adventure[] adventures = new Adventure[0];

	public void setName(String string) {
		Object oldValue = name;
		name = string;
		firePropertyChange("name", oldValue, name);
	}

	public void addAdventure(Adventure adventure) {
		adventures = (Adventure[]) append(adventures, adventure);
		firePropertyChange("adventures", null, null);
	}

	public Adventure[] getAdventures() {
		return adventures;
	}

	public String getName() {
		return name;
	}

}
