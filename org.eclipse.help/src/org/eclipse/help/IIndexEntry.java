/*******************************************************************************
 * Copyright (c) 2005 2006 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

/**
 * IIndexEntry represents a single entry of the help index. It includes
 * a keyword and related references into help content.
 * 
 * @since 3.2
 */
public interface IIndexEntry {
    /**
     * Returns the keyword that this entry is associated with
     *
     * @return the keyword
     */
    public String getKeyword();

    /**
     * Obtains help resources assosiated with this index entry (i.e. keyword).
     * 
     * @return Array of IHelpResource
     */
    public IHelpResource[] getTopics();

    /**
     * Obtains the index subentries contained in the entry.
     */
    public IIndexEntry[] getSubentries();
}
