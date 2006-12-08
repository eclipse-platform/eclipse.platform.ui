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

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.internal.base.HelpEvaluationContext;

public class TocModelSerializer {

	private static final String EMPTY_STRING = "";
	
	public static String serialize(IToc toc) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		buf.append(serializeAux(toc, ""));
		return buf.toString();
	}
	
	private static String serializeAux(IToc toc, String indent) {
		if (!UAContentFilter.isFiltered(toc, HelpEvaluationContext.getContext())) {
			StringBuffer buf = new StringBuffer();
			buf.append(indent + "<toc\n");
			buf.append(indent + "      label=\"" + toc.getLabel() + "\"\n");
			buf.append(indent + "      href=\"" + toc.getHref() + "\">\n");
			
			ITopic[] topics = toc.getTopics();
			for (int i=0;i<topics.length;++i) {
				buf.append(serializeAux(topics[i], indent + "   "));
			}
			
			buf.append(indent + "</toc>");
			return buf.toString();
		}
		return EMPTY_STRING;
	}
	
	private static String serializeAux(ITopic topic, String indent) {
		if (!UAContentFilter.isFiltered(topic, HelpEvaluationContext.getContext())) {
			StringBuffer buf = new StringBuffer();
			buf.append(indent + "<topic\n");
			buf.append(indent + "      label=\"" + topic.getLabel() + "\"\n");
			buf.append(indent + "      href=\"" + topic.getHref() + "\">\n");
			
			ITopic[] subtopics = topic.getSubtopics();
			for (int i=0;i<subtopics.length;++i) {
				buf.append(serializeAux(subtopics[i], indent + "   "));
			}
			
			buf.append(indent + "</topic>\n");
			return buf.toString();		
		}
		return EMPTY_STRING;
	}
}
