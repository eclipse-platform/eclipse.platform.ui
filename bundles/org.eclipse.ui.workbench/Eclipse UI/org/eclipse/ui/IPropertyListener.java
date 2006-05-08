/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Interface for listening for property changes on an object.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IWorkbenchPart#addPropertyListener
 */
public interface IPropertyListener {
    /**
     * Indicates that a property has changed.
     *
     * @param source the object whose property has changed
     * @param propId the id of the property which has changed; property ids
     *   are generally defined as constants on the source class
     */
    public void propertyChanged(Object source, int propId);
}
