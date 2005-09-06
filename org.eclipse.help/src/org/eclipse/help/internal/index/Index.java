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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author sturmash
 * Help index implementation
 */
public class Index implements IIndex {
    
    Map entries;
    
    public static final class IgnoreCaseComparator implements Comparator {

        public int compare(Object left, Object right) {
            return ((String)left).compareToIgnoreCase((String) right);
        }
    }	
    public Index() {
        entries = new TreeMap(new IgnoreCaseComparator());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.help.internal.index.IIndex#addEntry(java.lang.String, java.util.Collection)
     */
    public IndexEntry addEntry(String keyword) {
        IndexEntry oldEntry = (IndexEntry) entries.get(keyword);
        if (oldEntry == null) {
			oldEntry = new IndexEntry(keyword);
	        entries.put(keyword, oldEntry);
        }
		return oldEntry;
    }
	
	public Map getEntries() {
		return entries;
	}
}
