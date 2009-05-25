/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.views.properties;

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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((part == null) ? 0 : part.hashCode())
				+ ((getSelection() == null) ? 0 : getSelection().hashCode())
				+ ((getInput() == null) ? 0 : getInput().hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyShowInContext other = (PropertyShowInContext) obj;
		// part needs to be equal
		if (part == null) {
			if (other.part != null)
				return false;
		} else if (!part.equals(other.part))
			return false;
		// selection needs to be equal
		if (getSelection() == null) {
			if (other.getSelection() != null)
				return false;
		} else if (!getSelection().equals(other.getSelection()))
			return false;
		// input needs to be equal, but only if both are really set.
		// E.g. the property sheet doesn't have an input set if not created by ShowIn > ...
		if (getInput() == null || other.getInput() == null) {
				return true;
		} else if (!getInput().equals(other.getInput()))
			return false;
		return true;
	}
}
