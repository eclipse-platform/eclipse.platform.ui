/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.util.*;

import org.eclipse.help.*;
public class ContextsBuilder {
	protected PluginContexts contexts;
	private String definingPluginID;
	private String pluginID;
	/**
	 * Contexts Builder Constructor.
	 */
	public ContextsBuilder(PluginContexts pluginContexts) {
		this.contexts = pluginContexts;
	}
	public void build(RelatedTopic relatedTopic) {
		// set the href on the related topic
		String href = relatedTopic.getHref();
		if (href == null)
			relatedTopic.setHref(""); //$NON-NLS-1$
		else {
			if (!href.equals("") // no empty link //$NON-NLS-1$
					&& !href.startsWith("/") // no help url //$NON-NLS-1$
					&& href.indexOf(':') == -1) // no other protocols
			{
				relatedTopic.setHref("/" + definingPluginID + "/" + href); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	public void build(Context context) {
		context.setPluginID(pluginID);
		// if context with same Id exists, merge them
		Context existingContext = (Context) contexts.get(context.getShortId());
		if (existingContext != null) {
			mergeContexts(existingContext, context);
		} else {
			contexts.put(context.getShortId(), context);
		}
	}
	public void build(ContextsFile contextsFile) {
		this.pluginID = contextsFile.getPluginID();
		this.definingPluginID = contextsFile.getDefiningPluginID();
		ContextsFileParser parser = new ContextsFileParser(this);
		parser.parse(contextsFile);
	}
	public void build(List pluginContextsFiles) {
		for (Iterator contextFilesIt = pluginContextsFiles.iterator(); contextFilesIt
				.hasNext();) {
			ContextsFile contextsFile = (ContextsFile) contextFilesIt.next();
			contextsFile.build(this);
		}
	}
	/**
	 * Merges Text and Links from new Context into an existing Context
	 */
	private void mergeContexts(Context existingContext, Context newContext) {
		// Merge Text
		if (newContext.getStyledText() != null) {
			if (existingContext.getStyledText() != null) {
				existingContext.setStyledText(existingContext.getStyledText()
						+ "\n" //$NON-NLS-1$
						+ newContext.getStyledText());
			} else {
				existingContext.setStyledText(newContext.getStyledText());
			}
		}
		// Merge Related Links
		existingContext.getChildren().addAll(newContext.getChildren());
		removeDuplicateLinks(existingContext);
	}
	/**
	 * Filters out the duplicate related topics in a Context
	 */
	private void removeDuplicateLinks(Context context) {
		List links = context.getChildren();
		if (links == null || links.size() <= 0)
			return;
		List filtered = new ArrayList();
		for (Iterator it = links.iterator(); it.hasNext();) {
			IHelpResource topic1 = (IHelpResource) it.next();
			if (!isValidTopic(topic1))
				continue;
			boolean dup = false;
			for (int j = 0; j < filtered.size(); j++) {
				IHelpResource topic2 = (IHelpResource) filtered.get(j);
				if (!isValidTopic(topic2))
					continue;
				if (equalTopics(topic1, topic2)) {
					dup = true;
					break;
				}
			}
			if (!dup)
				filtered.add(topic1);
		}
		context.setChildren(filtered);
	}
	/**
	 * Checks if topic labels and href are not null and not empty strings
	 */
	private boolean isValidTopic(IHelpResource topic) {
		return topic != null && topic.getHref() != null
				&& !"".equals(topic.getHref()) && topic.getLabel() != null //$NON-NLS-1$
				&& !"".equals(topic.getLabel()); //$NON-NLS-1$
	}
	/**
	 * Check if two context topic are the same. They are considered the same if
	 * both labels and href are equal
	 */
	private boolean equalTopics(IHelpResource topic1, IHelpResource topic2) {
		return topic1.getHref().equals(topic2.getHref())
				&& topic1.getLabel().equals(topic2.getLabel());
	}
}
