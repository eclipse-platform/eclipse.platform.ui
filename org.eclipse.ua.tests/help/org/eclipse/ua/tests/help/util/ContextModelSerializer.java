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
package org.eclipse.ua.tests.help.util;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.help.IContext;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.context.PluginContexts;

public class ContextModelSerializer {

	private static final String EMPTY_STRING = "";
	
	public static String serialize(PluginContexts contexts) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		buf.append(serializeAux(contexts, ""));
		return buf.toString();
	}
	
	private static String serializeAux(PluginContexts contexts, String indent) {
		StringBuffer buf = new StringBuffer();
		buf.append(indent + "<contexts>\n");
		
		Iterator iter = contexts.getMap().entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			buf.append(serializeAux(entry, indent + "   "));
		}

		buf.append(indent + "</contexts>");
		return buf.toString();
	}
	
	private static String serializeAux(Map.Entry entry, String indent) {
		String id = (String)entry.getKey();
		IContext context = (IContext)entry.getValue();
		StringBuffer buf = new StringBuffer();
		buf.append(indent + "<context id=\"" + id + "\">\n");
		buf.append(indent + "   <description>");
		buf.append(context.getText());
		buf.append("<description>\n");

		IHelpResource[] topics = context.getRelatedTopics();
		for (int i=0;i<topics.length;++i) {
			buf.append(serializeAux(topics[i], indent + "   "));
		}

		buf.append(indent + "</context>\n");
		return buf.toString();
	}
	
	private static String serializeAux(IHelpResource topic, String indent) {
		if (!UAContentFilter.isFiltered(topic)) {
			StringBuffer buf = new StringBuffer();
			buf.append(indent + "<topic\n");
			buf.append(indent + "      label=\"" + topic.getLabel() + "\"\n");
			buf.append(indent + "      href=\"" + topic.getHref() + "\">\n");
			buf.append(indent + "</topic>\n");
			return buf.toString();		
		}
		return EMPTY_STRING;
	}
}
