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
package org.eclipse.help.internal.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.INode;
import org.eclipse.help.internal.Node;
import org.eclipse.help.internal.toc.Topic;

/*
 * Pre-fetches all data from IContexts and returns cached data. Also
 * useful for copying IContexts.
 */
public class ContextPrefetcher {

	public static IContext prefetch(IContext original) {
		String text = original.getText();
		Context context = new Context(text);
		IHelpResource[] topics = original.getRelatedTopics();
		if (topics != null) {
			for (int i=0;i<topics.length;++i) {
				context.addChild(prefetch(topics[i]));
			}
		}
		return context;
	}
		
	private static Topic prefetch(IHelpResource original) {
		String href = original.getHref();
		String label = original.getLabel();
		return new Topic(href, label);
	}

	private static class Context extends Node implements IContext {
		private String text;
	    private IHelpResource[] topics;
		
		public Context(String text) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
		public IHelpResource[] getRelatedTopics() {
			if (topics == null) {
				INode[] children = getChildren();
				if (children.length > 0) {
					List list = new ArrayList();
					for (int i=0;i<children.length;++i) {
						if (children[i] instanceof IHelpResource) {
							list.add(children[i]);
						}
					}
					topics = (IHelpResource[])list.toArray(new IHelpResource[list.size()]);
				}
				else {
					topics = new IHelpResource[0];
				}
			}
			return topics;
	    }
	}
}
