/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.util.List;

import org.eclipse.help.*;
import org.xml.sax.Attributes;
/**
 * Context object, as defined in the map.xml
 */
public class Context extends ContextsNode implements IContext {
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
		shortID = attrs.getValue("id");
	}
	public String getText() {
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
	public void setText(String s) {
		text = s;
	}
	/**
	 * Obtains short id (without plugin)
	 */
	public String getShortId() {
		return shortID;
	}
	public String getID() {
		return pluginID + "." + shortID;
	}
	/**
	 * Sets the pluginID.
	 * @param pluginID The pluginID to set
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
