/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * The ResourceFactory is used to save and recreate an IResource object.
 * As such, it implements the IPersistableElement interface for storage
 * and the IElementFactory interface for recreation.
 *
 * @see IMemento
 * @see IPersistableElement
 * @see IElementFactory
 */
public class WorkspaceFactory implements IElementFactory, IPersistableElement {
	private static final String FACTORY_ID = "org.eclipse.ui.internal.model.WorkspaceFactory";//$NON-NLS-1$

	/**
	 * Create a ResourceFactory.  This constructor is typically used
	 * for our IElementFactory side.
	 */
	public WorkspaceFactory() {
	}

	/**
	 * @see IElementFactory
	 */
	@Override
	public IAdaptable createElement(IMemento memento) {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * @see IPersistableElement
	 */
	@Override
	public String getFactoryId() {
		return FACTORY_ID;
	}

	/**
	 * @see IPersistableElement
	 */
	@Override
	public void saveState(IMemento memento) {
	}
}
