/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties;

/**
 * Interface used by <code>PropertySheetRoot</code> to obtain an 
 * <code>IPropertySource</code> for a given object.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IPropertySourceProvider {
    /**
     * Returns a property source for the given object.
     *
     * @param object the object
     */
    public IPropertySource getPropertySource(Object object);
}