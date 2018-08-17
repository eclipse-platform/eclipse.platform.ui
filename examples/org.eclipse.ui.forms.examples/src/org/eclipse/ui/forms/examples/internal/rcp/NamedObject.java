/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
package org.eclipse.ui.forms.examples.internal.rcp;

/**
 *
 */
public	class NamedObject {
	private String name;
	protected SimpleModel model;

	public NamedObject(String name) {
		this.name = name;
	}
	public void setModel(SimpleModel model) {
		this.model = model;
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString() {
		return getName();
	}
	public void setName(String name) {
		this.name = name;
		model.fireModelChanged(new Object [] {this}, IModelListener.CHANGED, "");
	}
}
