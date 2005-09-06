/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.index.IIndexEntry;
import org.eclipse.help.internal.index.IIndexTopic;

/**
 * This class manages help working sets
 */
public class SelectTopicData extends IndexData {

	private IIndexEntry entry;
	public SelectTopicData( ServletContext context,
							HttpServletRequest request, 
							HttpServletResponse response) {
		super(context, request, response);
		entry = getIndexEntry(request.getParameter("entry").split("\\,"));

	}
	
	public void generateTopics(Writer out) throws IOException {
		List topics = entry.getTopics();
		for(int i = 0; i< topics.size(); i++) {
			IIndexTopic topic = (IIndexTopic) topics.get(i); 
			out.write("<tr><td class=\"c0\"><input type=\"radio\" name=\"hrefs\" value=\"");
			out.write(UrlUtil.getHelpURL(topic.getHref())); 
			out.write("\" id=\"r" + i + "\"></td>");
			out.write("<td class=\"c1\"><label for=\"r" + i + "\">");
			out.write(UrlUtil.htmlEncode(topic.getLabel()));
			out.write("</label></td><td class=\"c2\"><label for=\"r" + i +"\">");
			out.write(UrlUtil.htmlEncode(topic.getLocation()));
			out.write("</label></td></tr>");
		}
	}
}
