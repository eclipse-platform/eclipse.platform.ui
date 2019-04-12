/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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

package org.eclipse.ui.internal.keys.model;

/**
 * @since 3.4
 *
 */
public class CommonModel extends ModelElement {

	public static final String PROP_SELECTED_ELEMENT = "selectedElement"; //$NON-NLS-1$
	private ModelElement selectedElement;

	/**
	 * @param kc
	 */
	public CommonModel(KeyController kc) {
		super(kc);
	}

	/**
	 * @return Returns the selectedContext.
	 */
	public ModelElement getSelectedElement() {
		return selectedElement;
	}

	/**
	 * @param selectedContext The selectedContext to set.
	 */
	public void setSelectedElement(ModelElement selectedContext) {
		ModelElement old = this.selectedElement;
		this.selectedElement = selectedContext;
		controller.firePropertyChange(this, PROP_SELECTED_ELEMENT, old, selectedContext);
	}

}
