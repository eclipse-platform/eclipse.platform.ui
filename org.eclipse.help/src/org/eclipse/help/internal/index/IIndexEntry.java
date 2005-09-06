/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.index;

import java.util.List;


/**
 * @author sturmash
 *
 * Represents a single entry of the help index
 */
public interface IIndexEntry extends IIndex {
    
    /**
     * Returns a keyword that this entry is associated with
     * @return
     */
    public String getKeyword();
    
    /**
     * Returns list of topics assosiated with this index entry (i.e. keyword)
     * @return List of IIndexTopic
     */
    public List getTopics();
        
}
