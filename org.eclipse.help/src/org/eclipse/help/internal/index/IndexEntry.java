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

import java.util.ArrayList;
import java.util.List;

/**
 * @author sturmash
 *
 * An internal implementation of index entry
 */
class IndexEntry extends Index implements IIndexEntry {
    String keyword;
    List topics;
    
    public IndexEntry(String keyword) {
		this(keyword, new ArrayList());
	}

	public IndexEntry(String keyword, List topics) {
        this.keyword = keyword;
        this.topics = topics;
    }

	public void addTopic(String label, String href, String location) {
		topics.add(new IndexTopic(label, href, location));
	}
    /* (non-Javadoc)
     * @see org.eclipse.help.internal.index.IIndexEntry#getKeyword()
     */
    public String getKeyword() {
        return keyword;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.help.internal.index.IIndexEntry#getTopics()
     */
    public List getTopics() {
        return topics;
    }

}
