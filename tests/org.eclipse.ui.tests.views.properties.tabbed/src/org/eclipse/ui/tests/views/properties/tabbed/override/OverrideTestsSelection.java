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
package org.eclipse.ui.tests.views.properties.tabbed.override;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;

/**
 * The selection in the override tests view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsSelection implements ISelection {

	private Element element;

	/**
	 * Constructor for OverrideTestsSelection
	 *
	 * @param newElement
	 *            the selected element.
	 */
	public OverrideTestsSelection(Element newElement) {
		this.element = newElement;
	}

	/**
	 * Get the selected element.
	 *
	 * @return the selected element.
	 */
	public Element getElement() {
		return element;
	}

	@Override
	public boolean isEmpty() {
		/*
		 * Since we want to display UI when there is no selection (empty
		 * selection), we need to return false to isEmpty().
		 */
		return false;
	}

	@Override
	public String toString() {
		if (getElement() == null) {
			return super.toString();
		}
		return getElement().getName();
	}

}
