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
import java.util.Collection;
import java.util.Stack;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.model.ITocElement;


/**
 * @author sturmash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IndexBuilder {
    
    private Collection contributedIndexFiles;
    private Collection unprocessedIndexFiles;
    private Index index;
    private IndexEntry current;
	private Stack entries;
    private ITocElement[] tocs;
    
    /**
     * Constructs the index builder
     */
    public IndexBuilder() {
        unprocessedIndexFiles = new ArrayList();
        index = new Index();
		entries = new Stack();
        tocs = HelpPlugin.getTocManager().getTocs(Platform.getNL());
    }
    
    public void build(Collection contributedIndexFiles) {
        this.contributedIndexFiles = contributedIndexFiles;
        unprocessedIndexFiles.addAll(this.contributedIndexFiles);
        while (!unprocessedIndexFiles.isEmpty()) {
            IndexFile indexFile = (IndexFile) unprocessedIndexFiles.iterator().next();
            indexFile.build(this);
        }
    }

    /**
     * @param file
     */
    public void buildIndexFile(IndexFile file) {
        unprocessedIndexFiles.remove(file);
        IndexFileParser parser = new IndexFileParser(this);
        parser.parse(file);
    }
    /**
     * Adds a new entry to the index
     * @param keyword
     * @param hrefs
     */
    protected void addIndexEntry(String keyword) {
		Index currIndex = current == null ? index : current; 
        IndexEntry newEntry = currIndex.addEntry(keyword);
		if(current != null) entries.push(current);
		current = newEntry;
    }

    protected void exitIndexEntry() {
		if(entries.empty())
			current = null;
		else
			current = (IndexEntry)entries.pop();
    }

	protected void addTopic(String label, String href, String location) {
		boolean emptyLabel = label == null || label.length() == 0;
		boolean emptyLocation = location == null || location.length() == 0;
		
        if ( emptyLabel || emptyLocation ) {
			for (int i = 0; i < tocs.length; i++) {
	            ITopic topic = tocs[i].getTopic(href);
	            if (topic != null) {
					if(emptyLabel) {
						label = topic.getLabel();
						emptyLabel = false;
					}
					if(emptyLocation) {
						location = tocs[i].getLabel();
						emptyLocation = false;
					}
	            }
	        }
        }

		if(emptyLocation) location = ""; //$NON-NLS-1$
		if(emptyLabel) label = ""; //$NON-NLS-1$
		if (current != null) {
			current.addTopic(label,href,location);
		}
    }

//    /**
//     * @param string
//     * @return
//     */
//    private ITopic findTopicByHref(String href) {
//        for (int i = 0; i < tocs.length; i++) {
//            ITopic topic = tocs[i].getTopic(href);
//            if ((topic != null) && (topic.getHref().equals(href)))
//                return topic;
//            else
//                continue;
//        }
//        return null;
//    }

    /**
     * @return
     */
    protected IIndex getBuiltIndex() {
        return index;
    }

}
