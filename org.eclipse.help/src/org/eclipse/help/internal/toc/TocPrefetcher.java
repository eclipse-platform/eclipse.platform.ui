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
package org.eclipse.help.internal.toc;

import org.eclipse.help.IAnchor;
import org.eclipse.help.IFilter;
import org.eclipse.help.IInclude;
import org.eclipse.help.INode;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.Anchor;
import org.eclipse.help.internal.Filter;
import org.eclipse.help.internal.Include;
import org.eclipse.help.internal.Node;

/*
 * Pre-fetches all data from ITocContributions and returns cached data. Also
 * useful for copying ITocContributions.
 */
public class TocPrefetcher {

	public static TocContribution prefetch(ITocContribution original) {
		String id = original.getId();
		String categoryId = original.getCategoryId();
		String locale = original.getLocale();
		Toc toc = prefetch(original.getToc());
		String linkTo = original.getLinkTo();
		boolean isPrimary = original.isPrimary();
		String[] extraDocuments = original.getExtraDocuments();
		TocContribution contribution = new TocContribution(id, categoryId, locale, toc, linkTo, isPrimary, extraDocuments);
		toc.setTocContribution(contribution);
		return contribution;
	}
	
	private static Toc prefetch(IToc original) {
		String label = original.getLabel();
		String topic = original.getTopic(null).getHref();
		Toc toc = new Toc(label, topic);
		Node[] children = prefetchChildren(original.getChildren());
		toc.addChildren(children);
		return toc;
	}
	
	private static Topic prefetch(ITopic original) {
		String href = original.getHref();
		String label = original.getLabel();
		Topic topic = new Topic(href, label);
		Node[] children = prefetchChildren(original.getChildren());
		topic.addChildren(children);
		return topic;
	}
	
	private static Include prefetch(IInclude original) {
		String target = original.getTarget();
		Include include = new Include(target);
		Node[] children = prefetchChildren(original.getChildren());
		include.addChildren(children);
		return include;
	}
	
	private static Filter prefetch(IFilter original) {
		String expression = original.getExpression();
		Filter filter = new Filter(expression);
		Node[] children = prefetchChildren(original.getChildren());
		filter.addChildren(children);
		return filter;
	}
	
	private static Anchor prefetch(IAnchor original) {
		String id = original.getId();
		Anchor anchor = new Anchor(id);
		Node[] children = prefetchChildren(original.getChildren());
		anchor.addChildren(children);
		return anchor;
	}
	
	private static Node[] prefetchChildren(INode[] children) {
		Node[] copy = new Node[children.length];
		for (int i=0;i<children.length;++i) {
			INode node = children[i];
			if (node instanceof IToc) {
				copy[i] = prefetch((IToc)node);
			}
			else if (node instanceof ITopic) {
				copy[i] = prefetch((ITopic)node);
			}
			else if (node instanceof IInclude) {
				copy[i] = prefetch((IInclude)node);
			}
			else if (node instanceof IFilter) {
				copy[i] = prefetch((IFilter)node);
			}
			else if (node instanceof IAnchor) {
				copy[i] = prefetch((IAnchor)node);
			}
		}
		return copy;
	}
}
