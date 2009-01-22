/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.workingset;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.w3c.dom.*;

/**
 * Makes help resources adaptable and persistable
 */
public abstract class AdaptableHelpResource
		implements
			IAdaptable,
			IHelpResource {
	protected IHelpResource element;
	protected IAdaptable parent;

	/**
	 * This constructor will be called when wrapping help resources.
	 */
	public AdaptableHelpResource(IHelpResource element) {
		this.element = element;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IHelpResource.class)
			return element;
		return null;
	}

	public abstract void saveState(Element element);

	public abstract AdaptableHelpResource[] getChildren();

	public IAdaptable getParent() {
		return parent;
	}

	public void setParent(IAdaptable parent) {
		this.parent = parent;
	}

	/**
	 * Tests the receiver and the object for equality
	 * 
	 * @param object
	 *            object to compare the receiver to
	 * @return true=the object equals the receiver, the name is the same. false
	 *         otherwise
	 */
	public boolean equals(Object object) {
		if (this == object)
			return true;
		else if (object instanceof AdaptableHelpResource)
			return (element == ((AdaptableHelpResource) object).element);
		else if (object instanceof IHelpResource)
			return element == object;
		else
			return false;
	}

	/**
	 * Returns the hash code.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		if (element == null)
			return -1;
		return element.hashCode();
	}

	/**
	 * Returns a descendant topic with a specified href
	 */
	public abstract ITopic getTopic(String href);

	/**
	 * @see org.eclipse.help.IHelpResource#getHref()
	 */
	public String getHref() {
		return element.getHref();
	}

	/**
	 * @see org.eclipse.help.IHelpResource#getLabel()
	 */
	public String getLabel() {
		return element.getLabel();
	}

}
