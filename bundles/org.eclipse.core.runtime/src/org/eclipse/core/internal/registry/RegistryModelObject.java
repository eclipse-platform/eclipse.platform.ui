/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.internal.runtime.InternalPlatform;

/**
 * An object which has the general characteristics of all elements
 * in a plug-in manifest.
 * <p>
 * This class may be subclassed.
 * </p>
 */

public abstract class RegistryModelObject {
	// DTD properties (included in plug-in manifest)
	protected String name = null;

	/**
	 * Returns the name of this element.
	 * 
	 * @return the name of this element or <code>null</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this element.
	 * 
	 * @param value the new name of this element.  May be <code>null</code>.
	 */
	public void setName(String value) {
		name = value;
	}

	/**
	 * Return a string representation of this object. This value is not to be relied
	 * on and can change at any time. To be used for debugging purposes only.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getClass() + ":" + getName() + "[" + super.toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public Object getAdapter(Class type) {
		return InternalPlatform.getDefault().getAdapterManager().getAdapter(this, type);
	}

	/*
	 * Return null for the default case.
	 */
	ExtensionRegistry getRegistry() {
		return null;
	}

	/**
	 * Optimization to replace a non-localized key with its localized value.  Avoids having
	 * to access resource bundles for further lookups.
	 */
	public void setLocalizedName(String value) {
		name = value;
		((ExtensionRegistry) InternalPlatform.getDefault().getRegistry()).setDirty(true);
	}
}