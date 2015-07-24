/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
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

/**
 * @since 3.2
 *
 */
public class SimpleCart extends ModelObject {

	private int numItems;

	public int getNumItems() {
		return numItems;
	}

	public void setNumItems(int numItems) {
		firePropertyChange("numItems", this.numItems, this.numItems = numItems);
	}

}
