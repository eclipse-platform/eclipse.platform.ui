/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
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
