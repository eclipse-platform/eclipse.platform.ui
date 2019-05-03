/*******************************************************************************
 * Copyright (c) 2008, 2015 Versant Corp. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.views.properties;

import java.util.Objects;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ShowInContext;

/**
 * @since 3.4
 *
 */
public class PropertyShowInContext extends ShowInContext {

	private IWorkbenchPart part;

	/**
	 * @param aPart
	 * @param selection
	 */
	public PropertyShowInContext(IWorkbenchPart aPart, ISelection selection) {
		super(null, selection);
		part = aPart;
	}

	/**
	 * @param aPart
	 * @param aShowInContext
	 */
	public PropertyShowInContext(IWorkbenchPart aPart,
			ShowInContext aShowInContext) {
		super(aShowInContext.getInput(), aShowInContext.getSelection());
		part = aPart;
	}

	/**
	 * @return Returns the part.
	 */
	public IWorkbenchPart getPart() {
		return part;
	}

	/**
	 * @param part
	 *            The part to set.
	 */
	public void setPart(IWorkbenchPart part) {
		this.part = part;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(part)
				+ Objects.hashCode(getSelection())
				+ Objects.hashCode(getInput());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyShowInContext other = (PropertyShowInContext) obj;
		if (Objects.equals(part, other.part) && Objects.equals(getSelection(), other.getSelection())) {
			// input needs to be equal, but only if both are really set.
			// E.g. the property sheet doesn't have an input set if not created by ShowIn >
			// ...
			if (getInput() == null || other.getInput() == null) {
				return true;
			}
			return getInput().equals(other.getInput());
		}
		return false;
	}
}
