/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public IAdaptable createElement(IMemento memento) {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * @see IPersistableElement
     */
    public String getFactoryId() {
        return FACTORY_ID;
    }

    /**
     * @see IPersistableElement
     */
    public void saveState(IMemento memento) {
    }
}
