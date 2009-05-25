/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	public boolean isEmpty() {
		/*
		 * Since we want to display UI when there is no selection (empty
		 * selection), we need to return false to isEmpty().
		 */
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (getElement() == null) {
			return super.toString();
		}
		return getElement().getName();
	}

}
