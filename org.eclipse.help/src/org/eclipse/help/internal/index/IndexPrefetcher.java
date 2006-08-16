/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.index;

import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexContribution;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.INode;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.toc.Topic;

/*
 * Pre-fetches all data from IIndexContributions and returns cached data. Also
 * useful for copying IIndexContributions.
 */
public class IndexPrefetcher {

	public static IndexContribution prefetch(IIndexContribution original) {
		String id = original.getId();
		String locale = original.getLocale();
		Index index = prefetch(original.getIndex());
		IndexContribution contribution = new IndexContribution(id, index, locale);
		return contribution;
	}
	
	private static Index prefetch(IIndex original) {
		Index index = new Index();
		Node[] children = prefetchChildren(original.getChildren());
		index.addChildren(children);
		return index;
	}

	private static IndexEntry prefetch(IIndexEntry original) {
		String keyword = original.getKeyword();
		IndexEntry indexEntry = new IndexEntry(keyword);
		Node[] children = prefetchChildren(original.getChildren());
		indexEntry.addChildren(children);
		return indexEntry;
	}

	private static Topic prefetch(ITopic original) {
		String href = original.getHref();
		String label = original.getLabel();
		Topic topic = new Topic(href, label);
		Node[] children = prefetchChildren(original.getChildren());
		topic.addChildren(children);
		return topic;
	}
		
	private static Node[] prefetchChildren(INode[] children) {
		Node[] copy = new Node[children.length];
		for (int i=0;i<children.length;++i) {
			INode node = children[i];
			if (node instanceof IIndexEntry) {
				copy[i] = prefetch((IIndexEntry)node);
			}
			else if (node instanceof ITopic) {
				copy[i] = prefetch((ITopic)node);
			}
			else if (node instanceof IIndex) {
				copy[i] = prefetch((IIndex)node);
			}
		}
		return copy;
	}
}
