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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;


/**
 * @author sturmash
 * Help index implementation
 */
public class Index implements IIndex {
    
    protected Map entries;
    
    public static final class IgnoreCaseComparator implements Comparator {

        public int compare(Object left, Object right) {
            return ((String)left).compareToIgnoreCase((String) right);
        }
    }	

    public Index() {
        entries = new TreeMap(new IgnoreCaseComparator());
    }

    public Index(List entries) {
    	this();
    	for (Iterator i = entries.iterator(); i.hasNext();) {
    		IndexEntry entry = (IndexEntry)i.next();
    		this.entries.put(entry.getKeyword(), entry);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.help.internal.index.IIndex#addEntry(java.lang.String, java.util.Collection)
     */
    protected IndexEntry addEntry(String keyword) {
        IndexEntry oldEntry = (IndexEntry) entries.get(keyword);
        if (oldEntry == null) {
			oldEntry = new IndexEntry(keyword);
	        entries.put(keyword, oldEntry);
        }
		return oldEntry;
    }

	public Map getEntryMap() {
		return entries;
	}

	public IIndexEntry[] getEntries() {
		if (entries == null)
			return new IIndexEntry[0];

		Collection entryCollection = entries.values(); 
		IIndexEntry[] entryArray = new IIndexEntry[entryCollection.size()];
		entryCollection.toArray(entryArray);
		return entryArray;
	}
}
