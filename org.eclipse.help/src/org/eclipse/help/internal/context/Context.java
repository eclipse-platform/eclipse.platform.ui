/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Phil Loats (IBM Corp.) - fix to use only foundation APIs
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.util.*;

import org.eclipse.help.*;
import org.xml.sax.*;
/**
 * Context object, as defined in the map.xml
 */
public class Context extends ContextsNode implements IStyledContext {
	private String text;
	protected String pluginID;
	protected String shortID;
	/**
	 * Context constructor.
	 */
	public Context(Attributes attrs) {
		super(attrs);
		if (attrs == null)
			return;
		shortID = attrs.getValue("id"); //$NON-NLS-1$
	}
	private static String stripStyle(String s, String style) {
		int len = s.length();
		int skipLen = style.length();
		StringBuffer sb = new StringBuffer(s.length());
		int idx;
		int lastIdx=0;
		
		while ((idx = s.indexOf(style, lastIdx)) != -1) {
			int start = idx;
			boolean hasSpace = false;
			while ((start>lastIdx) && Character.isWhitespace(s.charAt(start-1))) { 
				start--;
			}
			if (idx != start)
				hasSpace = true;
			//add everthing to start
			sb.append(s.substring(lastIdx, start));
			
			// skip style
			idx += skipLen;
			int end = idx;
			while ((end<len) && Character.isWhitespace(s.charAt(end)))
				end++;
			if (end != idx)
				hasSpace = true;
			lastIdx = end;
			
			if (hasSpace)
				sb.append(' ');
		}
		
		// add everything else
		sb.append(s.substring(lastIdx, len));
		return sb.toString();
	}
	/**
	 * @return plain text (without <@#$b>or </@#$b> bug 59541)
	 */
	public String getText() {
		//PAL foundation
		/*
		return text
		// if there are spaces on any or both side of bold they need to be
				// collapsed to one
				.replaceAll("(\\s+</?@#\\$b>\\s*)|(\\s*</?@#\\$b>\\s+)", " ") //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll("</?@#\\$b>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		*/
		return stripStyle(stripStyle(text,"<@#$b>"),"</@#$b>");
	}
	/**
	 * @return styled text with <@#$b>and </@#$b> to mark bold range
	 */
	public String getStyledText() {
		return text;
	}
	public IHelpResource[] getRelatedTopics() {
		if (children.size() > 0) {
			IHelpResource[] related = new IHelpResource[children.size()];
			children.toArray(related);
			return related;
		} else {
			// signal empty toc. handled by calling class.
			return null;
		}
	}
	public void setStyledText(String s) {
		text = s;
	}
	/**
	 * Obtains short id (without plugin)
	 */
	public String getShortId() {
		return shortID;
	}
	public String getID() {
		return pluginID + "." + shortID; //$NON-NLS-1$
	}
	/**
	 * Sets the pluginID.
	 * 
	 * @param pluginID
	 *            The pluginID to set
	 */
	public void setPluginID(String pluginID) {
		this.pluginID = pluginID;
	}
	/**
	 * @see ContextsNode#build(ContextsBuilder)
	 */
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
	/**
	 * Replaces children list
	 */
	public void setChildren(List children) {
		this.children = children;
	}
}
