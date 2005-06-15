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
package org.eclipse.ui.views.properties;

/**
 * Listener for changes in objects of type 
 * <code>IPropertySheetEntry</code>.
 * <p>
 * This interface is public since it appears in the api
 * of <code>IPropertySheetEntry</code>.  It is not intended
 * to be implemented outside of this package.
 * <p>
 */
public interface IPropertySheetEntryListener {
    /**
     * A node's children have changed (children added or removed) 
     *
     * @param node the node whose's children have changed
     */
    void childEntriesChanged(IPropertySheetEntry node);

    /**
     * A entry's error message has changed
     *
     * @param entry the entry whose's error message has changed
     */
    void errorMessageChanged(IPropertySheetEntry entry);

    /**
     * A entry's value has changed 
     *
     * @param entry the entry whose's value has changed
     */
    void valueChanged(IPropertySheetEntry entry);
}
