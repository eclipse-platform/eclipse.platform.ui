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

/**
 * An object which has the general characteristics of all the nestable elements
 * in a plug-in manifest.
 * <p>
 * This class may be subclassed.
 * </p>
 */

public abstract class NestedRegistryModelObject extends RegistryModelObject {
	private RegistryModelObject parent;

	/**
	 * Returns the plug-in model (descriptor or fragment) in which this extension is declared.
	 *
	 * @return the plug-in model in which this extension is declared
	 *  or <code>null</code>
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * Sets the plug-in model in which this extension is declared.
	 *
	 * @param value the plug-in model in which this extension is declared.  
	 *		May be <code>null</code>.
	 */
	public void setParent(RegistryModelObject value) {
		parent = value;
	}

	ExtensionRegistry getRegistry() {
		return parent == null ? null : parent.getRegistry();
	}
}